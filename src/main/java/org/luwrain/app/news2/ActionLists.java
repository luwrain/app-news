
package org.luwrain.app.news2;

import org.luwrain.core.*;
import org.luwrain.core.events.*;
import org.luwrain.controls.*;

final class ActionLists
{
    private final Strings strings;

    ActionLists(Strings strings)
    {
	NullCheck.notNull(strings, "strings");
	this.strings = strings;
    }

    Action[] getGroupsActions(ListArea groupsArea)
    {
	if (groupsArea.selectedIndex() < 0)
	    return new Action[]{
		new Action("add-group", strings.actionAddGroup(), new InputEvent(InputEvent.Special.INSERT)),
		new Action("show-with-read-only", strings.actionShowWithReadOnly(), new InputEvent('=')),
		new Action("hide-with-read-only", strings.actionHideWithReadOnly(), new InputEvent('-')),
		new Action("fetch", strings.actionFetch(), new InputEvent(InputEvent.Special.F9)),
	    };
	return new Action[]{
	    new Action("add-group", strings.actionAddGroup(), new InputEvent(InputEvent.Special.INSERT)),
	    new Action("delete-group", strings.actionDeleteGroup(), new InputEvent(InputEvent.Special.DELETE)),
	    new Action("mark-all-as-read", strings.actionMarkAllAsRead()/*, new InputEvent(InputEvent.Special.DELETE)*/), //FIXME: shift+delete
	    new Action("show-with-read-only", strings.actionShowWithReadOnly(), new InputEvent('=')),
	    new Action("hide-with-read-only", strings.actionHideWithReadOnly(), new InputEvent('-')),
	    new Action("fetch", strings.actionFetch(), new InputEvent(InputEvent.Special.F9)),
	};
    }

Action[] getSummaryActions(ListArea summaryArea)
    {
	if (summaryArea.selectedIndex() < 0)
	    return new Action[]{
			      new Action("fetch", strings.actionFetch(), new InputEvent(InputEvent.Special.F9)),
			  };
	    return new Action[]{
			      new Action("fetch", strings.actionFetch(), new InputEvent(InputEvent.Special.F9)),
			      new Action("read-article", strings.actionReadArticle()),
			      new Action("mark-article", strings.actionMarkArticle()),
			      new Action("unmark-article", strings.actionUnmarkArticle()),
			  };
    }


}
