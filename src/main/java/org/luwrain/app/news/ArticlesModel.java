/*
   Copyright 2012-2015 Michael Pozhidaev <msp@altlinux.org>

   This file is part of the Luwrain.

   Luwrain is free software; you can redistribute it and/or
   modify it under the terms of the GNU General Public
   License as published by the Free Software Foundation; either
   version 3 of the License, or (at your option) any later version.

   Luwrain is distributed in the hope that it will be useful,
   but WITHOUT ANY WARRANTY; without even the implied warranty of
   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
   General Public License for more details.
*/

package org.luwrain.app.news;

import java.util.*;

import org.luwrain.core.Log;
import org.luwrain.controls.*;
import org.luwrain.extensions.pim.*;

class ArticlesModel implements ListModel
{
    private NewsStoring newsStoring;
    private StoredNewsGroup group;
    private StoredNewsArticle[] articles;

    public ArticlesModel(NewsStoring newsStoring)
    {
	this.newsStoring = newsStoring;
	if (newsStoring == null)
	    throw new NullPointerException("newsStoring may not be null");
	group = null;
	articles = null;
    }

    public void setGroup(StoredNewsGroup group)
    {
	this.group = group;
	articles = null;
    }

    public StoredNewsGroup getGroup()
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
	    //	    articles = newsStoring.loadNewsArticlesOfGroup(group);

	    articles = newsStoring.loadNewsArticlesInGroupWithoutRead(group);
	    if (articles == null || articles.length < 1)
		articles = newsStoring.loadNewsArticlesOfGroup(group);


	}
	catch(Exception e)
	{
	    Log.error("news", "could not construct list of groups:" + e.getMessage());
	    e.printStackTrace();
	    articles = null;
	}
    }
}
