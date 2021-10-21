
package org.luwrain.app.news2;

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
