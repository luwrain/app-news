
package org.luwrain.app.news2;

import java.net.*;

import org.luwrain.core.*;
import org.luwrain.popups.*;

import org.luwrain.app.news.Strings;

final class Conversations
{
    private final Luwrain luwrain;
    private final Strings strings;

    Conversations(App app)
    {
	NullCheck.notNull(app, "app");
	this.luwrain = app.getLuwrain();
	this.strings = app.getStrings();
    }

    boolean confirmGroupDeleting(GroupWrapper wrapper)
    {
	NullCheck.notNull(wrapper, "wrapper");
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
