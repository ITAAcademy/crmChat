package com.intita.wschat.web;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Semaphore;

import javax.annotation.PostConstruct;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import com.intita.wschat.config.ChatPrincipal;
import com.intita.wschat.domain.SubscribedtoRoomsUsersBufferModal;
import com.intita.wschat.dto.mapper.DTOMapper;
import com.intita.wschat.dto.model.ChatUserDTO;
import com.intita.wschat.dto.model.IntitaUserDTO;
import com.intita.wschat.dto.model.UserMessageDTO;
import com.intita.wschat.dto.model.UserMessageWithLikesDTO;
import com.intita.wschat.enums.LikeState;
import com.intita.wschat.services.*;
import com.intita.wschat.services.common.UsersOperationsService;
import com.intita.wschat.util.HtmlUtility;
import org.hibernate.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpRequest;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.annotation.SubscribeMapping;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.stereotype.Service;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.context.request.async.DeferredResult;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.intita.wschat.config.FlywayMigrationStrategyCustom;
import com.intita.wschat.domain.SessionProfanity;
import com.intita.wschat.domain.UserWaitingForTrainer;
import com.intita.wschat.domain.interfaces.IPresentOnForum;
import com.intita.wschat.event.LoginEvent;
import com.intita.wschat.event.ParticipantRepository;
import com.intita.wschat.exception.ChatUserNotFoundException;
import com.intita.wschat.exception.ChatUserNotInRoomException;
import com.intita.wschat.exception.RoomNotFoundException;
import com.intita.wschat.models.ChatUser;
import com.intita.wschat.models.ChatUserLastRoomDate;
import com.intita.wschat.models.Course;
import com.intita.wschat.models.OperationStatus;
import com.intita.wschat.models.OperationStatus.OperationType;
import com.intita.wschat.models.Room;
import com.intita.wschat.models.RoomModelSimple;
import com.intita.wschat.models.User;
import com.intita.wschat.models.UserMessage;
import com.intita.wschat.util.ProfanityChecker;
import jsonview.Views;

/**
 * Controller that handles WebSocket chat messages
 * 
 * @author Nicolas Haiduchok
 */
@Controller
public class ChatController {

	@Autowired
	ConfigParamService configParamService;
	private final static Logger log = LoggerFactory.getLogger(ChatController.class);

	@Autowired
	private ProfanityChecker profanityFilter;
	@Autowired
	private SessionProfanity profanity;
	@Autowired
	private ParticipantRepository participantRepository;
	@Autowired
	private SimpMessagingTemplate simpMessagingTemplate;
	@Autowired
	private RoomsService chatRoomsService;
	@Autowired
	private UsersService userService;
	@Autowired
	private UserMessageService userMessageService;
	@Autowired
	private ChatUsersService chatUsersService;
	@Autowired
	private ChatUserLastRoomDateService chatUserLastRoomDateService;
	@Autowired
	private CourseService courseService;
	@Autowired
	private ChatLangService chatLangService;
	@Autowired
	private ChatTenantService chatTenantService;
	@Autowired
	private IntitaSubGtoupService subGroupService;
	@Autowired
	private FlywayMigrationStrategyCustom flyWayStategy;
	@Autowired
	private IntitaMailService mailService;

	@Autowired
	private CommonController commonController;

	@Autowired
	private RoomHistoryService roomHistoryService;

	@Autowired
	private DTOMapper dtoMapper;

	@Autowired UserMessageService messageService;

	@Autowired
	ChatLikeStatusService chatLikeStatusService;

	@Autowired RoomController roomController;

	@Autowired
	@Lazy
	UsersOperationsService usersOperationsService;

	@Value("${crmchat.send_unreaded_messages_email:true}")
	private Boolean sendUnreadedMessagesToEmail;


	private final Semaphore msgLocker = new Semaphore(1);

	@PersistenceContext
	EntityManager entityManager;

	protected Session getCurrentHibernateSession() {
		return entityManager.unwrap(Session.class);
	}

	private final static ObjectMapper mapper = new ObjectMapper();


	// =>
	// roomId

	// =>
	// roomId

	ConcurrentHashMap<DeferredResult<String>, String> globalInfoResult = new ConcurrentHashMap<DeferredResult<String>, String>();

	// of
	// tenatn

	public void removePrivateRoomRequiredTrainerFromList(Long roomId) {
		List<UserWaitingForTrainer> userWaitingRooms = new ArrayList<UserWaitingForTrainer>();
		for (UserWaitingForTrainer user : usersOperationsService.getUsersRequiredTrainers()) {
			if (Long.compare(user.getChatUserId(), roomId) == 0) {
				userWaitingRooms.add(user);
			}
		}
		usersOperationsService.getUsersRequiredTrainers().removeAll(userWaitingRooms);
	}


	@PostConstruct
	public void onCreate() {
		flyWayStategy.getFlyway().migrate();
		chatLangService.updateDataFromDatabase();
	}

