/*
   Copyright 2012-2019 Michael Pozhidaev <msp@luwrain.org>

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

import org.luwrain.core.*;
import org.luwrain.core.events.*;
import org.luwrain.controls.*;
import org.luwrain.controls.reader.*;
import org.luwrain.pim.news.*;
import org.luwrain.pim.*;

final class App implements Application, MonoApp
{
    private Luwrain luwrain = null;
    private Strings strings = null;
    private Base base = null;
    private Actions actions = null;
    private ActionLists actionLists = null;

    private ListArea groupsArea;
    private ListArea summaryArea;
    private ReaderArea viewArea;
    private AreaLayoutHelper layout = null;

    @Override public InitResult onLaunchApp(Luwrain luwrain)
    {
	NullCheck.notNull(luwrain, "luwrain");
	final Object o = luwrain.i18n().getStrings(Strings.NAME);
	if (o == null || !(o instanceof Strings))
	    return new InitResult(InitResult.Type.NO_STRINGS_OBJ, Strings.NAME);
	this.strings = (Strings)o;
	this.luwrain = luwrain;
	this.base = new Base(luwrain, strings);
	if (base.storing == null)
	    return new InitResult(InitResult.Type.FAILURE);
	this.actions = new Actions(luwrain, strings, base);
	this.actionLists = new ActionLists(strings);
	createAreas();
	this.layout = new AreaLayoutHelper(()->{
		luwrain.onNewAreaLayout();
		luwrain.announceActiveArea();
	    }, new AreaLayout(AreaLayout.LEFT_TOP_BOTTOM, groupsArea, summaryArea, viewArea));
	luwrain.runWorker(org.luwrain.pim.workers.News.NAME);
	return new InitResult();
    }

    private void createAreas()
    {
	final ListArea.Params groupsParams = new ListArea.Params();
	groupsParams.context = new DefaultControlContext(luwrain);
	groupsParams.model = base.newGroupsModel();
	groupsParams.appearance = new ListUtils.DefaultAppearance(groupsParams.context, Suggestions.CLICKABLE_LIST_ITEM);
	groupsParams.clickHandler = (area, index, obj)->{
	    if (!actions.onGroupsClick(summaryArea, obj))
		return false;
	    luwrain.setActiveArea(summaryArea);
	    return true;
	};
	groupsParams.name = strings.groupsAreaName();
	this.groupsArea = new ListArea(groupsParams) {
		      @Override public boolean onInputEvent(KeyboardEvent event)
		      {
			  NullCheck.notNull(event, "event");
			  if (event.isSpecial() && !event.isModified())
			      switch(event.getSpecial())
			      {
			      case TAB:
				  luwrain.setActiveArea(summaryArea);
			    return true;
			      case ESCAPE:
				  closeApp();
				  return true;
			      default:
				  return super.onInputEvent(event);
			      }
		    return super.onInputEvent(event);
		}
		@Override public boolean onSystemEvent(EnvironmentEvent event)
		{
		    NullCheck.notNull(event, "event");
		    if (event.getType() == EnvironmentEvent.Type.BROADCAST)
			switch(event.getCode())
			{
			case REFRESH:
			    if (event.getBroadcastFilterUniRef().startsWith("newsgroup:"))
			    refresh();
			    return true;
			default:
			    super.onSystemEvent(event);
			}
		    switch(event.getCode())
		    {
		    case REFRESH:
				luwrain.runWorker(org.luwrain.pim.workers.News.NAME);
				return super.onSystemEvent(event);
							case PROPERTIES:
			    return showGroupProps();
						    case ACTION:
										if (ActionEvent.isAction(event, "fetch"))
				  return actions.launchNewsFetch();
			if (ActionEvent.isAction(event, "mark-all-as-read"))
			    return actions.markAsReadWholeGroup(groupsArea, summaryArea, (GroupWrapper)groupsArea.selected());
									if (ActionEvent.isAction(event, "add-group"))
			    return actions.onAddGroup(groupsArea);
						if (ActionEvent.isAction(event, "delete-group"))
			    return actions.onDeleteGroup(groupsArea);
			if (ActionEvent.isAction(event, "show-with-read-only"))
			    return actions.setShowAllGroupsMode(groupsArea, true);
			if (ActionEvent.isAction(event, "hide-with-read-only"))
			    return actions.setShowAllGroupsMode(groupsArea, false);
			return false;
		    case CLOSE:
			closeApp();
			return true;
		    default:
			return super.onSystemEvent(event);
		    }
		}
		      @Override public Action[] getAreaActions()
		      {
			  return actionLists.getGroupsActions(this);
		      }
	    };

this.summaryArea = new ListArea(base.createSummaryParams((area, index, obj)->actions.onSummaryClick(summaryArea, groupsArea, viewArea, obj))) {
		@Override public boolean onInputEvent(KeyboardEvent event)
		{
		    NullCheck.notNull(event, "event");
		    if (event.isSpecial() && !event.isModified())
			switch(event.getSpecial())
			{
			case TAB:
			    luwrain.setActiveArea(viewArea);
			    return true;
			case BACKSPACE:
			    luwrain.setActiveArea(groupsArea);
			    return true;
			case ESCAPE:
			    closeApp();
			    return true;
			case F9:
			    return actions.launchNewsFetch();
			}
		    if (!event.isSpecial() && !event.isModified())
			switch (event.getChar())
			{
			case ' ':
			    return actions.onSummarySpace(groupsArea, summaryArea);
			}
		    return super.onInputEvent(event);
		}
		@Override public boolean onSystemEvent(EnvironmentEvent event)
		{
		    NullCheck.notNull(event, "event");
		    if (event.getType() != EnvironmentEvent.Type.REGULAR)
			return super.onSystemEvent(event);
		    switch(event.getCode())
		    {
		    case ACTION:
			return onSummaryAreaAction(event);
		    case CLOSE:
			closeApp();
			return true;
		    default:
			return super.onSystemEvent(event);
		    }
		}
		@Override public Action[] getAreaActions()
		{
		    return actionLists.getSummaryActions(this);
		}
	};

	final ReaderArea.Params viewParams = new ReaderArea.Params();
	viewParams.context = new DefaultControlContext(luwrain);

	this.viewArea = new ReaderArea(viewParams){
		@Override public boolean onInputEvent(KeyboardEvent event)
		{
		    NullCheck.notNull(event, "event");
		    if (event.isSpecial() && !event.isModified())
			switch(event.getSpecial())
			{
			case ENTER:
			    return actions.onOpenUrl(this);
			case TAB:
			    luwrain.setActiveArea(groupsArea);
			    return true;
			case BACKSPACE:
			    luwrain.setActiveArea(summaryArea);
			    return true;
			case ESCAPE:
			    closeApp();
			    return true;
			}
		    return super.onInputEvent(event);
		}
		@Override public boolean onSystemEvent(EnvironmentEvent event)
		{
		    NullCheck.notNull(event, "event");
		    if (event.getType() != EnvironmentEvent.Type.REGULAR)
			return super.onSystemEvent(event);
		    switch(event.getCode())
		    {
		    case OK:
			return actions.onOpenUrl(this);
		    case CLOSE:
			closeApp();
			return true;
		    default:
			return super.onSystemEvent(event);
		    }
		}
		@Override public String getAreaName()
		{
		    return strings.viewAreaName();
		}
	    };
    }

    private boolean onSummaryAreaAction(EnvironmentEvent event)
    {
	NullCheck.notNull(event, "event");
	return false;
 }

    private boolean showGroupProps()
    {
	final Object obj = groupsArea.selected();
	if (obj == null || !(obj instanceof GroupWrapper))
	    return false;
	final GroupWrapper wrapper = (GroupWrapper)obj;
	final FormArea area = new FormArea(new DefaultControlContext(luwrain), strings.groupPropertiesAreaName(wrapper.group.getName())) {
		@Override public boolean onInputEvent(KeyboardEvent event)
		{
		    NullCheck.notNull(event, "event");
		    if (event.isSpecial() && !event.isModified())
			switch(event.getSpecial())
			{
			case ESCAPE:
			    layout.closeTempLayout();
			    return true;
			}
		    return super.onInputEvent(event);
		}
		@Override public boolean onSystemEvent(EnvironmentEvent event)
		{
		    NullCheck.notNull(event, "event");
		    if (event.getType() != EnvironmentEvent.Type.REGULAR)
			return super.onSystemEvent(event);
		    switch(event.getCode())
		    {
		    case OK:
			try {
			    final String name = getEnteredText("name");
			    if (name.trim().isEmpty())
			    {
				luwrain.message(strings.groupPropertiesNameMayNotBeEmpty(), Luwrain.MessageType.ERROR);
				return true;
			    }
			    wrapper.group.setName(name);
			    if (getEnteredText("order-index").trim().isEmpty())
			    {
				luwrain.message(strings.groupPropertiesInvalidOrderIndex(), Luwrain.MessageType.ERROR);
				return true;
			    }
			    final int orderIndex;
			    try {
				orderIndex = Integer.parseInt(getEnteredText("order-index"));
			    }
			    catch(NumberFormatException e)
			    {
				luwrain.message(strings.groupPropertiesInvalidOrderIndex(), Luwrain.MessageType.ERROR);
				return true;
			    }
			    if (orderIndex < 0)
			    {
				luwrain.message(strings.groupPropertiesInvalidOrderIndex(), Luwrain.MessageType.ERROR);
				return true;
			    }
			    wrapper.group.setOrderIndex(orderIndex);
			    final List<String> urls = new LinkedList();
			    for(String s: getMultilineEditLines())
				if (!s.trim().isEmpty())
				    urls.add(s.trim());
			    wrapper.group.setUrls(urls.toArray(new String[urls.size()]));
			    groupsArea.refresh();
			    layout.closeTempLayout();
			    return true;
			}
			catch(PimException e)
			{
			    luwrain.crash(e);
			    return true;
			}
		    case CLOSE:
			closeApp();
			return true;
		    default:
			return super.onSystemEvent(event);
		    }
		}
	    };
	final String[] urls = wrapper.group.getUrls();
	final String[] urlLines;
	if (urls.length != 0)
	{
	    final List<String> res = new LinkedList();
	    for(String s: urls)
		res.add(s);
	    res.add("");
	    urlLines = res.toArray(new String[res.size()]);
	} else
	    urlLines = new String[0];
	area.addEdit("name", strings.groupPropertiesName(), wrapper.group.getName());
	area.addEdit("order-index", strings.groupPropertiesOrderIndex(), "" + wrapper.group.getOrderIndex());
	area.activateMultilineEdit(strings.groupPropertiesUrls(), urlLines, true);
	layout.openTempArea(area);
	return true;
    }

    @Override public void closeApp()
    {
	luwrain.closeApp();
    }

    @Override public String getAppName()
    {
	return strings.appName();
    }

    @Override public MonoApp.Result onMonoAppSecondInstance(Application app)
    {
	NullCheck.notNull(app, "app");
	return MonoApp.Result.BRING_FOREGROUND;
    }

    @Override public AreaLayout getAreaLayout()
    {
	return layout.getLayout();
    }
}
