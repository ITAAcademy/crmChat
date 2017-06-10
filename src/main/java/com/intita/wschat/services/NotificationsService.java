package com.intita.wschat.services;

import java.io.IOException;
import java.lang.reflect.Field;
import java.security.Principal;
import java.util.ArrayList;
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
import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.intita.wschat.domain.ChatMessage;
import com.intita.wschat.domain.UserRole;
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
public class NotificationsService {

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
	@Autowired private UserBirthdayService userBirthdayService;


	//check if have needs in update birthday mans
	int lastBirthdayUsersUpdate = -1;

	ArrayList<ChatUser> birthdayBoys = new ArrayList<>();

	@PostConstruct
	public void postConstructor(){

	}	

	public ArrayList<ChatNotification> generationNotification(){
		ArrayList<ChatNotification> list = new ArrayList<>();
		list.addAll(birthdayNotifications());
		return list;

	}

	private List<ChatNotification> birthdayNotifications(){
		ArrayList<ChatNotification> list = new ArrayList<>();
		ArrayList<ChatUser> users = userBirthdayService.getTodayBirthdayBoys();
		for(ChatUser user : users)
		{
			ChatNotification notification = new ChatNotification();
			notification.setType(ChatNotificationType.BIRTHDAY);
			notification.setImageUrl("images/birthday-cake.png");
			notification.setTitle("Не забуть привітати друга.");
			notification.setDetails("Cьогодні день народження у " + user.getNickName());
			notification.addParam("chatId", user.getId());
			list.add(notification);
		}
		return list;
	}

	public static class ChatNotification{
		private String title;
		private String details;
		private String imageUrl;
		private ChatNotificationType type;
		private Map<String, Object> params = new HashMap<>();
		
		public ChatNotification(){
			
		}
		public ChatNotification(String title, String detail, String imageUrl,ChatNotificationType type){
			this.title = title;
			this.details = details;
			this.imageUrl = imageUrl;
			this.type = type;
		}

		public String getTitle() {
			return title;
		}

		public void setTitle(String title) {
			this.title = title;
		}

		public String getDetails() {
			return details;
		}

		public void setDetails(String details) {
			this.details = details;
		}

		public String getImageUrl() {
			return imageUrl;
		}

		public void setImageUrl(String imageUrl) {
			this.imageUrl = imageUrl;
		}

		public ChatNotificationType getType() {
			return type;
		}

		public void setType(ChatNotificationType type) {
			this.type = type;
		}

		public Map<String, Object> getParams() {
			return params;
		}

		public void setParams(Map<String, Object> params) {
			this.params = params;
		}
		public void addParam(String key, Object param) {
			this.params.put(key, param);
		}
	}
	public enum ChatNotificationType{
		BIRTHDAY("birthday");

		private String value;

		private ChatNotificationType(String value){
			this.value = value;
		}
		@Override
		//@JsonValue
		public String toString() {
			return value;
		}
	}
}
