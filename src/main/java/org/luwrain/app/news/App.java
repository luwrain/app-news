/*
   Copyright 2012-2024 Michael Pozhidaev <msp@luwrain.org>

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

import java.util.*;

import org.luwrain.core.*;
import org.luwrain.core.events.*;
import org.luwrain.pim.*;
import org.luwrain.pim.news.*;
import org.luwrain.app.base.*;
import org.luwrain.core.annotations.*;

@AppNoArgs(name = "news", i18n = { "en=News", "ru=Новости" })
public final class App extends AppBase<Strings> implements MonoApp
{
    private NewsStoring storing = null;
    private MainLayout mainLayout = null;
    private Conv conv = null;
    private NewsGroup group = null;
    boolean showAllGroups = false;

    final List<GroupWrapper> groups = new ArrayList<>();
    final List<NewsArticle> articles = new ArrayList<>();

    public App() { super(Strings.NAME, Strings.class, "luwrain.news"); }

    @Override public AreaLayout onAppInit() throws Exception
    {
	this.storing = org.luwrain.pim.Connections.getNewsStoring(getLuwrain(), true);
	if (storing == null)
	    throw new Exception("No news storing");
		this.conv = new Conv(this);
	this.mainLayout = new MainLayout(this );
	setAppName(getStrings().appName());
	loadGroups();
	runFetching();
	return this.mainLayout.getAreaLayout();
    }

    boolean openGroup(NewsGroup newGroup)
    {
	NullCheck.notNull(newGroup, "newGroup");
	this.group = newGroup;
	loadArticles();
	return true;
    }

    void loadGroups()
    {
	final List<GroupWrapper> w = new ArrayList<>();
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

    @Override public boolean onEscape()
    {
	closeApp();
	return true;
    }

    @Override public boolean onSystemEvent(Area area, SystemEvent event)
    {
	NullCheck.notNull(area, "area");
	NullCheck.notNull(event, "event");
	if (event.getType() != SystemEvent.Type.REGULAR)
	    return super.onSystemEvent(area, event);
	switch(event.getCode())
	{
	case REFRESH:
	    runFetching();
	    return true;
	}
	return super.onSystemEvent(area, event);
    }

    void runFetching()
    {
	getLuwrain().runWorker(org.luwrain.pim.workers.News.NAME);
    }

    @Override public MonoApp.Result onMonoAppSecondInstance(Application app)
    {
	NullCheck.notNull(app, "app");
	runFetching();
	return MonoApp.Result.BRING_FOREGROUND;
    }

    Conv getConv() { return this.conv; }
    NewsStoring getStoring() { return this.storing; }
}
