
package org.luwrain.app.news;

import org.luwrain.core.*;
import org.luwrain.pim.news.*;

class Actions
{
    private final Luwrain luwrain;

    Actions(Luwrain luwrain)
    {
	NullCheck.notNull(luwrain, "luwrain");
	this.luwrain = luwrain;
    }
}
