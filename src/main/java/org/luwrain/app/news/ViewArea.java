//import java.util.*;

package org.luwrain.app.news;

import java.util.*;

import org.luwrain.core.*;
import org.luwrain.core.events.*;
import org.luwrain.controls.*;
import org.luwrain.pim.news.*;
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
	NullCheck.notNull(event, "event");
	if (event.isCommand() && !event.isModified())
	    switch (event.getCommand())
	    {
	    case KeyboardEvent.ENTER:
		return openUrl();
	    case KeyboardEvent.F9:
		actions.launchNewsFetch();
		return true;
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
	    actions.closeApp();
	    return true;
	default:
	    return super.onEnvironmentEvent(event);
	}
    }

    @Override public String getAreaName()
    {
	return strings.viewAreaName(); 
    }

    private boolean openUrl ()
    {
	if (article == null)
	    return false;
	final String url = article.getUrl ();
	if (url == null || url.trim().isEmpty())
	    return false;
	luwrain.launchApp("reader", new String[]{"--URL", url});
	return true;
    }

    private void prepareText()
    {
	if (article == null)
	{
	    text = null;
	    return;
	}
	    text = MlTagStrip.run(article.getContent()).split("\n");
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
