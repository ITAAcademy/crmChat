package com.intita.wschat.web;

import java.security.Principal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.intita.wschat.admin.models.MsgRequestRatingsModel;
import com.intita.wschat.domain.requestdata.UserActivityRequestData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

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

	@RequestMapping(value = "/statistic/user/get_activity", method = RequestMethod.POST)
	@ResponseBody
	public ChatUserActivityStatistic getUserActivityMapping(@RequestBody UserActivityRequestData activityRequestData) {
		final long ACTIVITY_DOORATION_MS = 5000;
		Date early = new Date(activityRequestData.getBeforeDate());
		Date late = new Date(activityRequestData.getAfterDate());
		List<Date> activityDates = userMessageService.getMessagesDatesByChatUserAndDate(activityRequestData.getChatUserId(),early,late);
		List<Long> datesLong = new ArrayList<Long>();
		for (Date date : activityDates){
			datesLong.add(date.getTime());
		}
		ChatUserActivityStatistic statistic = ChatUserActivityStatistic.createFromActiveTimeAndDuration(activityRequestData.getChatUserId(),ACTIVITY_DOORATION_MS, datesLong);
		
		return statistic;
	}
	
	/*@RequestMapping(value = "/statistic/user/get_week_activity_current_user", method = RequestMethod.GET)
	@ResponseBody
	public ChatUserActivityStatistic getCurrentUserActivityMapping(@RequestParam(value="days",required = false,defaultValue = "1") Integer days, Principal principal){
		ChatUser user = chatUserService.getChatUser(principal);
		return getUserActivityMapping(user.getId(),days);
	}*/
	
}