	public static class CurrentStatusUserRoomStruct {
		private Room room;
		private ChatUser user;

		public Room getRoom() {
			return room;
		}

		public ChatUser getUser() {
			return user;
		}

		public CurrentStatusUserRoomStruct(ChatUser user, Room room) {
			this.room = room;
			this.user = user;
		}
	}

	public static CurrentStatusUserRoomStruct isMyRoom(Long roomId, ChatPrincipal chatPrincipal , RoomsService chat_room_service) {
		if(roomId == null) return null;
		long startTime = System.currentTimeMillis();
		Room o_room = chat_room_service.getRoom(roomId);
		if (o_room == null)
			return null;
		ChatUser o_user = chatPrincipal.getChatUser();
		if (o_user == null || o_user.getId()==null) return null;

		Set<Room> all = chat_room_service.getAllRoomByUsersAndAuthor(o_user);

		if (!all.contains(o_room))
			return null;

		return new CurrentStatusUserRoomStruct(o_user, o_room);
	}

	/********************
	 * GET CHAT USERS LIST FOR TEST
	 *******************/
	@RequestMapping(value = "/chat/users", method = RequestMethod.POST)
	@ResponseBody
	public String getUsers(Authentication auth) throws JsonProcessingException {
		Page<User> pageUsers = userService.getUsers(1, 15);
		Set<LoginEvent> userList = new HashSet<>();
		for (User user : pageUsers) {
			ChatUser chat_user = chatUsersService.getChatUserFromIntitaUser(user, true);
			userList.add(new LoginEvent(user.getId(), user.getUsername(), user.getAvatar()));// participantRepository.isOnline(""+chat_user.getId())));
		}
		return new ObjectMapper().writeValueAsString(userList);
	}

	@RequestMapping(value = "/{room}/chat/loadOtherMessage", method = RequestMethod.POST)
	@ResponseBody
	public List<UserMessageWithLikesDTO> loadOtherMessageMapping(@PathVariable("room") Long room,
																	 @RequestBody Map<String, String> json, Authentication auth) {
		return loadOtherMessage(room, json, auth, false);
	}

	@RequestMapping(value = "/{room}/chat/loadOtherMessageWithFiles", method = RequestMethod.POST)
	@ResponseBody
	public List<UserMessageWithLikesDTO> loadOtherMessageWithFilesMapping(@PathVariable("room") Long room,
																		  @RequestBody(required = false) Map<String, String> json, Authentication auth) {
		return loadOtherMessage(room, json, auth, true);
	}

	public List<UserMessageWithLikesDTO> loadOtherMessage(Long room, Map<String, String> json, Authentication auth,
															  boolean filesOnly) {
		ChatPrincipal chatPrincipal = (ChatPrincipal)auth.getPrincipal();
		String dateMsStr = json.get("date");
		Long dateMs = null;
		if (dateMsStr != null && dateMsStr.length() > 0)
			dateMs = Long.parseLong(dateMsStr);
		Date date = (dateMs == null) ? null : new Date(dateMs);
		CurrentStatusUserRoomStruct struct = ChatController.isMyRoom(room, chatPrincipal,
				chatRoomsService);// Control room from LP
		if (struct == null)
			throw new ChatUserNotInRoomException("");
		String searchQuery = json.get("searchQuery");
		ChatUser chatUser = chatPrincipal.getChatUser();
		Date clearDate = roomHistoryService.getHistoryClearDate(struct.getRoom().getId(), chatUser.getId());
		ArrayList<UserMessage> messages = userMessageService.getMessages(struct.getRoom().getId(), date, clearDate,
				searchQuery, filesOnly, 20);
		if (messages.size() == 0)
			return null;
		List<UserMessageWithLikesDTO> messagesAfter = dtoMapper.mapListUserMessagesWithLikes(messages);

		if (messagesAfter.size() == 0)
			return null;

		return messagesAfter;
	}

	public UserMessage filterMessage(Long roomStr, UserMessageDTO messageDTO, Authentication auth) {
		return usersOperationsService.filterMessage(roomStr,messageDTO,auth);
	}







	@SubscribeMapping("/chat/room.private/room_require_trainer")
	public List<UserWaitingForTrainer> retrievePrivateRoomsRequiredTrainersSubscribeMapping() {
		return usersOperationsService.getUsersRequiredTrainers();
	}

	@MessageMapping("/{room}/chat.message")
	public UserMessageWithLikesDTO filterMessageWS(@DestinationVariable("room") Long roomId, @Payload UserMessageDTO message,
												  Authentication auth) {
		return usersOperationsService.filterMessageWS(roomId,message,auth);
	}

	@RequestMapping(value = "/{roomId}/chat/message", method = RequestMethod.POST)
	@ResponseBody
	public void filterMessageLP(@PathVariable("roomId") Long roomId, @RequestBody UserMessageDTO messageDTO,
			Authentication auth) {
		 usersOperationsService.filterMessageLP(roomId,messageDTO,auth);
	}


