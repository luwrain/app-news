
package org.luwrain.app.news;

import java.net.*;

import org.luwrain.core.*;
import org.luwrain.controls.*;
import org.luwrain.doctree.control.*;
import org.luwrain.doctree.loading.*;
import org.luwrain.pim.news.*;

class Actions
{
    private final Luwrain luwrain;

    Actions(Luwrain luwrain)
    {
	NullCheck.notNull(luwrain, "luwrain");
	this.luwrain = luwrain;
    }

    boolean onSummarySpace(Base base,
			   ListArea groupsArea, ListArea summaryArea)
    {
	NullCheck.notNull(base, "base");
	NullCheck.notNull(groupsArea, "groupsArea");
	NullCheck.notNull(summaryArea, "summaryArea");
	final Object obj = summaryArea.selected();
	if (obj == null || !(obj instanceof StoredNewsArticle))
	    return false;
	if (!base.markAsRead((StoredNewsArticle)obj))
	    return false;
	luwrain.onAreaNewContent(summaryArea);
	groupsArea.refresh();
	final int index = summaryArea.selectedIndex();
	if (index + 1 >= base.getSummaryModel().getItemCount())
	    luwrain.setActiveArea(groupsArea); else
	    summaryArea.select(index + 1, true);
	return true;
    }

    boolean launchNewsFetch()
    {
	luwrain.launchApp("fetch", new String[]{"--NEWS"});
	return true;
    }

    boolean onSummaryClick(Base base, 
			   ListArea summaryArea, DoctreeArea viewArea,
			   Object obj)
    {
	NullCheck.notNull(base, "base");
	NullCheck.notNull(summaryArea, "summaryArea");
	NullCheck.notNull(viewArea, "viewArea");
	NullCheck.notNull(obj, "obj");
	if (!(obj instanceof StoredNewsArticle))
	    return false;
	final StoredNewsArticle article = (StoredNewsArticle)obj;
	base.markAsRead(article);
	luwrain.onAreaNewContent(summaryArea);
	try {
	final StringLoader.Result res = new StringLoader(article.getContent(), "text/html", new URL(article.getUrl())).load();
	if (res.type() == StringLoader.Result.Type.OK)
	    viewArea.setDocument(res.doc, luwrain.getAreaVisibleWidth(viewArea));
	}
	catch(MalformedURLException e)
	{
	    //FIXME:
	    luwrain.message("url", Luwrain.MESSAGE_ERROR);
	    e.printStackTrace();
	    return true;
	}
luwrain.setActiveArea(viewArea);
	return true;
    }

    boolean setShowAllGroupsMode(Base base, ListArea groupsArea, boolean value)
    {
	NullCheck.notNull(base, "base");
	NullCheck.notNull(groupsArea, "groupsArea");
	base.getGroupsModel().setShowAllMode(value);
	groupsArea.refresh();
	return true;
    }

    boolean onGroupsClick(Base base, ListArea summaryArea, Object obj)
    {
	NullCheck.notNull(base, "base");
	NullCheck.notNull(summaryArea, "summaryArea");
	NullCheck.notNull(obj, "obj");
	if (!(obj instanceof NewsGroupWrapper))
	    return false;
	final NewsGroupWrapper group = (NewsGroupWrapper)obj;
	if (group.getStoredGroup() != base.getSummaryModel().getGroup())
	{
	    base.getSummaryModel().setGroup(group.getStoredGroup());
	    summaryArea.refresh(); 
	    summaryArea.resetHotPoint();
	    //FIXME:reset view;
	}
	//	gotoSummary();
	return true;
    }

    boolean markAsReadWholeGroup(Base base, ListArea groupsArea,
				 ListArea summaryArea, NewsGroupWrapper group)
    {
	NullCheck.notNull(base, "base");
	NullCheck.notNull(groupsArea, "groupsArea");
	NullCheck.notNull(summaryArea, "summaryArea");
	NullCheck.notNull(group, "group");
	if (base.markAsReadWholeGroup(group.getStoredGroup()))
	{
	    groupsArea.refresh();
	    groupsArea.announceSelected();
	}
	base.getSummaryModel().setGroup(null);
	summaryArea.refresh();
	return true;
    }
}
