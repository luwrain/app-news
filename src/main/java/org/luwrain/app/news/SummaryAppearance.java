
package org.luwrain.app.news;

import java.util.*;

import org.luwrain.core.*;
import org.luwrain.controls.*;
import org.luwrain.pim.news.*;

class SummaryAppearance implements ListArea.Appearance
{
    private final Luwrain luwrain;
    private final Strings strings;

    SummaryAppearance(Luwrain luwrain, Strings strings)
    {
	NullCheck.notNull(luwrain, "luwrain");
	NullCheck.notNull(strings, "strings");
	this.luwrain = luwrain;
	this.strings = strings;
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
	    luwrain.say(strings.readPrefix() + " " + article.getTitle() + " " + luwrain.i18n().getPastTimeBrief(article.getPublishedDate()));
	    break;
	case NewsArticle.MARKED:
	    luwrain.say(strings.markedPrefix() + " " + article.getTitle() + " " + luwrain.i18n().getPastTimeBrief(article.getPublishedDate()));
	    break;
	default:
	    luwrain.say(article.getTitle() + " " + luwrain.i18n().getPastTimeBrief(article.getPublishedDate()));
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
