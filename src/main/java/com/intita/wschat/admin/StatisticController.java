package com.intita.wschat.admin;

import java.util.*;

import com.intita.wschat.domain.responsedata.*;
import com.intita.wschat.domain.search.UserMessageSearchCriteria;
import com.intita.wschat.services.UsersService;
import com.intita.wschat.util.TimeUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import com.intita.wschat.services.ChatUsersService;
import com.intita.wschat.services.UserMessageService;

@Controller
public class StatisticController {
	final long ACTIVITY_DOORATION_MS = 5*60*1000;
@Autowired
ChatUsersService chatUserService;

	@Autowired
	UsersService usersService;

@Autowired
UserMessageService userMessageService;

	@RequestMapping(value = "/statistic/user/get_activity", method = RequestMethod.POST)
	@ResponseBody
	public ChatUserActivityStatistic getUserActivityMapping(@RequestBody UserMessageSearchCriteria activityRequestData) {

		List<Date> activityDates = userMessageService.findMessagesDate(activityRequestData);

		List<Long> datesLong = new ArrayList<Long>();
		for (Date date : activityDates){
			datesLong.add(date.getTime());
		}

		ChatUserActivityStatistic statistic = ChatUserActivityStatistic.createFromActiveTimeAndDuration(
				activityRequestData.getAuthorId(),ACTIVITY_DOORATION_MS, datesLong);
		
		return statistic;
	}




	@RequestMapping(value = "/statistic/user/get_visits", method = RequestMethod.GET)
	@ResponseBody
	public Map<Long,Integer> getUsersVisitsBetweenDatesMapping(@RequestParam(required = false) Long earlyDate,
															   @RequestParam(required = false) Long lateDate) {
		Date date1 = new Date(earlyDate);
		Date date2 = new Date(lateDate);
		Map<Long,Integer> statistic = chatUserService.getUsersVisitsInfoByDays(date1,date2);
		return statistic;
	}


	@RequestMapping(value ="/statistic/count_messages_today")
	@ResponseBody
	public StatisticResponseMessagesCount countMessagesToday(@RequestParam(required = false) String requestId){
		StatisticResponseMessagesCount responseData = new StatisticResponseMessagesCount();
		Long totalMessagesCount = userMessageService.getMessagesCountByDate(TimeUtil.getCurrentDay(),false);
		Long activeMessagesCount = userMessageService.getMessagesCountByDate(TimeUtil.getCurrentDay(),true);

		responseData.setTotalMessagesCount(totalMessagesCount);
		responseData.setActiveMessagesCount(activeMessagesCount);
		responseData.setRequestId(requestId);

		return responseData;
	}

	@RequestMapping(value ="/statistic/count_messages_per_role")
	@ResponseBody
	public StatisticResponseMessagesCountPerRole countMessagesToday(@RequestParam(required = false) String requestId,
															 @RequestParam(required = false) Long earlyDate,
															 @RequestParam(required = false) Long lateDate){
		StatisticResponseMessagesCountPerRole responseData = new StatisticResponseMessagesCountPerRole();
		Map<String,Integer> responseByRoles = userMessageService.countMessageByAllRolesAndBetweenDate(new Date(earlyDate)
				,new Date(lateDate));
		responseData.setMessageCountPerRole(responseByRoles);
		responseData.setRequestId(requestId);
		return responseData;
	}

	@RequestMapping(value = "/statistic/user/get_activity_per_day", method = RequestMethod.POST)
	@ResponseBody
	public ChatUserActivityPerDayStatistic getUserActivityByDayMapping(@RequestBody UserMessageSearchCriteria activityRequestData){
		ChatUserActivityStatistic statistic = getUserActivityMapping(activityRequestData);
		List<Long> datesWithoutTime = TimeUtil.removeTimeFromList(statistic.getActivityAtTime());
		Map<Long, Long> msOfActivityPerDay = new HashMap<Long,Long>();
		for (Long date : datesWithoutTime){
			if (!msOfActivityPerDay.containsKey(date))
			msOfActivityPerDay.put(date,ACTIVITY_DOORATION_MS);
			else{
				Long containedDate = msOfActivityPerDay.get(date);
				containedDate += ACTIVITY_DOORATION_MS;
				msOfActivityPerDay.put(date,containedDate);
			}
		}
		ChatUserActivityPerDayStatistic resultStatistic = new ChatUserActivityPerDayStatistic(msOfActivityPerDay);
		return resultStatistic;
	}
	@RequestMapping(value = "/statistic/user/count_active_users", method = RequestMethod.GET)
	@ResponseBody
	public StatisticResponseActiveUsers getActiveChatUsersCount(@RequestParam(required = false) String requestId,
																	 @RequestParam(required = false,defaultValue = "1") Integer days){
		long totalUsers = usersService.getUsersCount();
		long activeUsers =  chatUserService.getUsersVisitsFromDaysAgo(days);
		StatisticResponseActiveUsers activeUsersData = new StatisticResponseActiveUsers();
		activeUsersData.setTotalUsers(totalUsers);
		activeUsersData.setActiveUsers(activeUsers);
		activeUsersData.setRequestId(requestId);
		return activeUsersData;
	}

	
	/*@RequestMapping(value = "/statistic/user/get_week_activity_current_user", method = RequestMethod.GET)
	@ResponseBody
	public ChatUserActivityStatistic getCurrentUserActivityMapping(@RequestParam(value="days",required = false,defaultValue = "1") Integer days, Principal principal){
		ChatUser user = chatUserService.getChatUser(principal);
		return getUserActivityMapping(user.getId(),days);
	}*/
	
}
