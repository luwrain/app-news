//import java.util.*;

package org.luwrain.app.news;

import org.luwrain.core.*;
import org.luwrain.controls.*;
import org.luwrain.pim.news.*;

class SummaryAppearance implements ListItemAppearance
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

    @Override public void introduceItem(Object item, int flags)
    {
	if (item == null || !(item instanceof StoredNewsArticle))
	    return;
	final StoredNewsArticle article = (StoredNewsArticle)item;
	luwrain.playSound(Sounds.NEW_LIST_ITEM);
	if ((flags & ListItemAppearance.BRIEF) != 0)
	{
	    luwrain.say(article.getTitle());
	    return;
	}
	switch(article.getState())
	{
	case NewsArticle.READ:
	    luwrain.say(strings.readPrefix() + " " + article.getTitle() + " " + strings.passedTimeBrief(article.getPublishedDate()));
	    break;
	case NewsArticle.MARKED:
	    luwrain.say(strings.markedPrefix() + " " + article.getTitle() + " " + strings.passedTimeBrief(article.getPublishedDate()));
	    break;
	default:
	    luwrain.say(article.getTitle() + " " + strings.passedTimeBrief(article.getPublishedDate()));
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