	@RequestMapping(value = "/{room}/chat/message/update", method = RequestMethod.POST, produces = "application/json; charset=UTF-8")
	@ResponseBody
	public DeferredResult<String> updateMessageLP(@PathVariable("room") Long room) throws JsonProcessingException {
		Long timeOut = 60000L;
		Map<String, Queue<DeferredResult<String>>> responseBodyQueue = usersOperationsService.getResponseBodyQueu();
		usersOperationsService.getResponseBodyQueu();
		DeferredResult<String> result = new DeferredResult<String>(timeOut, "{}");
		Queue<DeferredResult<String>> queue =  responseBodyQueue.get(room.toString());
		if (queue == null) {
			queue = new ConcurrentLinkedQueue<DeferredResult<String>>();
			responseBodyQueue.put(room.toString(), queue);
		}
		queue.add(result);
		return result;
	}


	@RequestMapping(value = "/chat/global/lp/info", method = RequestMethod.POST)
	@ResponseBody
	public DeferredResult<String> updateGlobalInfoLP(Authentication auth) throws JsonProcessingException {
		int presenceIndexGrowth = 3;
		Long timeOut = 15000L;
		ChatPrincipal chatPrincipal = (ChatPrincipal)auth.getPrincipal();
		ChatUser chatUser = chatPrincipal.getChatUser();
		participantRepository.addParticipantPresenceByLastConnectionTime(chatUser.getId());
		DeferredResult<String> result = new DeferredResult<String>(timeOut, "{}");
		globalInfoResult.put(result, chatUser.getId().toString());
		return result;
	}

	public void processUsersPresence() {
		for (Long chatId : participantRepository.getActiveSessions().keySet()) {
			boolean isParticipantRemoved = participantRepository.invalidateParticipantPresence(chatId, false);
			ArrayList<ChatUser> tenantsToRemove = new ArrayList();
			if (isParticipantRemoved) {
				if (chatTenantService.isTenant(chatId))
					tenantsToRemove.add(chatUsersService.getChatUser(chatId));
			}
			if (tenantsToRemove.size() > 0)
				usersOperationsService.propagateRemovingTenantsFromList(tenantsToRemove);
		}
	}

	@Scheduled(fixedDelay = 10000L)
	public void processGlobalInfo() {
		String result;
		ConcurrentHashMap<String, ArrayList<Object>> infoMap = usersOperationsService.getInfoMap();
		ConcurrentHashMap<Long, ConcurrentHashMap<String, ArrayList<Object>>> infoMapForUser = usersOperationsService.getInfoMapForUser();
		processUsersPresence();
		try {
			result = mapper.writeValueAsString(infoMap);
		} catch (JsonProcessingException e) {
			e.printStackTrace();
			result = "{}";
		}
		for (DeferredResult<String> nextUser : globalInfoResult.keySet()) {
			// System.out.println("globalInfoResult.remove:"+globalInfoResult.get(nextUser));
			String chatId = globalInfoResult.get(nextUser);

			ConcurrentHashMap<String, ArrayList<Object>> map = infoMapForUser.get(Long.parseLong(chatId));
			if (map != null) {
				map.putAll(infoMap);
				try {
					result = mapper.writeValueAsString(map);
				} catch (JsonProcessingException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					result = "{}";
				}
			}
			LoginEvent loginEvent = new LoginEvent(Long.parseLong(chatId), chatId);// ,
			// participantRepository.isOnline(chatId));
			simpMessagingTemplate.convertAndSend("/topic/chat.logout", loginEvent);
			if (!nextUser.isSetOrExpired() && result != "{}")// @BAD@
				nextUser.setResult(result);
		}

		infoMap.clear();
		globalInfoResult.clear();
		infoMapForUser.clear();

	}

	@MessageMapping("/{room}/chat.private.{username}")
	public void filterPrivateMessage(@DestinationVariable Long room, @Payload UserMessageDTO messageDTO,
			@DestinationVariable("username") String username, Authentication auth) {
		checkProfanityAndSanitize(messageDTO);
		ChatPrincipal chatPrincipal = (ChatPrincipal)auth.getPrincipal();
		Long chatUserId = chatPrincipal.getChatUser().getId();
		OperationStatus operationStatus = new OperationStatus(OperationType.SEND_MESSAGE_TO_USER, true,
				"SENDING MESSAGE TO USER");
		String subscriptionStr = "/topic/users/" + chatUserId + "/status";
		simpMessagingTemplate.convertAndSend(subscriptionStr, operationStatus);

		simpMessagingTemplate.convertAndSend("/user/" + username + "/exchange/amq.direct/" + room + "/chat.message",
				messageDTO);
	}

	/*
	 * Go into room
	 */

