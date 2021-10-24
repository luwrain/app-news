
package org.luwrain.app.news2;

import org.luwrain.core.*;
import org.luwrain.pim.news.*;

final class GroupWrapper
{
    final NewsGroup group;
    final int newArticleCount;

    GroupWrapper(NewsGroup group, int newArticleCount)
    {
	NullCheck.notNull(group, "group");
	this.group = group;
	this.newArticleCount = newArticleCount;
    }

    @Override public String toString()
    {
	if (group == null)
	    return "";
	if (newArticleCount == 0)
	    return group.getName();
	return group.getName() + " (" + String.valueOf(newArticleCount) + ")";
    }
}
