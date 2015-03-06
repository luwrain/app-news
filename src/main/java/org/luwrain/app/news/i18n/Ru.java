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

package org.luwrain.app.news.i18n;

import java.util.*;

public class Ru implements org.luwrain.app.news.Strings
{
    @Override public String appName()
    {
	return "Новости";
    }

    @Override public String groupsAreaName()
    {
	return "Группы новостей";
    }

    @Override public String summaryAreaName()
    {
	return "Список статей";
    }

    @Override public String viewAreaName()
    {
	return "Текст статьи";
    }

    @Override public     String errorReadingArticles()
    {
	return "Произошла ошибка доставки новостных статей.";
    }

    @Override public String readPrefix()
    {
	return "прочитано";
    }

    @Override public String markedPrefix()
    {
	return "помечено";
    }

    @Override public String noSummaryItems()
    {
	return "Новостные статьи отсутствуют";
    }

    @Override public String noSummaryItemsAbove()
    {
	return "Начало списка новостных статей";
    }

    @Override public String noSummaryItemsBelow()
    {
	return "Конец списка новостных статей";
    }

    @Override public String errorUpdatingArticleState()
    {
	return "Произошла ошибка сохранения состояния новостной статьи";
    }
}
