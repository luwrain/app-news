/*
   Copyright 2012-2016 Michael Pozhidaev <michael.pozhidaev@gmail.com>

   This file is part of the LUWRAIN.

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

class SummaryAppearance implements ListArea.Appearance
{
    private Luwrain luwrain;
    private Strings strings;

    SummaryAppearance(Luwrain luwrain, Strings strings)
    {
	this.luwrain = luwrain;
	this.strings = strings;
	NullCheck.notNull(luwrain, "luwrain");
	NullCheck.notNull(strings, "strings");
    }

    @Override public void announceItem(Object item, Set<Flags> flags)
    {
	NullCheck.notNull(item, "item");
	NullCheck.notNull(flags, "flags");
	final StoredNewsArticle article = (StoredNewsArticle)item;
	luwrain.playSound(Sounds.LIST_ITEM);
	if (flags.contains(Flags.BRIEF))
	{
	    luwrain.say(article.getTitle());
	    return;
	}
	switch(article.getState())
	{
	case NewsArticle.READ:
	    luwrain.say(strings.readPrefix() + " " + article.getTitle() + " " + luwrain.i18n().pastTimeBrief(article.getPublishedDate()));
	    break;
	case NewsArticle.MARKED:
	    luwrain.say(strings.markedPrefix() + " " + article.getTitle() + " " + luwrain.i18n().pastTimeBrief(article.getPublishedDate()));
	    break;
	default:
	    luwrain.say(article.getTitle() + " " + luwrain.i18n().pastTimeBrief(article.getPublishedDate()));
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
