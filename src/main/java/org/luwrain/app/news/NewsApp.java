//import java.util.*;

package org.luwrain.app.news;

import org.luwrain.core.*;
import org.luwrain.core.events.*;
import org.luwrain.controls.*;
import org.luwrain.pim.news.*;

class NewsApp implements Application, Actions
{
    static private final String STRINGS_NAME = "luwrain.news";

    private Luwrain luwrain;
    private Strings strings;

    private NewsStoring newsStoring;
    private GroupsModel groupsModel;
    private SummaryModel summaryModel;
    private SummaryAppearance summaryAppearance;
    private ListArea groupsArea;
    private ListArea summaryArea;
    private ViewArea viewArea;

    @Override public boolean onLaunch(Luwrain luwrain)
    {
	final Object o = luwrain.i18n().getStrings(STRINGS_NAME);
	if (o == null || !(o instanceof Strings))
	    return false;
	strings = (Strings)o;
	this.luwrain = luwrain;
	final Object f =  luwrain.getSharedObject("luwrain.pim.news");
	if (f == null || !(f instanceof org.luwrain.pim.news.Factory))
	    return false;
	final org.luwrain.pim.news.Factory factory = (org.luwrain.pim.news.Factory)f;
	newsStoring = factory.createNewsStoring();
	if (newsStoring == null)
	    return false;
	createModels();
	createAreas();
	return true;
    }

    private void createModels()
    {
	groupsModel = new GroupsModel(newsStoring); 
	summaryModel = new SummaryModel(newsStoring);
	summaryAppearance = new SummaryAppearance(luwrain, strings);
    }

    private void createAreas()
    {
	final Actions a = this;
	final Strings s = strings;

	final ListClickHandler groupsHandler = new ListClickHandler(){
		private Actions actions = a;
		@Override public boolean onListClick(ListArea area,
						     int index,
						     Object item)
		{
		    if (index < 0 || item == null || !(item instanceof NewsGroupWrapper))
			return false;
		    actions.openGroup((NewsGroupWrapper)item);
		    return true;
		}
	    };

	final ListClickHandler summaryHandler = new ListClickHandler(){
		private Actions actions = a;
		@Override public boolean onListClick(ListArea area,
						     int index,
						     Object item)
		{
		    if (index < 0 || item == null || !(item instanceof StoredNewsArticle))
			return false;
		    actions.showArticle((StoredNewsArticle)item);
		    return true;
		}
	    };

	      groupsArea = new ListArea(new DefaultControlEnvironment(luwrain),
					groupsModel,
					new DefaultListItemAppearance(new DefaultControlEnvironment(luwrain)),
					groupsHandler,
					strings.groupsAreaName()) {
		      private Strings strings = s;
		      private Actions actions = a;
		      @Override public boolean onKeyboardEvent(KeyboardEvent event)
		      {
			  if (event.isCommand() && !event.isModified())
			      switch(event.getCommand())
			      {
			      case KeyboardEvent.TAB:
			    actions.gotoSummary();
			    return true;
			case KeyboardEvent.DELETE:
			    if (selected() != null && (selected() instanceof NewsGroupWrapper))
				actions.markAsReadWholeGroup((NewsGroupWrapper)selected()); else
				return false;
			    return true;
			      default:
				  return super.onKeyboardEvent(event);
			      }
			  if (!event.isCommand() && !event.isModified())
			      switch(event.getCharacter())
			      {
			      case '=':
				  actions.setShowAllGroupsMode(true);
				  return true;
			      case '-':
				  actions.setShowAllGroupsMode(false);
				  return true;
			      default:
		    return super.onKeyboardEvent(event);
			      }
		    return super.onKeyboardEvent(event);
		}
		@Override public boolean onEnvironmentEvent(EnvironmentEvent event)
		{
		    switch(event.getCode())
		    {
		    case EnvironmentEvent.CLOSE:
			actions.closeApp();
			return true;
		    default:
			return super.onEnvironmentEvent(event);
		    }
		}
	    };

	summaryArea = new ListArea(new DefaultControlEnvironment(luwrain),
				   summaryModel,
				   new SummaryAppearance(luwrain, strings),
				   summaryHandler,
				   strings.summaryAreaName()) {
		private Strings strings = s;
		private Actions actions = a;
		@Override public boolean onKeyboardEvent(KeyboardEvent event)
		{
		    if (event.isCommand() && !event.isModified())
			switch(event.getCommand())
			{
			case KeyboardEvent.TAB:
			    actions.gotoView();
			    return true;
			case KeyboardEvent.BACKSPACE:
			    actions.gotoGroups();
			    return true;
			}
		    if (!event.isCommand() && !event.isModified())
			switch (event.getCharacter())
			{
			case ' ':
			    return onSpace();
			}
		    return super.onKeyboardEvent(event);
		}
		@Override public boolean onEnvironmentEvent(EnvironmentEvent event)
		{
		    switch(event.getCode())
		    {
		    case EnvironmentEvent.CLOSE:
			actions.closeApp();
			return true;
		    default:
			return super.onEnvironmentEvent(event);
		    }
		}
		private boolean onSpace()
		{
		    final Object obj = selected();
		    if (obj == null || !(obj instanceof StoredNewsArticle))
			return false;
		    StoredNewsArticle article = (StoredNewsArticle)obj;
		    try {
			if (article.getState() == NewsArticle.NEW)
			    article.setState(NewsArticle.READ);
		    }
		    catch (Exception e)
		    {
			e.printStackTrace();
			return false;
		    }
		    luwrain.onAreaNewContent(this);
		    actions.refreshGroups();
		    final int index = selectedIndex();
		    if (index + 1 < model().getItemCount())
			setSelectedByIndex(index + 1, true); else
		    {
			selectEmptyLine();
			luwrain.hint(strings.noMoreUnreadInGroup());
		    }
		    return true;
		}
	};

	viewArea = new ViewArea(luwrain, this, strings);
    }

