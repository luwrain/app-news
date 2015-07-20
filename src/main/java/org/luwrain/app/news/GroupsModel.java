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
import org.luwrain.pim.news.*;

class GroupsModel implements ListModel
{
    private NewsStoring newsStoring;
    private NewsGroupWrapper[] items;
    private boolean showAllMode = false;

    public GroupsModel(NewsStoring newsStoring)
    {
	this.newsStoring = newsStoring;
	showAllMode = false;
	refresh();
    }

    public void setShowAllMode(boolean value)
    {
	showAllMode = value;
    }

    @Override public int getItemCount()
    {
	return items != null?items.length:0;
    }

    @Override public Object getItem(int index)
    {
	if (items == null || 
	    index < 0 ||
	    index >= items.length)
	    return null;
	return items[index];
    }

    @Override public void refresh()
    {
	if (newsStoring == null)
	{
	    items = null;
	    return;
	}
	Vector<NewsGroupWrapper> w = new Vector<NewsGroupWrapper>();
	try {
	    StoredNewsGroup[] groups = newsStoring.loadNewsGroups();
	    Arrays.sort(groups);
	    int[] newCounts = newsStoring.countNewArticlesInGroups(groups);
	    int[] markedCounts = newsStoring.countMarkedArticlesInGroups(groups);
	    for(int i = 0;i < groups.length;++i)
	    {
		final int newCount = i < newCounts.length?newCounts[i]:0;
		final int markedCount = i < markedCounts.length?markedCounts[i]:0;
    if (showAllMode || newCount > 0 || markedCount > 0)
	w.add(new NewsGroupWrapper(groups[i], newCount));
	    }
	}
	catch(Exception e)
	{
	    e.printStackTrace();
	    items = null;
	}
	items = w.toArray(new NewsGroupWrapper[w.size()]);
    }

    @Override public boolean toggleMark(int index)
    {
	return false;
    }
}
