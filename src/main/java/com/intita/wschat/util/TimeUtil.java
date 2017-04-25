package com.intita.wschat.util;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * Created by roma on 25.04.17.
 */
public class TimeUtil {
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
}
