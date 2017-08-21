/*
   Copyright 2012-2017 Michael Pozhidaev <michael.pozhidaev@gmail.com>

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
import org.luwrain.pim.news.*;

class SummaryModel implements ListArea.Model
{
    private final NewsStoring newsStoring;
    private StoredNewsGroup group;
    private StoredNewsArticle[] articles;

    SummaryModel(NewsStoring newsStoring)
    {
	NullCheck.notNull(newsStoring, "newsStoring");
	this.newsStoring = newsStoring;
	group = null;
	articles = null;
    }

    void setGroup(StoredNewsGroup group)
    {
	this.group = group;
	articles = null;
    }

    StoredNewsGroup getGroup()
    {
	return group;
    }

    @Override public int getItemCount()
    {
	return articles != null?articles.length:0;
    }

    @Override public Object getItem(int index)
    {
	if (articles == null)
	    return null;
	if (index < 0 || index >= articles.length)
	    throw new IllegalArgumentException("index equal to " + index + " must be between 0 and " + articles.length);
	return articles[index];
    }

    @Override public void refresh()
    {
	if (group == null)
	{
	    articles = null;
	    return;
	}
	try {
	    articles = newsStoring.loadArticlesInGroupWithoutRead(group);
	    if (articles == null || articles.length < 1)
		articles = newsStoring.loadArticlesInGroup(group);
	    if (articles != null)
		Arrays.sort(articles);
	}
	catch(Exception e)
	{
	    e.printStackTrace();
	    articles = null;
	}
    }

    /*
    @Override public boolean toggleMark(int index)
    {
	if (articles == null || index >= articles.length)
	    return false;
	final StoredNewsArticle article = articles[index];
	try {
	    if (article.getState() == NewsArticle.MARKED)
		article.setState(NewsArticle.READ); else
		article.setState(NewsArticle.MARKED);
	}
	catch (Exception e)
	{
	    e.printStackTrace();
	    return false;
	}
	return true;
    }
    */
}
