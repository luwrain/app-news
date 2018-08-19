/*
   Copyright 2012-2018 Michael Pozhidaev <michael.pozhidaev@gmail.com>

   This file is part of LUWRAIN.

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
import java.net.*;

import org.luwrain.core.*;
import org.luwrain.controls.*;
import org.luwrain.doctree.*;
import org.luwrain.controls.doc.*;
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
	group.name = name;
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
	if (obj == null || !(obj instanceof StoredNewsArticle))
	    return false;
	if (!base.markAsRead((StoredNewsArticle)obj))
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

    boolean onSummaryClick(ListArea summaryArea, ListArea groupsArea,
DocumentArea viewArea, Object obj)
    {
	NullCheck.notNull(summaryArea, "summaryArea");
	NullCheck.notNull(groupsArea, "groupsArea");
	NullCheck.notNull(viewArea, "viewArea");
	NullCheck.notNull(obj, "obj");
	if (!(obj instanceof StoredNewsArticle))
	    return false;
	final DocumentBuilder docBuilder = new DocumentBuilderLoader().newDocumentBuilder(luwrain, ContentTypes.TEXT_HTML_DEFAULT);
	if (docBuilder == null)
	    return false;
	final StoredNewsArticle article = (StoredNewsArticle)obj;
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
	    root.addSubnode(NodeFactory.newPara(strings.articleUrl(article.getUrl())));
	    root.addSubnode(NodeFactory.newPara(strings.articleTitle(article.getTitle())));
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

    boolean onOpenUrl(DocumentArea area)
    {
	NullCheck.notNull(area, "area");
	final Document doc = area.getDocument();
	if (doc == null || doc.getUrl() == null)
	    return false;
	luwrain.launchApp("reader", new String[]{doc.getUrl().toString()});
	return true;
    }
}