	@RequestMapping(value = "/chat.go.to.dialog/{roomId}", method = RequestMethod.POST)
	@ResponseBody
	public boolean userGoToDialogListenerLP(@PathVariable("roomId") Long roomId, Authentication auth) {
		// checkProfanityAndSanitize(message);
		ChatPrincipal chatPrincipal = (ChatPrincipal) auth.getPrincipal();
		CurrentStatusUserRoomStruct struct = isMyRoom(roomId, chatPrincipal,
				chatRoomsService);
		if (struct == null)
			throw new ChatUserNotInRoomException("CurrentStatusUserRoomStruct struct is null");

		ChatUser user = struct.getUser();
		Room room = struct.getRoom();

		if (room == null)
			throw new RoomNotFoundException("room is null");

		ChatUserLastRoomDate last = chatUserLastRoomDateService.getUserLastRoomDate(room, user);
		last.setLastLogout(new Date());
		chatUserLastRoomDateService.updateUserLastRoomDateInfo(last);

		/*
		 * send info about some user enter to current room
		 */
		Map <String, Object> result = new HashMap<>(); 
		result.put("roomId", roomId);
		result.put("chatUserId", user.getId());
		result.put("type", "roomRead");

		usersOperationsService.addFieldToInfoMap("roomRead", result);
		simpMessagingTemplate.convertAndSend("/topic/users/info", result);

		// return
		// chatRoomsService.getSimpleModelByUserPermissionsForRoom(struct.user,
		// 0 , new Date().toString(), room,
		// userMessageService.getLastUserMessageByRoom(room));
		return true;
	}

	@RequestMapping(value = "/chat/get_students/", method = RequestMethod.GET)
	@ResponseBody
	public Set<LoginEvent> getTrainerStudents(Authentication auth) {
		ChatPrincipal chatPrincipal = (ChatPrincipal)auth.getPrincipal();
		ChatUser user = chatPrincipal.getChatUser();
		ArrayList<User> users = null;
		User iUser = chatPrincipal.getIntitaUser();

		if (iUser != null) {
			Long intitaUserId = iUser.getId();
			users = userService.getStudents(intitaUserId);
		} else
			return new HashSet<LoginEvent>();

		Set<LoginEvent> userList = new HashSet<>();
		for (User u : users) {
			ChatUser chat_user = chatUsersService.getChatUserFromIntitaUser(u, true);
			userList.add(chatUsersService.getLoginEvent(chat_user));// ,participantRepository.isOnline(""+chat_user.getId())));
		}
		return userList;

	}

	@RequestMapping(value = "/chat/get_students/{id}", method = RequestMethod.GET)
	@ResponseBody
	public Set<LoginEvent> getTrainerStudentsById(@PathVariable Long trainerId, Authentication auth) {
		ChatPrincipal chatPrincipal = (ChatPrincipal)auth.getPrincipal();
		ChatUser user = chatPrincipal.getChatUser();
		ArrayList<User> users = null;
		User iUser = user.getIntitaUser();

		if (iUser != null) {
			Long intitaUserId = user.getIntitaUser().getId();
			users = userService.getStudents(intitaUserId);
		} else
			return new HashSet<LoginEvent>();

		Set<LoginEvent> userList = new HashSet<>();
		for (User u : users) {
			ChatUser chat_user = chatUsersService.getChatUserFromIntitaUser(u, true);
			userList.add(chatUsersService.getLoginEvent(chat_user));// ,participantRepository.isOnline(""+chat_user.getId())));
		}
		return userList;

	}
	/*
	 * LIKES
	 */

	@RequestMapping(value = "/chat/like_message/{messageId}", method = RequestMethod.GET)
	@ResponseBody
	public Collection<ChatUserDTO> getLikeMessageById(@PathVariable Long messageId, Authentication auth) throws Exception {
		ChatPrincipal chatPrincipal = (ChatPrincipal)auth.getPrincipal();
		ChatUser chatUser = chatPrincipal.getChatUser();
		List<LoginEvent> res = new LinkedList<>();
		Collection<ChatUser> listChatUsers = chatLikeStatusService.getChatUserWhoCheckStateByMsg(messageId, LikeState.LIKE);
		return dtoMapper.map(listChatUsers);
	}
	

	@RequestMapping(value = "/chat/dislike_message/{messageId}", method = RequestMethod.GET)
	@ResponseBody
	public Collection<ChatUserDTO> getDisLikeMessageById(@PathVariable Long messageId, Authentication auth) throws Exception {
		ChatPrincipal chatPrincipal = (ChatPrincipal)auth.getPrincipal();
		ChatUser chatUser = chatPrincipal.getChatUser();
		List<LoginEvent> res = new LinkedList<>();
		Collection<ChatUser> listChatUsers = chatLikeStatusService.getChatUserWhoCheckStateByMsg(messageId, LikeState.DISLIKE);
		return dtoMapper.map(listChatUsers);
	}
	
	@RequestMapping(value = "/chat/like_message/{messageId}", method = RequestMethod.POST)
	@ResponseBody
	public boolean likeMessageById(@PathVariable Long messageId, Authentication auth) throws Exception {
		ChatPrincipal chatPrincipal = (ChatPrincipal)auth.getPrincipal();
		ChatUser chatUser = chatPrincipal.getChatUser();
		boolean result = chatLikeStatusService.likeMessage(messageId,chatUser);
		if (!result) {
			throw new Exception("can't like user");
		}
		return true;
	}
	
