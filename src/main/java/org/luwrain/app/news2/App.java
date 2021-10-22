
package org.luwrain.app.news2;

import java.util.*;

import org.luwrain.core.*;
//import org.luwrain.core.events.*;
import org.luwrain.pim.*;
import org.luwrain.pim.news.*;
import org.luwrain.app.base.*;

import org.luwrain.app.news.Strings;

public final class App extends AppBase<Strings> implements MonoApp
{
    private NewsStoring storing = null;
    private MainLayout mainLayout = null;
    private NewsGroup group = null;
    private boolean showAllGroups = false;

    final List<GroupWrapper> groups = new ArrayList<>();
    final List<NewsArticle> articles = new ArrayList<>();

    public App() { super(Strings.NAME, Strings.class, "luwrain.news"); }

    @Override public AreaLayout onAppInit()
    {
	this.storing = org.luwrain.pim.Connections.getNewsStoring(getLuwrain(), true);
	if (storing == null)
	    return null;
	loadGroups();
	return this.mainLayout.getAreaLayout();
    }

    boolean openGroup(NewsGroup newGroup)
    {
	NullCheck.notNull(newGroup, "newGroup");
	if (this.group != null && this.group == newGroup)
	    return false;
	this.group = newGroup;
	loadArticles();
	return true;
    }

    void loadGroups()
    {
	try {
	    final List<GroupWrapper> w = new LinkedList();
	    final NewsGroup[] g = storing.getGroups().load();
	    Arrays.sort(g);
	    int[] newCounts = storing.getArticles().countNewInGroups(g);
	    int[] markedCounts = storing.getArticles().countMarkedInGroups(g);
	    for(int i = 0;i < g.length;++i)
	    {
		final int newCount = i < newCounts.length?newCounts[i]:0;
		final int markedCount = i < markedCounts.length?markedCounts[i]:0;
		if (showAllGroups || newCount > 0 || markedCount > 0)
		    w.add(new GroupWrapper(g[i], newCount));
	    }
	    this.groups.clear();
	    this.groups.addAll(w);
	}
	catch(PimException e)
	{
	    this.groups.clear();
	    crash(e);
	}
    }

    void loadArticles()
    {
	if (group == null)
	{
	    this.articles.clear();
	    return;
	}
	this.articles.clear();
	this.articles.addAll(Arrays.asList(storing.getArticles().loadWithoutRead(group)));
	if (articles.isEmpty())
	    this.articles.addAll(Arrays.asList(storing.getArticles().load(group)));
	if (articles != null)
	    Collections.sort(articles);
    }

    @Override public MonoApp.Result onMonoAppSecondInstance(Application app)
    {
	NullCheck.notNull(app, "app");
	return MonoApp.Result.BRING_FOREGROUND;
    }
}
