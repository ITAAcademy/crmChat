package com.intita.wschat.util;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Created by roma on 25.04.17.
 */
public class TimeUtil {

    public static Date removeTime(Date date) {
        Long dateWithoutTime = removeTime(date.getTime());
        return new Date(dateWithoutTime);
    }

    public static Long removeTime(Long date){
        Calendar cal = Calendar.getInstance(); // locale-specific
        cal.setTime(new Date(date));
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        long time = cal.getTimeInMillis();
        return time;
    }
    public static List<Long> removeTimeFromList(List<Long> source){
        List<Long> processedList = new ArrayList<Long>();
        for (Long date : source){
            processedList.add(removeTime(date));
        }
        return processedList;
    }

    public static Date getCurrentDay(){
        Calendar cal = Calendar.getInstance(); // locale-specific
        cal.setTime(new Date());
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        return cal.getTime();
    }

    public static int getDifferenceDays(Date d1,Date d2) {
        long diff = d2.getTime() - d1.getTime();
        return (int)TimeUnit.DAYS.convert(diff,TimeUnit.MILLISECONDS);
    }

    public static Date getDateWithNextDay(Date date) {
        Calendar c = Calendar.getInstance();
        c.setTime(date);
        c.add(Calendar.DATE,1);
        return c.getTime();
    }

}
