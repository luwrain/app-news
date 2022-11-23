/*
   Copyright 2012-2022 Michael Pozhidaev <msp@luwrain.org>

   This file is part of LUWRAIN.

   LUWRAIN is free software; you can redistribute it and/or
   modify it under the terms of the GNU General Public
   License as published by the Free Software Foundation; either
   version 3 of the License, or (at your option) any later version.

   LUWRAIN is distributed in the hope that it will be useful,
   but WITHOUT ANY WARRANTY; without even the implied warranty of
   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
   General Public License for more details.
*/

package org.luwrain.app.news;

import java.util.*;

import org.luwrain.core.*;
import org.luwrain.core.events.*;
import org.luwrain.controls.*;
import org.luwrain.reader.*;
import org.luwrain.controls.reader.*;
import org.luwrain.pim.news.*;
import org.luwrain.pim.*;
import org.luwrain.app.base.*;
import org.luwrain.controls.ListUtils.*;

import static org.luwrain.core.DefaultEventResponse.*;

final class MainLayout extends LayoutBase
{
    final App app;
    final ListArea<GroupWrapper> groupsArea;
    final ListArea<NewsArticle> summaryArea;
    final ReaderArea viewArea;

    MainLayout(App app)
    {
	super(app);
	this.app = app;
	this.groupsArea = new ListArea<GroupWrapper>(listParams((params)->{
		    params.name = app.getStrings().groupsAreaName();
		    params.model = new ListModel<GroupWrapper>(app.groups){
			    @Override public void refresh() { app.loadGroups(); }
			};
		    params.appearance = new DefaultAppearance<>(params.context, Suggestions.CLICKABLE_LIST_ITEM);
		    params.clickHandler = (area, index, group)->onGroupsClick(group);
		})){
		@Override public boolean onSystemEvent(SystemEvent event)
		{
		    if (event.getType() == SystemEvent.Type.BROADCAST)
			switch(event.getCode())
			{
			case REFRESH:
			if (event.getBroadcastFilterUniRef().startsWith("newsgroup:"))
			    refresh();
			return true;
			default:
			super.onSystemEvent(event);
			}
		    if (event.getType() == SystemEvent.Type.REGULAR)
			switch(event.getCode())
			{
			case PROPERTIES:
			return editGroupProps();
			}
		    return super.onSystemEvent(event);
		}
	    };
	final Actions groupsActions = actions(
					      action("show-read", app.getStrings().actionShowWithReadOnly(), new InputEvent('='), ()->setShowAllGroupsMode(true)),
					      action("hide-read", app.getStrings().actionHideWithReadOnly(), new InputEvent('-'),()->setShowAllGroupsMode(false)),
					      action("delete-group", app.getStrings().actionDeleteGroup(), new InputEvent(InputEvent.Special.DELETE, EnumSet.of(InputEvent.Modifiers.SHIFT)), this::actDeleteGroup),
					      action("add-group", app.getStrings().actionAddGroup(), new InputEvent(InputEvent.Special.INSERT), this::actNewGroup)
					      );
	this.summaryArea = new ListArea<NewsArticle>(listParams((params)->{
		    params.name = app.getStrings().summaryAreaName();
		    params.model = new ListModel<>(app.articles);
		    params.appearance = new SummaryAppearance();
		    params.clickHandler = (area, index, article)->onSummaryClick(article);
		    params.flags.add(ListArea.Flags.AREA_ANNOUNCE_SELECTED);
		})){
		@Override public boolean onInputEvent(InputEvent event)
		{
		    if (!event.isModified() && !event.isSpecial())
			switch (event.getChar())
			{
			case ' ':
			return onSummarySpace();
			}
		    return super.onInputEvent(event);
		}
		@Override public boolean onSystemEvent(SystemEvent event)
		{
		    if (event.getType() == SystemEvent.Type.REGULAR)
			switch(event.getCode())
			{
			case OK:
			return openArticleUrl();
			}
		    return super.onSystemEvent(event);
		}
	    };
	final Actions summaryActions = actions();
	final ReaderArea.Params viewParams = new ReaderArea.Params();
	viewParams.context = getControlContext();
	this.viewArea = new ReaderArea(viewParams){
		@Override public boolean onSystemEvent(SystemEvent event)
		{
		    if (event.getType() == SystemEvent.Type.REGULAR)
			switch(event.getCode())
			{
			case OK:
			    return false;//actions.onOpenUrl(this);
			}
		    return super.onSystemEvent(event);
		}
		@Override public String getAreaName()
		{
		    return app.getStrings().viewAreaName();
		}
	    };
	final Actions viewActions = actions();
	setAreaLayout(AreaLayout.LEFT_TOP_BOTTOM, groupsArea, groupsActions, summaryArea, summaryActions, viewArea, viewActions);
    }