	@RequestMapping(value = "/chat/dislike_message/{messageId}", method = RequestMethod.POST)
	@ResponseBody
	public boolean dislikeMessageById(@PathVariable Long messageId, Authentication auth) throws Exception {
		ChatPrincipal chatPrincipal = (ChatPrincipal)auth.getPrincipal();
		ChatUser chatUser = chatPrincipal.getChatUser();
		boolean result = chatLikeStatusService.dislikeMessage(messageId,chatUser);
		if (!result) {
			throw new Exception("can't unlike user");
		}
		return true;
	}

	@RequestMapping(value = "/chat/discard_like_message/{messageId}", method = RequestMethod.POST)
	@ResponseBody
	public boolean discardLikeMessageById(@PathVariable Long messageId, Authentication auth) throws Exception {
		ChatPrincipal chatPrincipal = (ChatPrincipal)auth.getPrincipal();
		ChatUser chatUser = chatPrincipal.getChatUser();
		boolean result = chatLikeStatusService.removeLikeStatus(messageId,chatUser);
		if (!result) {
			throw new Exception("can't discard like user");
		}
		return true;
	}


	/*
	 * Out from room
	 */
	@MessageMapping("/chat.go.to.dialog.list/{roomId}")
	public void userGoToDialogListListener(@DestinationVariable("roomId") Long roomId, Authentication auth,
			@RequestBody Map<String, Object> params) {
		// checkProfanityAndSanitize(message);
		ChatPrincipal chatPrincipal = (ChatPrincipal)auth.getPrincipal();
		CurrentStatusUserRoomStruct struct = isMyRoom(roomId, chatPrincipal,
				chatRoomsService);
		if (struct == null)
			return;

		ChatUser user = struct.getUser();
		Room room = struct.getRoom();

		if (room == null)
			return;
		// public ChatUserLastRoomDate(Long id, Date last_logout, Long
		// last_room){
		ChatUserLastRoomDate last = chatUserLastRoomDateService.getUserLastRoomDate(room, user);
		if (last == null) {
			last = new ChatUserLastRoomDate(user.getId(), new Date(), room);
			last.setChatUser(user);
		} else {
			last.setLastLogout(new Date());
		}
		chatUserLastRoomDateService.updateUserLastRoomDateInfo(last);

		usersOperationsService.updateRoomsByUser(user, (HashMap<String, Object>) params.get("roomForUpdate"));

	}



	@RequestMapping(value = "/chat/update/dialog_list", method = RequestMethod.POST)
	@ResponseBody
	public void updateRoomsByList(Authentication auth, @RequestBody Map<String, Object> params) {
		ChatPrincipal chatPrincipal = (ChatPrincipal)auth.getPrincipal();
		ChatUser user = chatPrincipal.getChatUser();
		if (user != null) {
			usersOperationsService.updateRoomsByUser(user, (HashMap<String, Object>) params.get("roomForUpdate"));
		}
	}

	@RequestMapping(value = "/chat.go.to.dialog.list/{roomId}", method = RequestMethod.POST)
	@ResponseBody
	public void userGoToDialogListListenerLP(@PathVariable("roomId") Long roomId, Authentication auth,
			@RequestBody Map<String, Object> params) {

		userGoToDialogListListener(roomId, auth, params);
	}

	/*
	 * Work only on WS
	 */
	private void checkProfanityAndSanitize(UserMessageDTO message) {
		long profanityLevel = profanityFilter.getMessageProfanity(message.getBody());
		profanity.increment(profanityLevel);
		message.setBody(profanityFilter.filter(message.getBody()));
	}

	@RequestMapping(value = "/get_commands_like", method = RequestMethod.GET)
	@ResponseBody
	public String getCommandsLike(@RequestParam String command) throws JsonProcessingException {
		List<String> commands = new ArrayList<String>();
		commands.add(new String("createDialogWithBot"));
		commands.add(new String("createConsultation"));

		List<String> result = new ArrayList<String>();
		for (int i = 0; i < commands.size(); i++) {
			if (commands.get(i).matches(new String(".*" + command + ".*")))
				result.add(commands.get(i));
		}

		ObjectMapper mapper = new ObjectMapper();

		String jsonInString = mapper.writeValueAsString(result);
		return jsonInString;
	}

