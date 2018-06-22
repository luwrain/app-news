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

import org.luwrain.core.*;
import org.luwrain.core.events.*;
import org.luwrain.controls.*;
import org.luwrain.controls.doctree.*;
import org.luwrain.pim.news.*;
import org.luwrain.pim.*;

class App implements Application, MonoApp
{
    private Luwrain luwrain = null;
    private Strings strings = null;
    private Base base = null;
    private Actions actions = null;
    private ActionLists actionLists = null;

    private ListArea groupsArea;
    private ListArea summaryArea;
    private DocumentArea viewArea;

    @Override public InitResult onLaunchApp(Luwrain luwrain)
    {
	NullCheck.notNull(luwrain, "luwrain");
	final Object o = luwrain.i18n().getStrings(Strings.NAME);
	if (o == null || !(o instanceof Strings))
	    return new InitResult(InitResult.Type.NO_STRINGS_OBJ, Strings.NAME);
	strings = (Strings)o;
	this.luwrain = luwrain;
	this.base = new Base(luwrain, strings);
	if (base.storing == null)
	    return new InitResult(InitResult.Type.FAILURE);
	this.actions = new Actions(luwrain, strings, base);
	this.actionLists = new ActionLists(strings);
	createAreas();
	luwrain.runWorker(org.luwrain.pim.workers.News.NAME);
	return new InitResult();
    }

    private void createAreas()
    {
	final ListArea.Params groupsParams = new ListArea.Params();
	groupsParams.context = new DefaultControlEnvironment(luwrain);
	groupsParams.model = base.newGroupsModel();
	groupsParams.appearance = new ListUtils.DefaultAppearance(groupsParams.context, Suggestions.CLICKABLE_LIST_ITEM);
	groupsParams.clickHandler = (area, index, obj)->{
	    if (!actions.onGroupsClick(summaryArea, obj))
		return false;
	    luwrain.setActiveArea(summaryArea);
	    return true;
	};
	groupsParams.name = strings.groupsAreaName();

	groupsArea = new ListArea(groupsParams) {
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
			  if (!event.isSpecial() && !event.isModified())
			      switch(event.getChar())
			      {
			      case '=':
				  return actions.setShowAllGroupsMode(groupsArea, true);
			      case '-':
				  return actions.setShowAllGroupsMode(groupsArea, false);
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
		    case ACTION:
			return onGroupsAreaAction(event);
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

	final ListArea.Params summaryParams = new ListArea.Params();
	summaryParams.context = new DefaultControlEnvironment(luwrain);
	summaryParams.model = base.newArticlesModel();
	summaryParams.appearance = new SummaryAppearance(luwrain, strings);
	summaryParams.clickHandler = (area, index, obj)->actions.onSummaryClick(summaryArea, viewArea, obj);
	summaryParams.name = strings.summaryAreaName();

	summaryArea = new ListArea(summaryParams) {
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

	viewArea = new DocumentArea(new DefaultControlEnvironment(luwrain), new Announcement(new DefaultControlEnvironment(luwrain), (org.luwrain.controls.doctree.Strings)luwrain.i18n().getStrings(org.luwrain.controls.doctree.Strings.NAME))){
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

    private boolean onGroupsAreaAction(EnvironmentEvent event)
    {
	NullCheck.notNull(event, "event");
			if (ActionEvent.isAction(event, "fetch"))
				  return actions.launchNewsFetch();
			if (ActionEvent.isAction(event, "mark-all-as-read"))
			    return actions.markAsReadWholeGroup(groupsArea, summaryArea, (NewsGroupWrapper)groupsArea.selected());
			if (ActionEvent.isAction(event, "show-with-read-only"))
			    return actions.setShowAllGroupsMode(groupsArea, true);
			if (ActionEvent.isAction(event, "hide-with-read-only"))
			    return actions.setShowAllGroupsMode(groupsArea, false);
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
