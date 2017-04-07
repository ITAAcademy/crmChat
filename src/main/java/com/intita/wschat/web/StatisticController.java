package com.intita.wschat.web;

import java.security.Principal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.intita.wschat.domain.ChatUserActivityStatistic;
import com.intita.wschat.models.ChatUser;
import com.intita.wschat.services.ChatUsersService;
import com.intita.wschat.services.UserMessageService;

@Controller
public class StatisticController {
final Long activityDurationMs = 5000L;
@Autowired
ChatUsersService chatUserService;

@Autowired
UserMessageService userMessageService;

	@RequestMapping(value = "/statistic/user/get_week_activity/{userId}", method = RequestMethod.GET)
	@ResponseBody
	public ChatUserActivityStatistic getUserActivityMapping(@PathVariable("userId") Long userId) {
		final long MS_IN_DAY = 24 * 60 * 60 * 1000;
		final long ACTIVITY_DOORATION_MS = 5000;
		
		Date currentTime = new Date();
		Date weekAgoTime = new Date();
		weekAgoTime.setTime(currentTime.getTime()-MS_IN_DAY*24);
		List<Date> activityDates = userMessageService.getMessagesDatesByChatUserAndDate(userId,weekAgoTime,currentTime);
		List<Long> datesLong = new ArrayList<Long>();
		for (Date date : activityDates){
			datesLong.add(date.getTime());
		}
		ChatUserActivityStatistic statistic = ChatUserActivityStatistic.createFromActiveTimeAndDuration(userId,ACTIVITY_DOORATION_MS, datesLong);
		
		return statistic;	
	}
	
	@RequestMapping(value = "/statistic/user/get_week_activity_current_user", method = RequestMethod.GET)
	@ResponseBody
	public ChatUserActivityStatistic getCurrentUserActivityMapping(Principal principal){
		ChatUser user = chatUserService.getChatUser(principal);
		return getUserActivityMapping(user.getId());
	}
	
}
