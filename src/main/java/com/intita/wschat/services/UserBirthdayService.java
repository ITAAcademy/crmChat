package com.intita.wschat.services;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import com.intita.wschat.event.ParticipantRepository;
import com.intita.wschat.models.ChatUser;
import com.intita.wschat.repositories.ChatLangRepository;
import com.intita.wschat.web.ChatController;
import com.intita.wschat.web.RoomController;

@Service
public class UserBirthdayService {

	@Autowired private SimpMessagingTemplate simpMessagingTemplate;
	@Autowired private RoomsService roomService;
	@Autowired private UsersService userService;
	@Autowired private UserMessageService userMessageService;
	@Autowired private ChatUsersService chatUsersService;
	@Autowired private ChatTenantService chatTenantService;
	@Autowired private ChatUserLastRoomDateService chatUserLastRoomDateService;
	@Autowired private ChatLangRepository chatLangRepository;
	@Autowired private ConsultationsService chatConsultationsService;
	@Autowired private CourseService courseService;
	@Autowired private BotAnswersService botAnswerService;
	@Autowired private ParticipantRepository participantRepository;
	@Autowired private ChatLangService chatLangService;
	@Autowired private RoomController roomControler;

	//check if have needs in update birthday mans
	int lastBirthdayUsersUpdate = -1;

	ArrayList<ChatUser> birthdayBoys = new ArrayList<>();

	@PostConstruct
	public void postConstructor(){
		getTodayBirthdayBoys();
	}	

	public ArrayList<ChatUser> getTodayBirthdayBoys(){
		Calendar cal = Calendar.getInstance();
		cal.setTime(new Date());
		int dayToday = cal.get(Calendar.DAY_OF_WEEK);
		if(lastBirthdayUsersUpdate != dayToday)
		{
			Long[] birthdayUsers =  userService.getAllUserWithBirthdayToday();
			birthdayBoys = chatUsersService.getChatUsersFromIntitaIds(new ArrayList<>(Arrays.asList(birthdayUsers)));    	
		}
		return birthdayBoys;
	}
}
