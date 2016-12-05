
package org.luwrain.app.news;

import java.util.*;

import org.luwrain.core.*;
import org.luwrain.core.events.*;
import org.luwrain.controls.*;
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
	actions = new Actions(luwrain);
	createAreas();
	return true;
    }

    private void createAreas()
    {

	final ListArea.Params groupsParams = new ListArea.Params();
	groupsParams.environment = new DefaultControlEnvironment(luwrain);
	groupsParams.model = base.getGroupsModel();
	groupsParams.appearance = new DefaultListItemAppearance(groupsParams.environment);
	groupsParams.clickHandler = (area, index, obj)->openGroup(obj);
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
			      case ' ':
				  if (selected() != null)
				      return openGroup(selected());
				  return false;
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
	summaryParams.environment = new DefaultControlEnvironment(luwrain);
	summaryParams.model = base.getSummaryModel();
	summaryParams.appearance = new SummaryAppearance(luwrain, strings);
	summaryParams.clickHandler = (area, index, obj)->showArticle(obj);
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

	viewArea = new ViewArea(luwrain, strings){

		@Override public boolean onKeyboardEvent(KeyboardEvent event)
		{
		    NullCheck.notNull(event, "event");
		    if (event.isSpecial() && !event.isModified())
			switch(event.getSpecial())
			{
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
		    case CLOSE:
			closeApp();
			return true;
		    default:
			return super.onEnvironmentEvent(event);
		    }
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
	groupsArea.refresh();
	final int index = summaryArea.selectedIndex();
	if (index + 1 >= base.getSummaryModel().getItemCount())
	    luwrain.setActiveArea(groupsArea); else
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

    private void closeApp()
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
}