	@RequestMapping(value = "/get_users_emails_like", method = RequestMethod.GET)
	@ResponseBody
	public String getEmailsLike(@RequestParam String login, @RequestParam Long room,
			boolean eliminate_users_of_current_room) throws JsonProcessingException {
		List<String> emails = null;

		if (eliminate_users_of_current_room) {
			List<ChatUser> users = new ArrayList<ChatUser>();
			Set<ChatUser> users_set = null;
			users_set = chatRoomsService.getRoom(room).getUsers();
			users.addAll(users_set);
			users.add(chatRoomsService.getRoom(room).getAuthor());

			List<Long> room_emails = new ArrayList<>();
			for (int i = 0; i < users.size(); i++) {
				User i_user = users.get(i).getIntitaUser();
				if (i_user != null)
					room_emails.add(i_user.getId());
			}
			emails = userService.getUsersEmailsFist5WhereUserNotIn(login, room_emails);
		} else
			emails = userService.getUsersEmailsFist5(login);

		ObjectMapper mapper = new ObjectMapper();

		String jsonInString = mapper.writeValueAsString(emails);
		return jsonInString;
	}

	@RequestMapping(value = "/get_users_like", method = RequestMethod.GET)
	@ResponseBody
	public String getUsersLike(@RequestParam String login, @RequestParam(required = false) Long room,
			@RequestParam(required = false) boolean eliminate_users_of_current_room) throws JsonProcessingException {
		ArrayList<User> usersResult = null;
		if (eliminate_users_of_current_room) {
			ArrayList<Long> roomUsers = chatRoomsService.getRoomUsersIds(room);
			usersResult = new ArrayList(userService.getUsersFistNWhereUserNotIn(login, roomUsers, 10));
		} else
			usersResult = new ArrayList(userService.getUsersFistN(login, 10));
		ObjectMapper mapper = new ObjectMapper();
		String jsonInString = mapper.writerWithView(Views.Public.class).writeValueAsString(usersResult);
		return jsonInString;
	}

	@RequestMapping(value = "/get_users_log_events_like", method = RequestMethod.GET)
	@ResponseBody
	public String getChatUsersLike(@RequestParam String login, @RequestParam(required = false) Long room,
			@RequestParam(required = false) boolean eliminate_users_of_current_room) throws JsonProcessingException {
		ArrayList<User> usersResult = null;
		if (eliminate_users_of_current_room) {
			ArrayList<Long> roomUsers = chatRoomsService.getRoomUsersIds(room);
			usersResult = new ArrayList(userService.getUsersFistNWhereUserNotIn(login, roomUsers, 10));
		} else
			usersResult = new ArrayList(userService.getUsersFistN(login, 10));
		ArrayList<LoginEvent> loginEvents = new ArrayList<>();
		for (User u : usersResult)
			loginEvents.add(new LoginEvent(u, chatUsersService.getChatUserFromIntitaUser(u, false).getId()));
		ObjectMapper mapper = new ObjectMapper();
		String jsonInString = mapper.writerWithView(Views.Public.class).writeValueAsString(loginEvents);
		return jsonInString;
	}

	@RequestMapping(value = "/get_group_users_by_trainer", method = RequestMethod.GET)
	@ResponseBody
	public String getGroupUsersByTrainer(@RequestParam(required = true) Long trainerChatId)
			throws JsonProcessingException {
		User intitaUser = userService.getUserFromChat(trainerChatId);
		ArrayList<ChatUser> usersResult = subGroupService.getTrainerStudents(intitaUser.getId());
		ArrayList<LoginEvent> loginEvents = new ArrayList<>();
		for (ChatUser chatUser : usersResult)
			loginEvents.add(new LoginEvent(chatUser));
		ObjectMapper mapper = new ObjectMapper();
		String jsonInString = mapper.writerWithView(Views.Public.class).writeValueAsString(loginEvents);
		return jsonInString;
	}

	@RequestMapping(value = "/get_group_rooms_by_trainer", method = RequestMethod.GET)
	@ResponseBody
	public String getGroupRoomsByTrainer(@RequestParam(required = true) Long trainerChatId, Authentication auth)
			throws JsonProcessingException {
		ChatPrincipal chatPrincipal = (ChatPrincipal)auth.getPrincipal();
		User intitaUser = userService.getUserFromChat(trainerChatId);
		ArrayList<Room> roomsResult = subGroupService.getTrainerGroupRooms(intitaUser.getId());
		ArrayList<RoomModelSimple> roomsModels = new ArrayList<RoomModelSimple>();
		for (Room room : roomsResult) {
			CurrentStatusUserRoomStruct struct = ChatController.isMyRoom(room.getId(), chatPrincipal, chatRoomsService);
			RoomModelSimple sb = chatRoomsService.getSimpleModelByUserPermissionsForRoom(struct.getUser(), 0,
					new Date().toString(), struct.getRoom(),
					userMessageService.getLastUserMessageByRoom(struct.getRoom()));
			roomsModels.add(sb);
		}
		ObjectMapper mapper = new ObjectMapper();
		String jsonInString = mapper.writerWithView(Views.Public.class).writeValueAsString(roomsModels);
		return jsonInString;
	}

	@RequestMapping(value = "/get_rooms_containing_string", method = RequestMethod.GET)
	@ResponseBody
	public List<RoomModelSimple> getChatUsersLike(@RequestParam String query, Authentication auth)
			throws JsonProcessingException {
		ChatPrincipal chatPrincipal = (ChatPrincipal)auth.getPrincipal();
		ChatUser chatUser = chatPrincipal.getChatUser();
		List<RoomModelSimple> result = chatRoomsService.getRoomsContainingStringByOwner(query, chatUser);
		return result;
	}

