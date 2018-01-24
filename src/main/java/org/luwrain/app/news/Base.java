/*
   Copyright 2012-2018 Michael Pozhidaev <michael.pozhidaev@gmail.com>

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

class Base
{
    private final Luwrain luwrain;
    private final Strings strings;
final NewsStoring storing;
    private SummaryAppearance summaryAppearance;

    private NewsGroupWrapper[] groups = new NewsGroupWrapper[0];
    private StoredNewsGroup group = null;
    private StoredNewsArticle[] articles = new StoredNewsArticle[0];
    private boolean showAllGroups = false;

Base(Luwrain luwrain, Strings strings)
    {
	NullCheck.notNull(luwrain, "luwrain");
	NullCheck.notNull(strings, "strings");
	this.luwrain = luwrain;
	this.strings = strings;
	this.storing = org.luwrain.pim.Connections.getNewsStoring(luwrain, true);
	if (storing == null)
	    return;
	loadGroups();
	summaryAppearance = new SummaryAppearance(luwrain, strings);
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

    NewsStoring getStoring()
    {
	return storing;
    }

    void setShowAllGroups(boolean value)
    {
	this.showAllGroups = value;
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
	    articles = storing.getArticles().loadWithoutRead(group);
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
	    final List<NewsGroupWrapper> w = new LinkedList<NewsGroupWrapper>();
	    final StoredNewsGroup[] g = storing.getGroups().load();
	    Arrays.sort(g);
	    int[] newCounts = storing.getArticles().countNewInGroups(g);
	    int[] markedCounts = storing.getArticles().countMarkedInGroups(g);
	    for(int i = 0;i < g.length;++i)
	    {
		final int newCount = i < newCounts.length?newCounts[i]:0;
		final int markedCount = i < markedCounts.length?markedCounts[i]:0;
		if (showAllGroups || newCount > 0 || markedCount > 0)
		    w.add(new NewsGroupWrapper(g[i], newCount));
	    }
	    groups = w.toArray(new NewsGroupWrapper[w.size()]);
	}
	catch(PimException e)
	{
	    groups = new NewsGroupWrapper[0];
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

    ListArea.Model newGroupsModel()
    {
	return new GroupsModel();
    }

    ListArea.Model newArticlesModel()
    {
	return new ArticlesModel();
    }

    private class GroupsModel implements ListArea.Model
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

    private class ArticlesModel implements ListArea.Model
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
}
