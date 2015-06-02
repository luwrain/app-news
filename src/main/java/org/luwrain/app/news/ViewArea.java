/*
   Copyright 2012-2015 Michael Pozhidaev <msp@altlinux.org>

   This file is part of the Luwrain.

   Luwrain is free software; you can redistribute it and/or
   modify it under the terms of the GNU General Public
   License as published by the Free Software Foundation; either
   version 3 of the License, or (at your option) any later version.

   Luwrain is distributed in the hope that it will be useful,
   but WITHOUT ANY WARRANTY; without even the implied warranty of
   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
   General Public License for more details.
*/

package org.luwrain.app.news;

import java.util.*;

import org.luwrain.core.*;
import org.luwrain.core.events.*;
import org.luwrain.controls.*;
import org.luwrain.extensions.pim.*;
import org.luwrain.util.MlTagStrip;

class ViewArea extends NavigateArea
{
    private static final int MAX_LINE_LENGTH = 60;
    private static final int EXTRA_LINES_COUNT = 7;

    private Luwrain luwrain;
    private Strings strings;
    private Actions actions;

    private StoredNewsArticle article;
    private String[] text;

    public ViewArea(Luwrain luwrain,
		    Actions actions,
		    Strings strings)
    {
	super(new DefaultControlEnvironment(luwrain));
	this.luwrain = luwrain;
	this.actions =  actions;
	this.strings = strings;
	if (luwrain == null)
	    throw new NullPointerException("luwrain may not be null");
	if (strings == null)
	    throw new NullPointerException("strings may not be null");
	if (actions == null)
	    throw new NullPointerException("actions may not be null");
    }

    public void show(StoredNewsArticle article)
    {
	this.article = article;
	prepareText();
	setHotPoint(0, 0);
	luwrain.onAreaNewContent(this);
	luwrain.onAreaNewHotPoint(this);//Maybe needless;
    }

    @Override public int getLineCount()
    {
	if (article == null)
	    return 1;
	if (text == null)
	    return EXTRA_LINES_COUNT;
	return text.length + EXTRA_LINES_COUNT;
    }

    @Override public String getLine(int index)
    {
	if (article == null)
	    return "";
	if (text != null && index < text.length)
	    return text[index];
	return extraLine(index - (text != null?text.length:0));
    }

    @Override public boolean onKeyboardEvent(KeyboardEvent event)
    {
	if (event == null)
	    throw new NullPointerException("event may not be null");
	if (event.isCommand() && !event.isModified())
	    switch (event.getCommand())
	    {
	    case KeyboardEvent.TAB:
		actions.gotoGroups();
		return true;
	    case KeyboardEvent.BACKSPACE:
		actions.gotoSummary();
		return true;
	    }
	return super.onKeyboardEvent(event);
    }

    @Override public boolean onEnvironmentEvent(EnvironmentEvent event)
    {
	if (event == null)
	    throw new NullPointerException("event may not be null");
	switch(event.getCode())
	{
	case EnvironmentEvent.CLOSE:
	    actions.close();
	    return true;
	default:
	    return super.onEnvironmentEvent(event);
	}
    }

    @Override public String getAreaName()
    {
	return strings.viewAreaName(); 
    }

    private void prepareText()
    {
	if (article == null)
	{
	    text = null;
	    return;
	}
	final String parsed = MlTagStrip.run(article.getContent());
	Vector<String> lines = new Vector<String>();
	String line = "";
	for(int i = 0;i < parsed.length();++i)
	{
	    final char c = parsed.charAt(i);
	    if (c == '\n')
	    {
		if (line.trim().isEmpty())
		    continue;
		lines.add(line.trim());
		lines.add("");
		line = "";
		continue;
	    }
	    if (Character.isSpace(c))
	    {
		if (line.trim().length() > MAX_LINE_LENGTH)
		{
		    lines.add(line.trim());
		    line = "";
		    continue;
		}
		if (!line.trim().isEmpty())
		    line += ' ';
		continue;
	    }
	    line += c;
	}
	if (!line.trim().isEmpty())
	    lines.add(line.trim());
	text = lines.toArray(new String[lines.size()]);
    }

    private String extraLine(int index)
    {
	DateUtils dateUtils = new DateUtils();
	switch(index)
	{
	case 1:
	    return "Заголовок: " + article.getTitle();
	case 2:
	    return "Ссылка: " + article.getUrl();
	case 3:
	    return "Источник: " + article.getSourceTitle();

	case 4:
	    return "Автор: " + article.getAuthor();
	case 5:
	    return "Время публикации: " + dateUtils.passedTime(article.getPublishedDate()) + " назад (" + dateUtils.dateTime(article.getPublishedDate()) + ")";
	default:
	    return "";
	}
    }

}
