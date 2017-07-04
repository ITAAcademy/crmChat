package com.intita.wschat.domain.responsedata;

import java.util.Date;
import java.util.Map;

/**
 * Created by roma on 25.04.17.
 */
public class ChatUserActivityPerDayStatistic {
    public ChatUserActivityPerDayStatistic(){

    }
    public ChatUserActivityPerDayStatistic(Map<Long,Long> statistic){
        this.data = statistic;
    }

    public Map<Long, Long> getData() {
        return data;
    }

    public void setData(Map<Long, Long> data) {
        this.data = data;
    }

    private Map<Long, Long> data;
}
