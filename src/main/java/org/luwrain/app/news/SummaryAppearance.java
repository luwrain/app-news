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

import org.luwrain.core.*;
import org.luwrain.controls.*;
import org.luwrain.extensions.pim.*;

class SummaryAppearance implements ListItemAppearance
{
    private Luwrain luwrain;
    private Strings strings;

    public SummaryAppearance(Luwrain luwrain, Strings strings)
    {
	this.luwrain = luwrain;
	this.strings = strings;
	if (luwrain == null)
	    throw new NullPointerException("luwrain may not be null");
	if (strings == null)
	    throw new NullPointerException("strings may not be null");
    }

    @Override public void introduceItem(Object item, int flags)
    {
	if (item == null || !(item instanceof StoredNewsArticle))
	    return;
	final StoredNewsArticle article = (StoredNewsArticle)item;
	switch(article.getState())
	{
	case NewsArticle.READ:
	    if ((flags & ListItemAppearance.BRIEF) == 0)
		luwrain.say(strings.readPrefix() + " " + article.getTitle()); else
		luwrain.say(article.getTitle());
	    break;
	case NewsArticle.MARKED:
	    if ((flags & ListItemAppearance.BRIEF) == 0)
		luwrain.say(strings.markedPrefix() + " " + article.getTitle()); else
		luwrain.say(article.getTitle());
	    break;
	default:
	    luwrain.say(article.getTitle());
	}
    }

    @Override public String getScreenAppearance(Object item, int flags)
    {
	if (item == null || !(item instanceof StoredNewsArticle))
	    return "  ";
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
