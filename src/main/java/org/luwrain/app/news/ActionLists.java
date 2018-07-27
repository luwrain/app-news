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
			      new Action("fetch", strings.actionFetch(), new KeyboardEvent(KeyboardEvent.Special.F9)),
			      			      new Action("show-with-read-only", strings.actionShowWithReadOnly(), new KeyboardEvent('=')),
			      new Action("hide-with-read-only", strings.actionHideWithReadOnly(), new KeyboardEvent('-')),
			  };
			  return new Action[]{
			      new Action("fetch", strings.actionFetch(), new KeyboardEvent(KeyboardEvent.Special.F9)),
			      new Action("mark-all-as-read", strings.actionMarkAllAsRead()/*, new KeyboardEvent(KeyboardEvent.Special.DELETE)*/), //FIXME: shift+delete
			      new Action("delete-group", strings.actionDeleteGroup(), new KeyboardEvent(KeyboardEvent.Special.DELETE)),
			      new Action("show-with-read-only", strings.actionShowWithReadOnly(), new KeyboardEvent('=')),
			      new Action("hide-with-read-only", strings.actionHideWithReadOnly(), new KeyboardEvent('-')),
			  };
    }

Action[] getSummaryActions(ListArea summaryArea)
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


}
