package com.intita.wschat.services;

import java.io.IOException;
import java.lang.reflect.Field;
import java.security.Principal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Timer;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.intita.wschat.config.ChatPrincipal;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.intita.wschat.domain.ChatMessage;
import com.intita.wschat.event.ParticipantRepository;
import com.intita.wschat.models.BotAnswer;
import com.intita.wschat.models.BotCategory;
import com.intita.wschat.models.BotDialogItem;
import com.intita.wschat.models.ChatTenant;
import com.intita.wschat.models.ChatUser;
import com.intita.wschat.models.LangId;
import com.intita.wschat.models.Room;
import com.intita.wschat.models.RoomPermissions;
import com.intita.wschat.models.User;
import com.intita.wschat.models.UserMessage;
import com.intita.wschat.repositories.ChatLangRepository;
import com.intita.wschat.web.ChatController;
import com.intita.wschat.web.RoomController;

import utils.RandomString;

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
	@Autowired private ChatController chatController;

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
