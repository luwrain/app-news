
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

    private Luwrain luwrain;
    private Strings strings;

    private final Base base = new Base();
    private Actions actions = null;
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
	actions = new Actions(luwrain, strings);
	createAreas();
	return new InitResult();
    }

    private void createAreas()
    {

	final ListArea.Params groupsParams = new ListArea.Params();
	groupsParams.context = new DefaultControlEnvironment(luwrain);
	groupsParams.model = base.getGroupsModel();
	groupsParams.appearance = new ListUtils.DefaultAppearance(groupsParams.context, Suggestions.CLICKABLE_LIST_ITEM);
	groupsParams.clickHandler = (area, index, obj)->{
	    if (!actions.onGroupsClick(base, summaryArea, obj))
		return false;
	    gotoSummary();
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
gotoSummary();
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
			  return getGroupsAreaActions();
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
			    gotoView();
			    return true;
			case BACKSPACE:
			    gotoGroups();
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
		    return getSummaryAreaActions();
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
			    gotoGroups();
			    return true;
			case BACKSPACE:
			    gotoSummary();
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
				  return actions.launchNewsFetch();
			if (ActionEvent.isAction(event, "mark-all-as-read"))
			    return actions.markAsReadWholeGroup(base, groupsArea, summaryArea, (NewsGroupWrapper)groupsArea.selected());
			if (ActionEvent.isAction(event, "show-with-read-only"))
			    return actions.setShowAllGroupsMode(base, groupsArea, true);
			if (ActionEvent.isAction(event, "hide-with-read-only"))
			    return actions.setShowAllGroupsMode(base, groupsArea, false);
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


    @Override public void closeApp()
    {
	luwrain.closeApp();
    }

    private void gotoGroups()
    {
	luwrain.setActiveArea(groupsArea);
    }

    private void gotoSummary()
    {
	luwrain.setActiveArea(summaryArea);
    }

    private void gotoView()
    {
	luwrain.setActiveArea(viewArea);
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
