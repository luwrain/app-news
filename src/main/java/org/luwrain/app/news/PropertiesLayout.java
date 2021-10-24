
package org.luwrain.app.news2;

import java.util.*;

import org.luwrain.core.*;
import org.luwrain.controls.*;
import org.luwrain.pim.news.*;
import org.luwrain.app.base.*;

final class PropertiesLayout extends LayoutBase
{
    static private final String
	NAME = "name",
	ORDER_INDEX = "order-index";

    private final App app;
    final FormArea formArea;

    PropertiesLayout(App app, NewsGroup group, ActionHandler closing)
    {
	super(app);
	NullCheck.notNull(group, "group");
	this.app = app;
	this.formArea = new FormArea(getControlContext(), app.getStrings().groupPropertiesAreaName(group.getName())) ;
	final List<String> urls = group.getUrls();
	final String[] urlLines;
	if (urls != null && !urls.isEmpty())
	{
	    final List<String> res = new ArrayList<>();
	    for(String s: urls)
		res.add(s);
	    res.add("");
	    urlLines = res.toArray(new String[res.size()]);
	} else
	    urlLines = new String[0];
	formArea.addEdit(NAME, app.getStrings().groupPropertiesName(), group.getName());
	formArea.addEdit(ORDER_INDEX, app.getStrings().groupPropertiesOrderIndex(), "" + group.getOrderIndex());
	formArea.activateMultilineEdit(app.getStrings().groupPropertiesUrls(), urlLines, true);
	setCloseHandler(closing);
	setOkHandler(()->{
		if (!save(group))
		    return true;
		return closing.onAction();
	    });
	setAreaLayout(formArea, actions());
    }

    private boolean save(NewsGroup group)
    {
	NullCheck.notNull(group, "group");
	final String name = formArea.getEnteredText(NAME);
	if (name.trim().isEmpty())
	{
	    app.message(app.getStrings().groupPropertiesNameMayNotBeEmpty(), Luwrain.MessageType.ERROR);
	    return false;
	}
	group.setName(name);
	if (formArea.getEnteredText("order-index").trim().isEmpty())
	{
	    app.message(app.getStrings().groupPropertiesInvalidOrderIndex(), Luwrain.MessageType.ERROR);
	    return false;
	}
	final int orderIndex;
	try {
	    orderIndex = Integer.parseInt(formArea.getEnteredText(ORDER_INDEX));
	}
	catch(NumberFormatException e)
	{
	    app.message(app.getStrings().groupPropertiesInvalidOrderIndex(), Luwrain.MessageType.ERROR);
	    return false;
	}
	if (orderIndex < 0)
	{
	    app.message(app.getStrings().groupPropertiesInvalidOrderIndex(), Luwrain.MessageType.ERROR);
	    return false;
	}
	group.setOrderIndex(orderIndex);
	final List<String> urls = new ArrayList<>();
	for(String s: formArea.getMultilineEditLines())
	    if (!s.trim().isEmpty())
		urls.add(s.trim());
	group.setUrls(urls);
	group.save();
	return true;
    }
}
