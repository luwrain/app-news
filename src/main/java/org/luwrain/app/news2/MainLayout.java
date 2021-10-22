
package org.luwrain.app.news2;

import java.util.*;

import org.luwrain.core.*;
import org.luwrain.core.events.*;
import org.luwrain.controls.*;
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
		    params.model = new ListModel<>(app.groups);
	params.appearance = new DefaultAppearance<>(params.context, Suggestions.CLICKABLE_LIST_ITEM);
				    /*
	params.clickHandler = (area, index, group)->{
		    if (!onGroupsClick(group))
			return false;
		    setActiveArea(summaryArea);
		    return true;
	    };
				    */


	    })){
				/*
		    case REFRESH:
			luwrain.runWorker(org.luwrain.pim.workers.News.NAME);
			return super.onSystemEvent(event);
		    case PROPERTIES:
			return showGroupProps();
			*/
	    };
			
		this.summaryArea = new ListArea<>(listParams((params)->{
			    //			    params.clickHandler = (area, index, obj)->actions.onSummaryClick
			    params.model = new ListModel<>(app.articles);
	params.appearance = new SummaryAppearance();
				/*
(summaryArea, groupsArea, viewArea, obj))) {
			switch (event.getChar())
			{
			case ' ':
			    return actions.onSummarySpace(groupsArea, summaryArea);
			};

				*/
			}));
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

        boolean markAsRead(NewsArticle article)
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
	    app.crash(e);
	    return false;
	}
    }

    /*
    boolean markAsReadWholeGroup(NewsGroup group)
    {
	NullCheck.notNull(group, "group");
	try {
	    NewsArticle articles[];
	    articles = storing.getArticles().loadWithoutRead(group);
	    if (articles == null || articles.length < 1)
		return true;
	    for(NewsArticle a: articles)
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
    */

        private boolean onGroupsClick(GroupWrapper group)
    {
	NullCheck.notNull(group, "group");
	/*
	if (base.openGroup(group.group))
	{
	    summaryArea.redraw(); 
	    summaryArea.resetHotPoint();
	}
	*/
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
