

package org.luwrain.app.news;

import java.util.*;

class DateUtils
{
    private Date now = new Date();

    public String passedTime(Date moment)
    {
	if (moment == null)
	    throw new NullPointerException("moment may not be null");
	final long wasTime = moment.getTime();
	final long nowTime = now.getTime();
	final long passed = nowTime - wasTime;
	final long secondsTotal = passed / 1000;
	final long minutesTotal = secondsTotal / 60;
	final long seconds = secondsTotal - (minutesTotal * 60);
	final long hoursTotal = minutesTotal / 60;
	final long minutes = minutesTotal - (hoursTotal * 60);
	final long daysTotal = hoursTotal / 24;
	final long hours = hoursTotal - (daysTotal * 24);

	String res = "" + seconds + "сек.";
	if (minutes > 0)
	    res = "" + minutes + "мин. " + res;
	if (hours > 0)
	    res = "" + hours + "ч " + res;
	if (daysTotal > 0)
	    res = "" + daysTotal + "д. " + res;
	return res;
    }

    public static String dateTime(Date moment)
    {
	if (moment == null)
	    throw new NullPointerException("moment may not be null");
	String res = addZeroes(moment.getHours(), 2) + ":" + addZeroes(moment.getMinutes(), 2);
	res += ", ";
	res += moment.getDate() + " " + month(moment.getMonth() + 1) + " " + (moment.getYear() + 1900) + "г.";
	return res;
    }

    private static String month(int m)
    {
	switch (m)
	{
	case 1:
	    return "января";
	case 2:
	    return "февраля";
	case 3:
	    return "марта";
	case 4:
	    return "апреля";
	case 5:
	    return "мая";
	case 6:
	    return "июня";
	case 7:
	    	    return "июля";
	case 8:
	    return "августа";
	case 9:
	    return "сентября";
	case 10:
	    return "октября";
	case 11:
	    return "ноября";
	case 12:
	    return "декабря";
	default:
	    return "";
	    	}
    }

    private static String  addZeroes(int num, int z)
    {
	String res = "" + num;
	while (res.length() < z)
	    res = "0" + res;
	return res;
    }
}