    @Override public void showArticle(StoredNewsArticle article)
    {
	NullCheck.notNull(article, "article");
	try {
	    if (article.getState() == NewsArticle.NEW)
		article.setState(NewsArticle.READ);
	    luwrain.onAreaNewContent(summaryArea);
	}
	catch (Exception e)
	{
	    e.printStackTrace(); 
	}
	viewArea.show(article);
	luwrain.setActiveArea(viewArea);
    }

    @Override public void setShowAllGroupsMode(boolean value)
    {
	groupsModel.setShowAllMode(value);
	groupsArea.refresh();
    }

    @Override public void refreshGroups()
    {
	groupsArea.refresh();
    }

    @Override public void openGroup(NewsGroupWrapper group)
    {
	if (group == null)
	    throw new NullPointerException("group may not be null");
	if (group.getStoredGroup() != summaryModel.getGroup())
	{
	    summaryModel.setGroup(group.getStoredGroup());
	    summaryArea.refresh(); 
	    summaryArea.resetHotPoint();
	    //FIXME:reset view;
	}
	gotoSummary();
    }

    @Override public void markAsReadWholeGroup(NewsGroupWrapper group)
    {
	if (group == null)
	    throw new NullPointerException("group may not be null");
	StoredNewsArticle articles[];
	try {
	    articles = newsStoring.loadNewsArticlesInGroupWithoutRead(group.getStoredGroup());
	    if (articles == null || articles.length < 1)
		return;
	    for(StoredNewsArticle a: articles)
		if (a.getState() == NewsArticle.NEW)
		    a.setState(NewsArticle.READ);
	}
	catch (Exception e)
	{
	    e.printStackTrace();
	    luwrain.message(strings.errorReadingArticles(), Luwrain.MESSAGE_ERROR);
	    summaryModel.setGroup(null);
	    summaryArea.refresh();
	    return;
	}
	groupsArea.refresh();
	groupsArea.introduceSelected();
	summaryModel.setGroup(null);
	summaryArea.refresh();
    }

    @Override public AreaLayout getAreasToShow()
    {
	return new AreaLayout(AreaLayout.LEFT_TOP_BOTTOM, groupsArea, summaryArea, viewArea);
    }

    @Override public void closeApp()
    {
	luwrain.closeApp();
    }

    @Override public void gotoGroups()
    {
	luwrain.setActiveArea(groupsArea);
    }

    @Override public void gotoSummary()
    {
	luwrain.setActiveArea(summaryArea);
    }

    @Override public void gotoView()
    {
	luwrain.setActiveArea(viewArea);
    }

    @Override public String getAppName()
    {
	return strings.appName();
    }
}
