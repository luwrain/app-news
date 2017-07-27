
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
