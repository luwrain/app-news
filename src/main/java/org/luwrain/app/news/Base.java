/*
   Copyright 2012-2019 Michael Pozhidaev <msp@luwrain.org>

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
import org.luwrain.controls.*;
import org.luwrain.pim.*;
import org.luwrain.pim.news.*;

final class Base
{
    private final Luwrain luwrain;
    private final Strings strings;
final NewsStoring storing;
    final Conversations conv;
    private SummaryAppearance summaryAppearance;

    private GroupWrapper[] groups = new GroupWrapper[0];
    private StoredNewsGroup group = null;
    private StoredNewsArticle[] articles = new StoredNewsArticle[0];
    private boolean showAllGroups = false;

    Base(Luwrain luwrain, Strings strings)
    {
	NullCheck.notNull(luwrain, "luwrain");
	NullCheck.notNull(strings, "strings");
	this.luwrain = luwrain;
	this.strings = strings;
	this.conv = new Conversations(luwrain, strings);
	this.storing = org.luwrain.pim.Connections.getNewsStoring(luwrain, true);
	if (storing == null)
	    return;
	loadGroups();
	summaryAppearance = new SummaryAppearance();
    }

    boolean openGroup(StoredNewsGroup newGroup)
    {
	NullCheck.notNull(newGroup, "newGroup");
	if (this.group != null && this.group == newGroup)
	    return false;
	this.group = newGroup;
	loadArticles();
	return true;
    }

    void closeGroup()
    {
	group = null;
	articles = new StoredNewsArticle[0];
    }

    void setShowAllGroups(boolean value)
    {
	this.showAllGroups = value;
    }

    boolean markAsRead(StoredNewsArticle article)
    {
	NullCheck.notNull(article, "article");
	try {
	    if (article.getState() == NewsArticle.NEW)
	    {
		article.setState(NewsArticle.READ);
		article.save();
	    }
	    return true;
	}
	catch (Exception e)
	{
	    luwrain.crash(e);
	    return false;
	}
    }

    boolean markAsReadWholeGroup(StoredNewsGroup group)
    {
	NullCheck.notNull(group, "group");
	try {
	    StoredNewsArticle articles[];
	    articles = storing.getArticles().loadWithoutRead(group);
	    if (articles == null || articles.length < 1)
		return true;
	    for(StoredNewsArticle a: articles)
		if (a.getState() == NewsArticle.NEW)
		{
		    a.setState(NewsArticle.READ);
		    a.save();
		}
	    return true;
	}
	catch (PimException e)
	{
	    luwrain.crash(e);
	    return false;
	}
    }

boolean toggleArticleMark(int index)
    {
	if (articles == null || 
index < 0 || index >= articles.length)
	    return false;
	final StoredNewsArticle article = articles[index];
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

    void loadGroups()
    {
	try {
	    final List<GroupWrapper> w = new LinkedList();
	    final StoredNewsGroup[] g = storing.getGroups().load();
	    Arrays.sort(g);
	    int[] newCounts = storing.getArticles().countNewInGroups(g);
	    int[] markedCounts = storing.getArticles().countMarkedInGroups(g);
	    for(int i = 0;i < g.length;++i)
	    {
		final int newCount = i < newCounts.length?newCounts[i]:0;
		final int markedCount = i < markedCounts.length?markedCounts[i]:0;
		if (showAllGroups || newCount > 0 || markedCount > 0)
		    w.add(new GroupWrapper(g[i], newCount));
	    }
	    groups = w.toArray(new GroupWrapper[w.size()]);
	}
	catch(PimException e)
	{
	    groups = new GroupWrapper[0];
	    luwrain.crash(e);
	}
    }

void loadArticles()
    {
	if (group == null)
	{
	    articles = new StoredNewsArticle[0];
	    return;
	}
	try {
	    articles = storing.getArticles().loadWithoutRead(group);
	    if (articles == null || articles.length < 1)
		articles = storing.getArticles().load(group);
	    if (articles != null)
		Arrays.sort(articles);
	}
	catch(PimException e)
	{
	    luwrain.crash(e);
	    articles = new StoredNewsArticle[0];
	}
    }

    ListArea.Params createGroupsListParams(ListArea.ClickHandler clickHandler)
    {
	NullCheck.notNull(clickHandler, "clickHandler");
	final ListArea.Params params = new ListArea.Params();
	params.context = new DefaultControlContext(luwrain);
	params.model = new GroupsModel();
	params.appearance = new ListUtils.DefaultAppearance(params.context, Suggestions.CLICKABLE_LIST_ITEM);
	params.clickHandler = clickHandler;
	params.name = strings.groupsAreaName();
	return params;
    }

    ListArea.Params createSummaryParams(ListArea.ClickHandler clickHandler)
    {
	NullCheck.notNull(clickHandler, "clickHandler");
	final ListArea.Params params = new ListArea.Params();
	params.context = new DefaultControlContext(luwrain);
	params.model = new SummaryModel();
	params.appearance = new SummaryAppearance();
	params.clickHandler = clickHandler;
	params.name = strings.summaryAreaName();
	params.flags.add(ListArea.Flags.AREA_ANNOUNCE_SELECTED);
	return params;
    }

    private final class GroupsModel implements ListArea.Model
{
    @Override public int getItemCount()
    {
	return groups.length;
    }
    @Override public Object getItem(int index)
    {
	if (index < 0 || index >= groups.length)
	    throw new IllegalArgumentException("Illegal index value (" + index + ")");
	return groups[index];
    }
    @Override public void refresh()
    {
	loadGroups();
    }
}

    private final class SummaryModel implements ListArea.Model
{
    @Override public int getItemCount()
    {
	return articles.length;
    }
    @Override public Object getItem(int index)
    {
	if (index < 0 || index >= articles.length)
	    throw new IllegalArgumentException("Illegal index value (" + index + ")");
	return articles[index];
    }
    @Override public void refresh()
    {
	loadArticles();
    }
}

    private final class SummaryAppearance implements ListArea.Appearance
    {
	@Override public void announceItem(Object item, Set<Flags> flags)
	{
	    NullCheck.notNull(item, "item");
	    NullCheck.notNull(flags, "flags");
	    final StoredNewsArticle article = (StoredNewsArticle)item;
	    final String title = luwrain.getSpeakableText(article.getTitle(), Luwrain.SpeakableTextType.NATURAL);
	    if (flags.contains(Flags.BRIEF))
	    {
		luwrain.setEventResponse(DefaultEventResponse.text(Sounds.LIST_ITEM, title));
		return;
	    }
	    switch(article.getState())
	    {
	    case NewsArticle.READ:
		luwrain.setEventResponse(DefaultEventResponse.text(Sounds.LIST_ITEM, strings.readPrefix() + " " + title + " " + luwrain.i18n().getPastTimeBrief(article.getPublishedDate())));
		break;
	    case NewsArticle.MARKED:
		luwrain.setEventResponse(DefaultEventResponse.text(strings.markedPrefix() + " " + title + " " + luwrain.i18n().getPastTimeBrief(article.getPublishedDate())));
		break;
	    default:
		luwrain.setEventResponse(DefaultEventResponse.text(Sounds.LIST_ITEM, title + " " + luwrain.i18n().getPastTimeBrief(article.getPublishedDate())));
	    }
	}
	@Override public String getScreenAppearance(Object item, Set<Flags> flags)
	{
	    NullCheck.notNull(item, "item");
	    NullCheck.notNull(flags, "flags");
	    final StoredNewsArticle article = (StoredNewsArticle)item;
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
	@Override public int getObservableLeftBound(Object item)
	{
	    if (item == null || !(item instanceof StoredNewsArticle))
		return 0;
	    return 2;
	}
	@Override public int getObservableRightBound(Object item)
	{
	    if (item == null || !(item instanceof StoredNewsArticle))
		return 0;
	    final StoredNewsArticle article = (StoredNewsArticle)item;
	    return article.getTitle().length() + 2;
	}
    }
}
