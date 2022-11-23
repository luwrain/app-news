/*
   Copyright 2012-2021 Michael Pozhidaev <msp@luwrain.org>

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

import java.net.*;

import org.luwrain.core.*;
import org.luwrain.popups.*;

final class Conv
{
    private final Luwrain luwrain;
    private final Strings strings;

    Conv(App app)
    {
	this.luwrain = app.getLuwrain();
	this.strings = app.getStrings();
    }

    boolean confirmGroupDeleting(GroupWrapper wrapper)
    {
	return Popups.confirmDefaultNo(luwrain, strings.groupDeletingPopupName(), strings.groupDeletingPopupText(wrapper.group.getName()));
    }

    String newGroupName()
    {
	final String res = Popups.text(luwrain, strings.groupAddingPopupName(), strings.groupAddingPopupPrefix(), "", (line)->{
		if (line.trim().isEmpty())
		{
		    luwrain.message(strings.groupAddingNameMayNotBeEmpty(), Luwrain.MessageType.ERROR);
		    return false;
		}
		return true;
	    });
	if (res == null)
	    return null;
	return res.trim();
    }
}
