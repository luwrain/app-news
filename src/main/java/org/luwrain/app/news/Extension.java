
package org.luwrain.app.news;

import org.luwrain.core.*;

public class Extension extends org.luwrain.core.extensions.EmptyExtension
{
    @Override public Command[] getCommands(Luwrain luwrain)
    {
	return new Command[]{
	    new Command(){
		@Override public String getName()
		{
		    return "news";
		}
		@Override public void onCommand(Luwrain luwrain)
		{
		    luwrain.launchApp("news");
		}
	    }};
    }

    @Override public Shortcut[] getShortcuts(Luwrain luwrain)
    {
	return new Shortcut[]{
	    new Shortcut() {
		@Override public String getName()
		{
		    return "news";
		}
		@Override public Application[] prepareApp(String[] args)
		{
		    Application[] res = new Application[1];
		    res[0] = new NewsApp();
		    return res;
		}
	    }};
    }
}
