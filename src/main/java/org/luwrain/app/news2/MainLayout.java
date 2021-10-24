
package org.luwrain.app.news2;

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

import org.luwrain.app.news.Strings;

final class MainLayout extends LayoutBase
{
    private final App app;
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
		    NullCheck.notNull(event, "event");
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
		    if (event.getType() == SystemEvent.Type.BROADCAST)
			switch(event.getCode())
			{
			case PROPERTIES:
			//return showGroupProps();
			return false;
			default:
			return super.onSystemEvent(event);
			}
		    return super.onSystemEvent(event);
		}
	    };
	final Actions groupsActions = actions(
					      action("add-group", app.getStrings().actionAddGroup(), new InputEvent(InputEvent.Special.INSERT), this::actNewGroup),
					      action("delete-group", app.getStrings().actionDeleteGroup(), new InputEvent(InputEvent.Special.DELETE, EnumSet.of(InputEvent.Modifiers.SHIFT)), this::actDeleteGroup)
					      );

	this.summaryArea = new ListArea<NewsArticle>(listParams((params)->{
		    params.name = app.getStrings().summaryAreaName();
		    params.model = new ListModel<>(app.articles);
		    params.appearance = new SummaryAppearance();
		    params.clickHandler = (area, index, article)->onSummaryClick(article);
		})){
		@Override public boolean onInputEvent(InputEvent event)
		{
		    NullCheck.notNull(event, "event");
		    if (!event.isModified() && !event.isSpecial())
			switch (event.getChar())
			{
			case ' ':
			    return onSummarySpace();
			}
		    return super.onInputEvent(event);
		}
	    };
	final Actions summaryActions = actions();

	final ReaderArea.Params viewParams = new ReaderArea.Params();
	viewParams.context = getControlContext();
	this.viewArea = new ReaderArea(viewParams){
		@Override public boolean onSystemEvent(SystemEvent event)
		{
		    NullCheck.notNull(event, "event");
		    if (event.getType() != SystemEvent.Type.REGULAR)
			return super.onSystemEvent(event);
		    switch(event.getCode())
		    {
		    case OK:
			return false;//actions.onOpenUrl(this);
		    default:
			return super.onSystemEvent(event);
		    }
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
	if (app.openGroup(group.group))
	{
	    summaryArea.reset(false);
	    summaryArea.refresh();
	}
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
	app.loadGroups();
	groupsArea.refresh();
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
	    final Node root = doc.getRoot();
	    root.addSubnode(NodeBuilder.newParagraph(app.getStrings().articleUrl(article.getUrl())));
	    root.addSubnode(NodeBuilder.newParagraph(app.getStrings().articleTitle(article.getTitle())));
	    doc.commit();
	    viewArea.setDocument(doc, getLuwrain().getAreaVisibleWidth(viewArea));
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
