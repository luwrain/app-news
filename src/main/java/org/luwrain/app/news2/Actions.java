
package org.luwrain.app.news2;

import java.util.*;
import java.net.*;

import org.luwrain.core.*;
import org.luwrain.controls.*;
import org.luwrain.reader.*;
import org.luwrain.controls.reader.*;
import org.luwrain.pim.news.*;

final class Actions
{
    private final Luwrain luwrain;
    private final Strings strings;
    private final Base base;

    Actions(Luwrain luwrain, Strings strings, Base base)
    {
	NullCheck.notNull(luwrain, "luwrain");
	NullCheck.notNull(strings, "strings");
	NullCheck.notNull(base, "base");
	this.luwrain = luwrain;
	this.strings = strings;
	this.base = base;
    }

    boolean onAddGroup(ListArea groupsArea)
    {
	NullCheck.notNull(groupsArea, "groupsArea");
	final String name = base.conv.newGroupName();
	if (name == null)
	    return false;
	final NewsGroup group = new NewsGroup();
	group.setName(name);
	try {
	    base.storing.getGroups().save(group);
	}
	catch(org.luwrain.pim.PimException e)
	{
	    luwrain.crash(e);
	    return true;
	}
	base.loadGroups();
	groupsArea.redraw();
	return true;
    }

    boolean onDeleteGroup(ListArea groupsArea)
    {
	NullCheck.notNull(groupsArea, "groupsArea");
	final Object obj = groupsArea.selected();
	if (obj == null || !(obj instanceof GroupWrapper))
	    return false;
	final GroupWrapper wrapper = (GroupWrapper)obj;
	if (!base.conv.confirmGroupDeleting(wrapper))
	    return true;
	try {
	    base.storing.getGroups().delete(wrapper.group);
	}
	catch(org.luwrain.pim.PimException e)
	{
	    luwrain.crash(e);
	    return true;
	}
	base.loadGroups();
	groupsArea.redraw();
	return true;
    }

    boolean onSummarySpace(ListArea groupsArea, ListArea summaryArea)
    {
	NullCheck.notNull(groupsArea, "groupsArea");
	NullCheck.notNull(summaryArea, "summaryArea");
	final Object obj = summaryArea.selected();
	if (obj == null || !(obj instanceof NewsArticle))
	    return false;
	if (!base.markAsRead((NewsArticle)obj))
	    return false;
	summaryArea.redraw();
	groupsArea.refresh();
	final int index = summaryArea.selectedIndex();
	if (index + 1 >= summaryArea.getListModel().getItemCount())
	    luwrain.setActiveArea(groupsArea); else
	    summaryArea.select(index + 1, true);
	return true;
    }

    boolean launchNewsFetch()
    {
	luwrain.launchApp("fetch", new String[]{"--NEWS"});
	return true;
    }

    boolean onSummaryClick(ListArea summaryArea, ListArea groupsArea, ReaderArea viewArea, Object obj)
    {
	NullCheck.notNull(summaryArea, "summaryArea");
	NullCheck.notNull(groupsArea, "groupsArea");
	NullCheck.notNull(viewArea, "viewArea");
	NullCheck.notNull(obj, "obj");
	if (!(obj instanceof NewsArticle))
	    return false;
	final DocumentBuilder docBuilder = new DocumentBuilderLoader().newDocumentBuilder(luwrain, ContentTypes.TEXT_HTML_DEFAULT);
	if (docBuilder == null)
	    return false;
	final NewsArticle article = (NewsArticle)obj;
	base.markAsRead(article);
	summaryArea.redraw();
	groupsArea.refresh();
	//	try {
	    final Properties props = new Properties();
	    props.setProperty("url", article.getUrl());
	    final Document doc = docBuilder.buildDoc(article.getContent(), props);
	if (doc != null)
	{
	    final Node root = doc.getRoot();
	    root.addSubnode(NodeBuilder.newParagraph(strings.articleUrl(article.getUrl())));
	    root.addSubnode(NodeBuilder.newParagraph(strings.articleTitle(article.getTitle())));
	    doc.commit();
	    viewArea.setDocument(doc, luwrain.getAreaVisibleWidth(viewArea));
	}
	/*
	}
	catch(MalformedURLException e)
	{
	    luwrain.message("url", Luwrain.MessageType.ERROR);//FIXME:
	    return true;
	}
	*/
luwrain.setActiveArea(viewArea);
	return true;
    }

    boolean setShowAllGroupsMode(ListArea groupsArea, boolean value)
    {
	NullCheck.notNull(groupsArea, "groupsArea");
	base.setShowAllGroups(value);
	groupsArea.refresh();
	luwrain.playSound(Sounds.OK);
	return true;
    }

    boolean onGroupsClick(ListArea summaryArea, Object obj)
    {
	NullCheck.notNull(summaryArea, "summaryArea");
	NullCheck.notNull(obj, "obj");
	if (!(obj instanceof GroupWrapper))
	    return false;
	final GroupWrapper group = (GroupWrapper)obj;
	if (base.openGroup(group.group))
	{
	    summaryArea.redraw(); 
	    summaryArea.resetHotPoint();
	}
	return true;
    }

    boolean markAsReadWholeGroup(ListArea groupsArea, ListArea summaryArea, GroupWrapper group)
    {
	NullCheck.notNull(groupsArea, "groupsArea");
	NullCheck.notNull(summaryArea, "summaryArea");
	NullCheck.notNull(group, "group");
	if (base.markAsReadWholeGroup(group.group))
	{
	    groupsArea.refresh();
	    groupsArea.announceSelected();
	}
	base.closeGroup();
	summaryArea.refresh();
	return true;
    }

    boolean onOpenUrl(ReaderArea area)
    {
	NullCheck.notNull(area, "area");
	final Document doc = area.getDocument();
	if (doc == null || doc.getUrl() == null)
	    return false;
	luwrain.launchApp("reader", new String[]{doc.getUrl().toString()});
	return true;
    }
}
