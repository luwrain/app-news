/*
   Copyright 2012-2016 Michael Pozhidaev <michael.pozhidaev@gmail.com>

   This file is part of the LUWRAIN.

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

import org.luwrain.core.*;
import org.luwrain.core.events.*;
import org.luwrain.controls.*;
import org.luwrain.pim.news.*;
import org.luwrain.pim.*;

class NewsApp implements Application, MonoApp, Actions
{
    static private final String STRINGS_NAME = "luwrain.news";

    private Luwrain luwrain;
    private Strings strings;

    private final Base base = new Base();
    private ListArea groupsArea;
    private ListArea summaryArea;
    private ViewArea viewArea;

    @Override public boolean onLaunch(Luwrain luwrain)
    {
	NullCheck.notNull(luwrain, "luwrain");
	final Object o = luwrain.i18n().getStrings(STRINGS_NAME);
	if (o == null || !(o instanceof Strings))
	    return false;
	strings = (Strings)o;
	this.luwrain = luwrain;
	if (!base.init(luwrain, strings))
	    return false;
	createAreas();
	return true;
    }

    private void createAreas()
    {
	final Actions actions = this;

	final ListArea.Params groupsParams = new ListArea.Params();
	groupsParams.environment = new DefaultControlEnvironment(luwrain);
	groupsParams.model = base.getGroupsModel();
	groupsParams.appearance = new DefaultListItemAppearance(groupsParams.environment);
	groupsParams.clickHandler = (area, index, obj)->openGroup(obj);
	groupsParams.name = strings.groupsAreaName();
	groupsParams.flags = ListArea.Params.loadRegularFlags(luwrain.getRegistry());

	groupsArea = new ListArea(groupsParams) {
		      @Override public boolean onKeyboardEvent(KeyboardEvent event)
		      {
			  NullCheck.notNull(event, "event");
			  if (event.isSpecial() && !event.isModified())
			      switch(event.getSpecial())
			      {
			      case TAB:
gotoSummary();
			    return true;
			      default:
				  return super.onKeyboardEvent(event);
			      }
			  if (!event.isSpecial() && !event.isModified())
			      switch(event.getChar())
			      {
			      case '=':
setShowAllGroupsMode(true);
				  return true;
			      case '-':
setShowAllGroupsMode(false);
				  return true;
			      }
		    return super.onKeyboardEvent(event);
		}
		@Override public boolean onEnvironmentEvent(EnvironmentEvent event)
		{
		    NullCheck.notNull(event, "event");
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
			  return getGroupsAreaActions();
		      }
	    };

	final ListArea.Params summaryParams = new ListArea.Params();
	summaryParams.environment = new DefaultControlEnvironment(luwrain);
	summaryParams.model = base.getSummaryModel();
	summaryParams.appearance = new SummaryAppearance(luwrain, strings);
	summaryParams.clickHandler = (area, index, obj)->showArticle(obj);
	summaryParams.name = strings.summaryAreaName();
	summaryParams.flags = ListArea.Params.loadRegularFlags(luwrain.getRegistry());

	summaryArea = new ListArea(summaryParams) {
		@Override public boolean onKeyboardEvent(KeyboardEvent event)
		{
		    if (event.isSpecial() && !event.isModified())
			switch(event.getSpecial())
			{
			case TAB:
			    gotoView();
			    return true;
			case BACKSPACE:
			    gotoGroups();
			    return true;
			case F9:
			    launchNewsFetch();
			    return true;
			}
		    if (!event.isSpecial() && !event.isModified())
			switch (event.getChar())
			{
			case ' ':
			    return onSummarySpace();
			}
		    return super.onKeyboardEvent(event);
		}
		@Override public boolean onEnvironmentEvent(EnvironmentEvent event)
		{
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
		    return getSummaryAreaActions();
		}
	};

	viewArea = new ViewArea(luwrain, this, strings);
    }

    private Action[] getGroupsAreaActions()
    {
	if (groupsArea.selectedIndex() < 0)
			  return new Action[]{
			      new Action("fetch", strings.actionFetch(), new KeyboardEvent(KeyboardEvent.Special.F9)),
			  };
			  return new Action[]{
			      new Action("fetch", strings.actionFetch(), new KeyboardEvent(KeyboardEvent.Special.F9)),
			      new Action("mark-all-as-read", strings.actionMarkAllAsRead(), new KeyboardEvent(KeyboardEvent.Special.DELETE)),
			      new Action("show-with-read-only", strings.actionShowWithReadOnly(), new KeyboardEvent('=')),
			      new Action("hide-with-read-only", strings.actionHideWithReadOnly(), new KeyboardEvent('-')),
			  };
    }

    private boolean onGroupsAreaAction(EnvironmentEvent event)
    {
	NullCheck.notNull(event, "event");
			if (ActionEvent.isAction(event, "fetch"))
				  return launchNewsFetch();
			if (ActionEvent.isAction(event, "mark-all-as-read"))
			    return markAsReadWholeGroup((NewsGroupWrapper)groupsArea.selected());
			if (ActionEvent.isAction(event, "show-with-read-only"))
				  return setShowAllGroupsMode(true);
			if (ActionEvent.isAction(event, "hide-with-read-only"))
				  return setShowAllGroupsMode(false);
			return false;
 }

    private Action[] getSummaryAreaActions()
    {
	if (summaryArea.selectedIndex() < 0)
	    return new Action[]{
			      new Action("fetch", strings.actionFetch(), new KeyboardEvent(KeyboardEvent.Special.F9)),
			  };

	    return new Action[]{
			      new Action("fetch", strings.actionFetch(), new KeyboardEvent(KeyboardEvent.Special.F9)),
			      new Action("read-article", strings.actionReadArticle()),
			      new Action("mark-article", strings.actionMarkArticle()),
			      new Action("unmark-article", strings.actionUnmarkArticle()),

			  };



    }

    private boolean onSummaryAreaAction(EnvironmentEvent event)
    {
	NullCheck.notNull(event, "event");
	return false;
 }

private boolean launchNewsFetch()
    {
	luwrain.launchApp("fetch", new String[]{"--NEWS"});
	return true;
    }

    private boolean showArticle(Object obj)
    {
	NullCheck.notNull(obj, "obj");
	if (!(obj instanceof StoredNewsArticle))
	    return false;
	final StoredNewsArticle article = (StoredNewsArticle)obj;
	base.markAsRead(article);
	luwrain.onAreaNewContent(summaryArea);
	viewArea.show(article);
	luwrain.setActiveArea(viewArea);
	return true;
    }

private boolean setShowAllGroupsMode(boolean value)
    {
	base.getGroupsModel().setShowAllMode(value);
	groupsArea.refresh();
	return true;
    }

    private boolean openGroup(Object obj)
    {
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
	gotoSummary();
	return true;
    }

    private boolean markAsReadWholeGroup(NewsGroupWrapper group)
    {
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

    private boolean onSummarySpace()
    {
	final Object obj = summaryArea.selected();
	if (obj == null || !(obj instanceof StoredNewsArticle))
	    return false;
	if (!base.markAsRead((StoredNewsArticle)obj))
	    return false;
	luwrain.onAreaNewContent(summaryArea);
	final int index = summaryArea.selectedIndex();
	if (index + 1 >= base.getSummaryModel().getItemCount())
	{
	    summaryArea.selectEmptyLine();
	    luwrain.message(strings.noMoreUnreadInGroup(), Luwrain.MESSAGE_NOT_READY);
	} else
	    summaryArea.select(index + 1, true);
	return true;
    }

    @Override public MonoApp.Result onMonoAppSecondInstance(Application app)
    {
	NullCheck.notNull(app, "app");
	return MonoApp.Result.BRING_FOREGROUND;
    }

    @Override public AreaLayout getAreasToShow()
    {
	return new AreaLayout(AreaLayout.LEFT_TOP_BOTTOM, groupsArea, summaryArea, viewArea);
    }

    @Override public void closeApp()
    {
	luwrain.closeApp();
    }

    @Override public void gotoGroups()
    {
	luwrain.setActiveArea(groupsArea);
    }

    @Override public void gotoSummary()
    {
	luwrain.setActiveArea(summaryArea);
    }

    @Override public void gotoView()
    {
	luwrain.setActiveArea(viewArea);
    }

    @Override public String getAppName()
    {
	return strings.appName();
    }
}
