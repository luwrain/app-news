/*
   Copyright 2012-2018 Michael Pozhidaev <michael.pozhidaev@gmail.com>

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

public interface Strings
{
    static final String NAME = "luwrain.news";

    String actionAddGroup();
    String actionDeleteGroup();
    String actionFetch();
    String actionHideWithReadOnly();
    String actionMarkAllAsRead();
    String actionMarkArticle();
    String actionReadArticle();
    String actionShowWithReadOnly();
    String actionUnmarkArticle();
    String appName();
    String articleTitle(String title);
    String articleUrl(String url);
    String groupAddingNameMayNotBeEmpty();
    String groupAddingPopupName();
    String groupAddingPopupPrefix();
    String groupDeletingPopupName();
    String groupDeletingPopupText(String groupName);
    String groupPropertiesAreaName(String groupArea);
    String groupPropertiesInvalidOrderIndex();
    String groupPropertiesName();
    String groupPropertiesNameMayNotBeEmpty();
    String groupPropertiesOrderIndex();
    String groupPropertiesUrls();
    String groupsAreaName();
    String markedPrefix();
    String readPrefix();
    String summaryAreaName();
    String viewAreaName();
}
