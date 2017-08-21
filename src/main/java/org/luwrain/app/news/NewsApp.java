/*
   Copyright 2012-2017 Michael Pozhidaev <michael.pozhidaev@gmail.com>

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
import org.luwrain.controls.doctree.*;
import org.luwrain.pim.news.*;
import org.luwrain.pim.*;

class NewsApp implements Application, MonoApp
{
    static private final String STRINGS_NAME = "luwrain.news";

    private Luwrain luwrain = null;
    private Strings strings = null;
    private final Base base = new Base();
    private Actions actions = null;
    private ActionLists actionLists = null;

    private ListArea groupsArea;
    private ListArea summaryArea;
    private DoctreeArea viewArea;

    @Override public InitResult onLaunchApp(Luwrain luwrain)
    {
	NullCheck.notNull(luwrain, "luwrain");
	final Object o = luwrain.i18n().getStrings(STRINGS_NAME);
	if (o == null || !(o instanceof Strings))
	    return new InitResult(InitResult.Type.NO_STRINGS_OBJ, STRINGS_NAME);
	strings = (Strings)o;
	this.luwrain = luwrain;
	if (!base.init(luwrain, strings))
	    return new InitResult(InitResult.Type.FAILURE);
	this.actions = new Actions(luwrain, strings);
	this.actionLists = new ActionLists(strings);
	createAreas();
	return new InitResult();
    }

    private void createAreas()
    {

	final ListArea.Params groupsParams = new ListArea.Params();
	groupsParams.context = new DefaultControlEnvironment(luwrain);
	groupsParams.model = base.newGroupsModel();
	groupsParams.appearance = new ListUtils.DefaultAppearance(groupsParams.context, Suggestions.CLICKABLE_LIST_ITEM);
	groupsParams.clickHandler = (area, index, obj)->{
	    if (!actions.onGroupsClick(base, summaryArea, obj))
		return false;
	    luwrain.setActiveArea(summaryArea);
	    return true;
	};
	groupsParams.name = strings.groupsAreaName();
	groupsParams.flags = EnumSet.of(ListArea.Flags.EMPTY_LINE_BOTTOM);

	groupsArea = new ListArea(groupsParams) {
		      @Override public boolean onKeyboardEvent(KeyboardEvent event)
		      {
			  NullCheck.notNull(event, "event");
			  if (event.isSpecial() && !event.isModified())
			      switch(event.getSpecial())
			      {
			      case TAB:
				  luwrain.setActiveArea(summaryArea);
			    return true;
			      default:
				  return super.onKeyboardEvent(event);
			      }
			  if (!event.isSpecial() && !event.isModified())
			      switch(event.getChar())
			      {
			      case '=':
				  return actions.setShowAllGroupsMode(base, groupsArea, true);
			      case '-':
				  return actions.setShowAllGroupsMode(base, groupsArea, false);
			      }
		    return super.onKeyboardEvent(event);
		}
		@Override public boolean onEnvironmentEvent(EnvironmentEvent event)
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
			    super.onEnvironmentEvent(event);
			}
		    switch(event.getCode())
		    {
		    case ACTION:
			return onGroupsAreaAction(event);
		    case CLOSE:
			closeApp();
			return true;
		    default:
			return super.onEnvironmentEvent(event);
		    }
		}
		      @Override public Action[] getAreaActions()
		      {
			  return actionLists.getGroupsActions(this);
		      }
	    };

	final ListArea.Params summaryParams = new ListArea.Params();
	summaryParams.context = new DefaultControlEnvironment(luwrain);
	summaryParams.model = base.getSummaryModel();
	summaryParams.appearance = new SummaryAppearance(luwrain, strings);
	summaryParams.clickHandler = (area, index, obj)->actions.onSummaryClick(base, summaryArea, viewArea, obj);
	summaryParams.name = strings.summaryAreaName();
	summaryParams.flags = EnumSet.of(ListArea.Flags.EMPTY_LINE_BOTTOM);

	summaryArea = new ListArea(summaryParams) {
		@Override public boolean onKeyboardEvent(KeyboardEvent event)
		{
		    if (event.isSpecial() && !event.isModified())
			switch(event.getSpecial())
			{
			case TAB:
			    luwrain.setActiveArea(viewArea);
			    return true;
			case BACKSPACE:
			    luwrain.setActiveArea(groupsArea);
			    return true;
			case F9:
			    return actions.launchNewsFetch();
			}
		    if (!event.isSpecial() && !event.isModified())
			switch (event.getChar())
			{
			case ' ':
			    return actions.onSummarySpace(base, groupsArea, summaryArea);
			}
		    return super.onKeyboardEvent(event);
		}
		@Override public boolean onEnvironmentEvent(EnvironmentEvent event)
		{
		    NullCheck.notNull(event, "event");
		    if (event.getType() != EnvironmentEvent.Type.REGULAR)
			return super.onEnvironmentEvent(event);
		    switch(event.getCode())
		    {
		    case ACTION:
			return onSummaryAreaAction(event);
		    case CLOSE:
			closeApp();
			return true;
		    default:
			return super.onEnvironmentEvent(event);
		    }
		}
		@Override public Action[] getAreaActions()
		{
		    return actionLists.getSummaryActions(this);
		}
	};

	viewArea = new DoctreeArea(new DefaultControlEnvironment(luwrain), new Announcement(new DefaultControlEnvironment(luwrain), (org.luwrain.controls.doctree.Strings)luwrain.i18n().getStrings(org.luwrain.controls.doctree.Strings.NAME))){
		@Override public boolean onKeyboardEvent(KeyboardEvent event)
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
			}
		    return super.onKeyboardEvent(event);
		}
		@Override public boolean onEnvironmentEvent(EnvironmentEvent event)
		{
		    NullCheck.notNull(event, "event");
		    if (event.getType() != EnvironmentEvent.Type.REGULAR)
			return super.onEnvironmentEvent(event);
		    switch(event.getCode())
		    {
		    case OK:
			return actions.onOpenUrl(this);
		    case CLOSE:
			closeApp();
			return true;
		    default:
			return super.onEnvironmentEvent(event);
		    }
		}
		@Override public String getAreaName()
		{
		    return strings.viewAreaName();
		}
	    };
    }

    private boolean onGroupsAreaAction(EnvironmentEvent event)
    {
	NullCheck.notNull(event, "event");
			if (ActionEvent.isAction(event, "fetch"))
				  return actions.launchNewsFetch();
			if (ActionEvent.isAction(event, "mark-all-as-read"))
			    return actions.markAsReadWholeGroup(base, groupsArea, summaryArea, (NewsGroupWrapper)groupsArea.selected());
			if (ActionEvent.isAction(event, "show-with-read-only"))
			    return actions.setShowAllGroupsMode(base, groupsArea, true);
			if (ActionEvent.isAction(event, "hide-with-read-only"))
			    return actions.setShowAllGroupsMode(base, groupsArea, false);
			return false;
 }

    private boolean onSummaryAreaAction(EnvironmentEvent event)
    {
	NullCheck.notNull(event, "event");
	return false;
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
	return new AreaLayout(AreaLayout.LEFT_TOP_BOTTOM, groupsArea, summaryArea, viewArea);
    }
}
