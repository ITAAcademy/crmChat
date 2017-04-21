package com.intita.wschat.domain;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

public class ChatUserActivityStatistic {

public ChatUserActivityStatistic(){
	
}
/**
 * create new instance from activeTime, which will be sorted inside function
 * @param activityDurationMs
 * @param activeTime
 * @return
 */
public static ChatUserActivityStatistic createFromActiveTimeAndDuration(Long userId,Long activityDurationMs,List<Long> activeTime){
	ChatUserActivityStatistic statistic = new ChatUserActivityStatistic();

	List<Long> activeTimeCopy = new ArrayList<Long>(activeTime);
	List<Long> filterdResult = new ArrayList<Long>();//redundant information filtered out
	Collections.sort(activeTimeCopy);
	for (int i = 0; i < activeTimeCopy.size(); i++){
		Long currentDate = activeTimeCopy.get(i);
		if (i==0)
		filterdResult.add(currentDate);
		else
		if(i<activeTimeCopy.size()-1){
		Long nextDate = activeTimeCopy.get(i+1);
		Long previousDate = activeTimeCopy.get(i-1);
		if (previousDate+activityDurationMs >= nextDate )continue;//skip middle time stamp because  of it redundant
		//current date is same as neighbor
		if (Long.compare(currentDate, previousDate)==0 || Long.compare(currentDate, nextDate)==0)continue;
		filterdResult.add(currentDate);
		}
	}
	statistic.userId = userId;
	statistic.activityAtTime = filterdResult;
	statistic.activityDurationMs = activityDurationMs;
	
	return statistic;
}
private Long activityDurationMs;
private Long userId;
private List<Long> activityAtTime;
public Long getUserId() {
	return userId;
}
public void addActivityStartPoint(Long date){
	activityAtTime.add(date);
}

public List<Long> getActivityAtTime() {
	return activityAtTime;
}
public void setActivityAtTime(List<Long> activityAtTime) {
	this.activityAtTime = activityAtTime;
}
public void setUserId(Long userId) {
	this.userId = userId;
}
public Long getActivityDurationMs() {
	return activityDurationMs;
}
public void setActivityDurationMs(Long activityDurationMs) {
	this.activityDurationMs = activityDurationMs;
}




}