	@RequestMapping(value = "/get_all_users_emails_like", method = RequestMethod.GET)
	@ResponseBody
	public String getAllEmailsLike(@RequestParam String email) throws JsonProcessingException {
		List<String> emails = userService.getUsersEmailsFist5(email);
		ObjectMapper mapper = new ObjectMapper();
		String jsonInString = mapper.writeValueAsString(emails);
		return jsonInString;
	}

	@RequestMapping(value = "/get_courses_like", method = RequestMethod.GET)
	@ResponseBody
	public String getCoursesLike(@RequestParam String prefix, @RequestParam String lang)
			throws JsonProcessingException {
		ArrayList<String> coursesNames = courseService.getAllCoursesNamesWithTitlePrefix(prefix, lang);
		ObjectMapper mapper = new ObjectMapper();

		String jsonInString = mapper.writeValueAsString(coursesNames);
		return jsonInString;
	}

	@RequestMapping(value = "/get_users_nicknames_like", method = RequestMethod.GET)
	@ResponseBody
	public Set<LoginEvent> getNickNamesLike(@RequestParam String nickName, @RequestParam Long room)
			throws JsonProcessingException {

		Set<ChatUser> users_set = chatRoomsService.getRoom(room).cloneChatUsers();
		List<ChatUser> users = new ArrayList<ChatUser>();
		users.addAll(users_set);

		users.add(chatRoomsService.getRoom(room).getAuthor());
		Set<LoginEvent> usersData = new HashSet<LoginEvent>();

		List<String> room_nicks = new ArrayList<String>();
		for (int i = 0; i < users.size(); i++) {
			room_nicks.add(users.get(i).getNickName());
		}

		List<String> nicks = chatUsersService.getUsersNickNameFist5(nickName, room_nicks);

		for (ChatUser singleChatUser : users) {
			String nn = singleChatUser.getNickName();
			if (!nicks.contains(nn))
				continue;
			LoginEvent userData = new LoginEvent(singleChatUser.getId(), nn);// ,participantRepository.isOnline(""+singleChatUser.getId()));
			usersData.add(userData);
		}
		return usersData;
	}

	@RequestMapping(value = "/get_id_by_username", method = RequestMethod.GET)
	@ResponseBody
	public Long getIdByUsername(@RequestParam String intitaUsername) {
		User user = userService.getUser(intitaUsername);
		if (user == null)
			throw new ChatUserNotFoundException("");
		return user.getId();
	}

	@RequestMapping(value = "/get_course_alias_by_title", method = RequestMethod.GET)
	@ResponseBody
	public String getCourseAliasByTitle(@RequestParam String title, @RequestParam String lang) {
		Course course = courseService.getByTitle(title, lang);
		String jsonInString = null;
		try {
			jsonInString = mapper.writeValueAsString(course.getAlias());
		} catch (JsonProcessingException e) {
			e.printStackTrace();
		}
		return jsonInString;
	}

	@RequestMapping(value = "/get_users_nicknames_like_without_room", method = RequestMethod.GET)
	@ResponseBody
	public Set<LoginEvent> getNickNamesLike(@RequestParam String nickName) throws JsonProcessingException {
		List<ChatUser> users = chatUsersService.getChatUsersByEmailAndName(nickName);
		Set<LoginEvent> usersData = new HashSet<LoginEvent>();
		for (ChatUser user : users) {
			usersData.add(new LoginEvent(user));// participantRepository.isOnline(""+nick.getId())));
		}
		return usersData;
	}

	@RequestMapping(value = "/user_by_chat_id", method = RequestMethod.GET)
	@ResponseBody
	public IntitaUserDTO getNickNamesLike(@RequestParam Long chatUserId) throws JsonProcessingException {
		User intitaUser = userService.getUserFromChat(chatUserId);
		if(intitaUser == null)
			return null;
		IntitaUserDTO userDTO = dtoMapper.map(intitaUser);
		return userDTO;
	}


	@RequestMapping(value = "/chatTemplate.html", method = RequestMethod.GET)
	public String getChatTemplate(HttpRequest request, Model model, Authentication auth) {
		ChatPrincipal chatPrincipal = (ChatPrincipal)auth.getPrincipal();
		ChatUser user = chatPrincipal.getChatUser();
		User iUser = user.getIntitaUser();
		if (iUser != null) {
			Long intitaUserId = user.getIntitaUser().getId();
		}
		return commonController.getTeachersTemplate(request, "chatTemplate", model, auth);
	}

