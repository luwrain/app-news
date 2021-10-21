
package org.luwrain.app.news2;

import java.net.*;

import org.luwrain.core.*;
import org.luwrain.popups.*;

final class Conversations
{
    private final Luwrain luwrain;
    private final Strings strings;

    Conversations(Luwrain luwrain, Strings strings)
    {
	NullCheck.notNull(luwrain, "luwrain");
	NullCheck.notNull(strings, "strings");
	this.luwrain = luwrain;
	this.strings = strings;
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
