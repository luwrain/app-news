
package org.luwrain.app.news;

import org.luwrain.pim.news.StoredNewsGroup;

class NewsGroupWrapper
{
    private StoredNewsGroup group;
    private int newArticleCount;

    public NewsGroupWrapper(StoredNewsGroup group, int newArticleCount)
    {
	this.group = group;
	this.newArticleCount = newArticleCount;
    }

    public StoredNewsGroup getStoredGroup()
    {
	return group;
    }

    public String toString()
    {
	if (group == null)
	    return "";
	if (newArticleCount == 0)
	    return group.getName();
	return group.getName() + " (" + newArticleCount + ")";
    }
}