	@RequestMapping(value = "/get_room_messages", method = RequestMethod.GET)
	@ResponseBody
	public String getRoomMessages(@RequestParam Long roomId, Authentication auth) throws JsonProcessingException {
		mapper.disable(MapperFeature.DEFAULT_VIEW_INCLUSION);
		ChatPrincipal chatPrincipal = (ChatPrincipal)auth.getPrincipal();
		boolean isAdmin = userService.isAdmin(chatPrincipal.getIntitaUser().getId());
		if (!isAdmin)
			return null;
		return mapper.writerWithView(Views.Public.class)
				.writeValueAsString(userMessageService.getUserMessagesByRoomId(roomId));
	}

	@RequestMapping(value = "/chat/room/{roomId}/get_messages_contains", method = RequestMethod.POST)
	@ResponseBody
	public List<UserMessageWithLikesDTO> getRoomMessagesContains(@PathVariable("roomId") Long roomId,
																	 @RequestBody(required = false) String searchQuery, Authentication auth) throws JsonProcessingException {
		ChatPrincipal chatPrincipal = (ChatPrincipal)auth.getPrincipal();
		ChatUser chatUser = chatPrincipal.getChatUser();
		Date clearDate = roomHistoryService.getHistoryClearDate(roomId, chatUser.getId());
		ArrayList<UserMessage> userMessages = userMessageService.getMessages(roomId, null, clearDate, searchQuery,
				false, 20);
		List<UserMessageWithLikesDTO> chatMessages = dtoMapper.mapListUserMessagesWithLikes(userMessages);
		return chatMessages;
	}

	@RequestMapping(value = "/chat/room/{roomId}/clear_history", method = RequestMethod.POST)
	@ResponseBody
	public boolean clearRoomHistory(@PathVariable("roomId") Long roomId, Authentication auth)
			throws JsonProcessingException {
		ChatPrincipal chatPrincipal = (ChatPrincipal)auth.getPrincipal();
		ChatUser chatUser = chatPrincipal.getChatUser();
		Room room = chatRoomsService.getRoom(roomId);
		// if (room.getAuthor().equals(chatUser))
		roomHistoryService.clearRoomHistory(room, chatUser);
		return true;
	}

	@RequestMapping(value = "/chat/persist_temporary_guest", method = RequestMethod.POST)
	@ResponseBody
	public boolean persistTemporaryUser(Authentication auth,@RequestBody String firstMessage)
			throws JsonProcessingException {
		ChatPrincipal chatPrincipal = (ChatPrincipal)auth.getPrincipal();
		ChatUser chatUser = chatPrincipal.getChatUser();
		ChatUser persistedUser = chatUsersService.persistGuest(chatUser.getNickName());
		if (persistedUser==null)return false;
		chatPrincipal.setChatUser(chatUser);
		Room room = chatRoomsService.register(chatUser.getNickName(),chatUser);
		messageService.addMessage(persistedUser,room,firstMessage);
		usersOperationsService.welcomeTenantToRoomWithGuest(room,chatUser);
		return true;
	}

	@RequestMapping(value = "/chat/user/send_new_messages_notification", method = RequestMethod.GET)
	@ResponseBody
	public boolean sendNewMessageNotifications(Authentication auth) throws JsonProcessingException {
		ChatPrincipal chatPrincipal = (ChatPrincipal)auth.getPrincipal();
		User user = chatPrincipal.getIntitaUser();
		log.info("sending email to:" + user.getEmail());
		try {
			mailService.sendUnreadedMessageToIntitaUserFrom24Hours(user);
		} catch (Exception e) {
			log.info("sending failed");
		}
		return true;
	}

	private void sendAllNewMessageNotificationsFromLast24Hours() {
		final int pageSize = 30;
		int currentPage = 1;
		Page<User> intitaUsersPage = userService.getChatUsers(currentPage, pageSize);
		log.info("sending emails to users:");
		int pagesTotal = intitaUsersPage.getTotalPages();
		while(currentPage<=pagesTotal){
			log.info("sending page "+currentPage+"/"+pagesTotal);
			for (User user : intitaUsersPage.getContent()) {
				log.info("sending to " + user.getEmail());
				try {
					mailService.sendUnreadedMessageToIntitaUserFrom24Hours(user);
				} catch (Exception e) {
					log.info("sending failed: \n" + e.getMessage());
				}
			}
			currentPage++;
			if (currentPage<=pagesTotal)
				intitaUsersPage = userService.getChatUsers(currentPage, pageSize);
		}
	}

	boolean isEmailSendingRequired = true;

	@Scheduled(fixedDelay = 3600000L) // every 1 hour
	public void notificateUsersByEmail() {
		if (!sendUnreadedMessagesToEmail)return;
		Date date = new Date(); // given date
		Calendar calendar = GregorianCalendar.getInstance(); // creates a new
		// calendar
		// instance
		calendar.setTime(date); // assigns calendar to given date
		if (calendar.get(Calendar.HOUR_OF_DAY) >= 0 && calendar.get(Calendar.HOUR_OF_DAY) <= 4) {
			if (isEmailSendingRequired)
				sendAllNewMessageNotificationsFromLast24Hours();
			isEmailSendingRequired = false;
		} else
			isEmailSendingRequired = true;
	}
}