    /*
    boolean toggleArticleMark(int index)
    {
	if (articles == null || 
index < 0 || index >= articles.length)
	    return false;
	final NewsArticle article = articles[index];
	try {
	    if (article.getState() == NewsArticle.MARKED)
		article.setState(NewsArticle.READ); else
		article.setState(NewsArticle.MARKED);
	    article.save();
	return true;
	}
	catch (PimException e)
	{
	    luwrain.crash(e);
	    return true;
	}
    }
    */

    private boolean actMarkAsReadWholeGroup()
    {
	final GroupWrapper wrapper = groupsArea.selected();
	if (wrapper == null)
	    return false;
	final NewsGroup group = wrapper.group;
	final NewsArticle[] articles = app.getStoring().getArticles().loadWithoutRead(group);
	if (articles == null)
	    return true;
	for(NewsArticle a: articles)
	    if (a.getState() == NewsArticle.NEW)
	    {
		a.setState(NewsArticle.READ);
		a.save();
	    }
	return true;
    }

    private boolean onGroupsClick(GroupWrapper group)
    {
	NullCheck.notNull(group, "group");
	app.openGroup(group.group);
	summaryArea.reset(false);
	summaryArea.refresh();
	setActiveArea(summaryArea);
	return true;
    }

    private boolean actNewGroup()
    {
	final String name = app.getConv().newGroupName();
	if (name == null)
	    return false;
	final NewsGroup group = new NewsGroup();
	group.setName(name);
	app.getStoring().getGroups().save(group);
	app.showAllGroups = true;
	app.loadGroups();
	groupsArea.refresh();
	for(GroupWrapper g: app.groups)
	    if (g.group.getName().equals(name))
	    {
		groupsArea.select(g, true);
		break;
	    }
	return true;
    }

    private boolean actDeleteGroup()
    {
	final GroupWrapper wrapper = groupsArea.selected();
	if (wrapper == null)
	    return false;
	if (!app.getConv().confirmGroupDeleting(wrapper))
	    return true;
	app.getStoring().getGroups().delete(wrapper.group);
	app.loadGroups();
	groupsArea.refresh();
	return true;
    }

    private boolean openArticleUrl()
    {
	final NewsArticle article = summaryArea.selected();
	if (article == null)
	    return false;
	markAsRead(article);
	final String url = article.getUrl();
	if (url == null || url.trim().isEmpty())
	    return false;
	app.getLuwrain().launchApp("reader", new String[]{url.trim()});
		return true;
    }

    private boolean editGroupProps()
    {
	final GroupWrapper wrapper = groupsArea.selected();
	if (wrapper == null)
	    return false;
	final PropertiesLayout propLayout = new PropertiesLayout(app, wrapper.group, ()->{
		app.setAreaLayout(MainLayout.this);
		app.getLuwrain().announceActiveArea();
		groupsArea.refresh();
		return true;
	    });
	app.setAreaLayout(propLayout);
	app.getLuwrain().announceActiveArea();
	return true;
    }

    private boolean onSummarySpace()
    {
	final NewsArticle article = summaryArea.selected();
	if (article == null)
	    return false;
	if (!markAsRead(article))
	    return false;
	summaryArea.refresh();
	groupsArea.refresh();
	final int index = summaryArea.selectedIndex();
	if (index + 1 >= summaryArea.getListModel().getItemCount())
	    setActiveArea(groupsArea); else
	    summaryArea.select(index + 1, true);
	return true;
    }

