
package org.luwrain.app.news;

import org.luwrain.core.*;
import org.luwrain.pim.*;
import org.luwrain.pim.news.*;

class Base
{
    private Luwrain luwrain;
    private Strings strings;
    private NewsStoring storing;
    private GroupsModel groupsModel;
    private SummaryModel summaryModel;
    private SummaryAppearance summaryAppearance;

    boolean init(Luwrain luwrain, Strings strings)
    {
	NullCheck.notNull(luwrain, "luwrain");
	NullCheck.notNull(strings, "strings");
	this.luwrain = luwrain;
	this.strings = strings;
	final Object f =  luwrain.getSharedObject("luwrain.pim.news");
	if (f == null || !(f instanceof org.luwrain.pim.news.Factory))
	    return false;
	storing = ((org.luwrain.pim.news.Factory)f).createNewsStoring();
	if (storing == null)
	    return false;
	groupsModel = new GroupsModel(storing); 
	summaryModel = new SummaryModel(storing);
	summaryAppearance = new SummaryAppearance(luwrain, strings);
	return true;
    }

    NewsStoring getStoring()
    {
	return storing;
    }

    GroupsModel getGroupsModel()
    {
	return groupsModel;
    }

    SummaryModel getSummaryModel()
    {
	return summaryModel;
    }

    SummaryAppearance getSummaryAppearance()
    {
	return summaryAppearance;
    }

    boolean markAsRead(StoredNewsArticle article)
    {
	NullCheck.notNull(article, "article");
	try {
	    if (article.getState() == NewsArticle.NEW)
		article.setState(NewsArticle.READ);
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
	    articles = storing.loadArticlesInGroupWithoutRead(group);
	    if (articles == null || articles.length < 1)
		return true;
	    for(StoredNewsArticle a: articles)
		if (a.getState() == NewsArticle.NEW)
		    a.setState(NewsArticle.READ);
	    return true;
	}
	catch (PimException e)
	{
	    luwrain.crash(e);
	    return false;
	}
    }
}
