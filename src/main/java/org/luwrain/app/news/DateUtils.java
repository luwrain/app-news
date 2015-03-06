

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
}