    private boolean onSummaryClick(NewsArticle article)
    {
	NullCheck.notNull(article, "article");
	final DocumentBuilder docBuilder = new DocumentBuilderLoader().newDocumentBuilder(getLuwrain(), ContentTypes.TEXT_HTML_DEFAULT);
	if (docBuilder == null)
	    return false;
	markAsRead(article);
	summaryArea.refresh();
	groupsArea.refresh();
	final Properties props = new Properties();
	props.setProperty("url", article.getUrl());
	final Document doc = docBuilder.buildDoc(article.getContent(), props);
	if (doc != null)
	{
	    doc.commit();
	    viewArea.setDocument(doc, getAreaVisibleWidth(viewArea));
	}
	setActiveArea(viewArea);
	return true;
    }

    private boolean markAsRead(NewsArticle article)
    {
	NullCheck.notNull(article, "article");
	if (article.getState() == NewsArticle.NEW)
	{
	    article.setState(NewsArticle.READ);
	    article.save();
	}
	return true;
    }

    boolean setShowAllGroupsMode(boolean value)
    {
	app.showAllGroups = value;
	groupsArea.refresh();
	app.getLuwrain().playSound(Sounds.OK);
	return true;
    }

    /*
    boolean markAsReadWholeGroup(ListArea groupsArea, ListArea summaryArea, GroupWrapper group)
    {
	NullCheck.notNull(groupsArea, "groupsArea");
	NullCheck.notNull(summaryArea, "summaryArea");
	NullCheck.notNull(group, "group");
	if (base.markAsReadWholeGroup(group.group))
	{
	    groupsArea.refresh();
	    groupsArea.announceSelected();
	}
	base.closeGroup();
	summaryArea.refresh();
	return true;
    }

    boolean onOpenUrl(ReaderArea area)
    {
	NullCheck.notNull(area, "area");
	final Document doc = area.getDocument();
	if (doc == null || doc.getUrl() == null)
	    return false;
	luwrain.launchApp("reader", new String[]{doc.getUrl().toString()});
	return true;
    }
}
    */

    final class SummaryAppearance implements ListArea.Appearance<NewsArticle>
    {
	@Override public void announceItem(NewsArticle article, Set<Flags> flags)
	{
	    NullCheck.notNull(article, "article");
	    NullCheck.notNull(flags, "flags");
	    final String title = getLuwrain().getSpeakableText(article.getTitle(), Luwrain.SpeakableTextType.NATURAL);
	    if (flags.contains(Flags.BRIEF))
	    {
		app.setEventResponse(DefaultEventResponse.text(Sounds.LIST_ITEM, title));
		return;
	    }
	    switch(article.getState())
	    {
	    case NewsArticle.READ:
		app.setEventResponse(text(Sounds.LIST_ITEM, app.getStrings().readPrefix() + " " + title + " " + app.getI18n().getPastTimeBrief(article.getPublishedDate())));
		break;
	    case NewsArticle.MARKED:
		app.setEventResponse(text(app.getStrings().markedPrefix() + " " + title + " " + app.getI18n().getPastTimeBrief(article.getPublishedDate())));
		break;
	    default:
		app.setEventResponse(text(Sounds.LIST_ITEM, title + " " + app.getI18n().getPastTimeBrief(article.getPublishedDate())));
	    }
	}
	@Override public String getScreenAppearance(NewsArticle article, Set<Flags> flags)
	{
	    NullCheck.notNull(article, "article");
	    NullCheck.notNull(flags, "flags");
	    switch(article.getState())
	    {
	    case NewsArticle.NEW:
		return " [" + article.getTitle() + "]";
	    case NewsArticle.MARKED:
		return "* " + article.getTitle();
	    default:
		return "  " + article.getTitle();
	    }
	}
	@Override public int getObservableLeftBound(NewsArticle article)
	{
	    return 2;
	}
	@Override public int getObservableRightBound(NewsArticle article)
	{
	    return article.getTitle().length() + 2;
	}
    }
}
