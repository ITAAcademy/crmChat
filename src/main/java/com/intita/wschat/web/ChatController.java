package com.intita.wschat.web;

import java.security.Principal;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.HashSet;
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

import org.hibernate.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpRequest;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.annotation.SubscribeMapping;
import org.springframework.scheduling.annotation.Scheduled;
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
import com.intita.wschat.domain.ChatMessage;
import com.intita.wschat.domain.SessionProfanity;
import com.intita.wschat.domain.UserWaitingForTrainer;
import com.intita.wschat.domain.interfaces.IPresentOnForum;
import com.intita.wschat.event.LoginEvent;
import com.intita.wschat.event.ParticipantRepository;
import com.intita.wschat.exception.ChatUserNotFoundException;
import com.intita.wschat.exception.ChatUserNotInRoomException;
import com.intita.wschat.exception.RoomNotFoundException;
import com.intita.wschat.models.ChatTenant;
import com.intita.wschat.models.ChatUser;
import com.intita.wschat.models.ChatUserLastRoomDate;
import com.intita.wschat.models.Course;
import com.intita.wschat.models.OperationStatus;
import com.intita.wschat.models.OperationStatus.OperationType;
import com.intita.wschat.models.Room;
import com.intita.wschat.models.RoomModelSimple;
import com.intita.wschat.models.User;
import com.intita.wschat.models.UserMessage;
import com.intita.wschat.services.ChatLangService;
import com.intita.wschat.services.ChatTenantService;
import com.intita.wschat.services.ChatUserLastRoomDateService;
import com.intita.wschat.services.ChatUsersService;
import com.intita.wschat.services.ConfigParamService;
import com.intita.wschat.services.CourseService;
import com.intita.wschat.services.IntitaMailService;
import com.intita.wschat.services.IntitaSubGtoupService;
import com.intita.wschat.services.RoomHistoryService;
import com.intita.wschat.services.RoomsService;
import com.intita.wschat.services.UserMessageService;
import com.intita.wschat.services.UsersService;
import com.intita.wschat.util.ProfanityChecker;
import com.intita.wschat.web.RoomController.SubscribedtoRoomsUsersBufferModal;

import jsonview.Views;

/**
 * Controller that handles WebSocket chat messages
 * 
 * @author Nicolas Haiduchok
 */
@Service
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

	private final Semaphore msgLocker = new Semaphore(1);

	@PersistenceContext
	EntityManager entityManager;

	protected Session getCurrentHibernateSession() {
		return entityManager.unwrap(Session.class);
	}

	private final static ObjectMapper mapper = new ObjectMapper();

	private volatile Map<String, Queue<UserMessage>> messagesBuffer = Collections
			.synchronizedMap(new ConcurrentHashMap<String, Queue<UserMessage>>());// key
																					// =>
																					// roomId
	private final Map<String, Queue<DeferredResult<String>>> responseBodyQueue = new ConcurrentHashMap<String, Queue<DeferredResult<String>>>();// key
																																				// =>
																																				// roomId

	private final ConcurrentHashMap<String, ArrayList<Object>> infoMap = new ConcurrentHashMap<>();
	private final ConcurrentHashMap<Long, ConcurrentHashMap<String, ArrayList<Object>>> infoMapForUser = new ConcurrentHashMap<>();

	ConcurrentHashMap<DeferredResult<String>, String> globalInfoResult = new ConcurrentHashMap<DeferredResult<String>, String>();
	private List<UserWaitingForTrainer> usersRequiredTrainers = new ArrayList<>();// RoomId,ChatUserId
																					// of
																					// tenatn

	public void tryRemoveChatUserRequiredTrainer(ChatUser chatUser) {
		UserWaitingForTrainer userWaiting = null;
		for (UserWaitingForTrainer user : usersRequiredTrainers) {
			if (Long.compare(user.getChatUserId(), chatUser.getId()) == 0) {
				userWaiting = user;
				break;
			}
		}
		if (userWaiting != null) {
			usersRequiredTrainers.remove(userWaiting);
			String subscriptionStr = "/topic/chat/room.private/room_require_trainer.remove";
			simpMessagingTemplate.convertAndSend(subscriptionStr, userWaiting.getChatUserId());
		}
	}

	public void removePrivateRoomRequiredTrainerFromList(Long roomId) {
		List<UserWaitingForTrainer> userWaitingRooms = new ArrayList<UserWaitingForTrainer>();
		for (UserWaitingForTrainer user : usersRequiredTrainers) {
			if (Long.compare(user.getChatUserId(), roomId) == 0) {
				userWaitingRooms.add(user);
			}
		}
		usersRequiredTrainers.removeAll(userWaitingRooms);
	}

	public void addFieldToInfoMap(String key, Object value) {
		ArrayList<Object> listElm = infoMap.get(key);
		if (listElm == null) {
			listElm = new ArrayList<>();
			infoMap.put(key, listElm);
		}
		listElm.add(value);
	}

	public boolean tryAddTenantInListToTrainerLP(Long chatUserId) {
		ChatUser chatUser = chatUsersService.getChatUser(chatUserId);
		return tryAddTenantInListToTrainerLP(chatUser);
	}

	public boolean tryAddTenantInListToTrainerLP(ChatUser chatUser) {
		User user = chatUser.getIntitaUser();
		if (user == null)
			return false;
		if (!chatTenantService.isTenant(user.getId())) {
			return false;
		}
		ArrayList<ChatUser> users = new ArrayList<ChatUser>();
		users.add(chatUser);
		propagateAdditionTenantsToList(users);
		return true;
	}

	public void groupCastAddTenantToList(ChatUser tenant) {
		String subscriptionStr = "/topic/chat.tenants.add";
		simpMessagingTemplate.convertAndSend(subscriptionStr, new LoginEvent(tenant));
	}

	public void groupCastRemoveTenantFromList(ChatUser tenant) {
		String subscriptionStr = "/topic/chat.tenants.remove";
		simpMessagingTemplate.convertAndSend(subscriptionStr, new LoginEvent(tenant));
	}

	public void propagateRemovingTenantFromListToTrainer(ChatUser user) {
		if (user == null)
			return;
		ArrayList<ChatUser> users = new ArrayList<ChatUser>();
		users.add(user);
		propagateRemovingTenantsFromList(users);
	}

	public void propagateAdditionTenantsToList(ArrayList<ChatUser> usersIds) {
		ArrayList<ChatUser> chatUsers = chatUsersService.getAllTrainers();
		if (usersIds.size() <= 0)
			return;

		for (ChatUser tenantUser : usersIds) {
			for (ChatUser trainerUser : chatUsers) {
				if (trainerUser == null)
					continue;
				Long trainerChatId = trainerUser.getId();
				if (participantRepository.isOnline(trainerChatId)) {
					ConcurrentHashMap<Long, IPresentOnForum> activeSessions = participantRepository.getActiveSessions();
					if (activeSessions.get(trainerChatId).isTimeBased())
						addFieldToUserInfoMap(trainerUser, "tenants.add", chatUsersService.getLoginEvent(tenantUser));
					else if (activeSessions.get(trainerChatId).isConnectionBased()) {
						// Do nothing now, but may be usable in future
					}
				}
			}
			groupCastAddTenantToList(tenantUser);
		}
	}

	public void propagateRemovingTenantsFromList(ArrayList<ChatUser> usersIds) {
		ArrayList<ChatUser> chatUsers = chatUsersService.getAllTrainers();
		if (usersIds.size() <= 0)
			return;
		ConcurrentHashMap<Long, IPresentOnForum> activeSessions = participantRepository.getActiveSessions();
		for (ChatUser tenantUser : usersIds) {
			for (ChatUser trainerUser : chatUsers) {
				if (trainerUser == null || !activeSessions.containsKey(trainerUser.getId().toString()))
					continue;
				Long trainerChatId = trainerUser.getId();
				if (!participantRepository.isOnline(trainerChatId)) {
					if (activeSessions.get(trainerChatId).isTimeBased())
						addFieldToUserInfoMap(trainerUser, "tenants.remove",
								chatUsersService.getLoginEvent(tenantUser));
					else if (activeSessions.get(trainerChatId).isConnectionBased()) {
						// Do nothing now, but may be usable in future
					}
				}
			}
			groupCastRemoveTenantFromList(tenantUser);
		}
	}

	public void addFieldToUserInfoMap(ChatUser user, String key, Object value) {
		if (user != null && !key.isEmpty())
			addFieldToUserInfoMap(user.getId(), key, value);
	}

	public void addFieldToUserInfoMap(Long userId, String key, Object value) {
		ConcurrentHashMap<String, ArrayList<Object>> t_infoMap = infoMapForUser.get(userId);
		if (t_infoMap == null) {
			t_infoMap = new ConcurrentHashMap<>();
			infoMapForUser.put(userId, t_infoMap);
		}

		ArrayList<Object> listElm = t_infoMap.get(userId);
		if (listElm == null) {
			listElm = new ArrayList<>();
			t_infoMap.put(key, listElm);
		}
		listElm.add(value);
		// log.info(String.format("field '%s' with value '%s' added to infomap
		// for user '%s'", key,value,userId));
	}

	public Map<String, Queue<UserMessage>> getMessagesBuffer() {
		return messagesBuffer;
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

	public static CurrentStatusUserRoomStruct isMyRoom(Long roomId, Principal principal, UsersService user_service,
			ChatUsersService chat_user_service, RoomsService chat_room_service) {
		long startTime = System.currentTimeMillis();
		Room o_room = chat_room_service.getRoom(roomId);
		if (o_room == null)
			return null;
		ChatUser o_user = chat_user_service.getChatUser(principal);
		if (o_user == null)
			return null;

		Set<Room> all = o_user.getRoomsFromUsers();
		all.addAll(o_user.getRootRooms());

		if (!all.contains(o_room))
			return null;

		return new CurrentStatusUserRoomStruct(o_user, o_room);
	}

	/********************
	 * GET CHAT USERS LIST FOR TEST
	 *******************/
	@RequestMapping(value = "/chat/users", method = RequestMethod.POST)
	@ResponseBody
	public String getUsers(Principal principal) throws JsonProcessingException {

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
	public ArrayList<ChatMessage> loadOtherMessageMapping(@PathVariable("room") Long room,
			@RequestBody Map<String, String> json, Principal principal) {
		return loadOtherMessage(room, json, principal, false);
	}

	@RequestMapping(value = "/{room}/chat/loadOtherMessageWithFiles", method = RequestMethod.POST)
	@ResponseBody
	public ArrayList<ChatMessage> loadOtherMessageWithFilesMapping(@PathVariable("room") Long room,
			@RequestBody(required = false) Map<String, String> json, Principal principal) {
		return loadOtherMessage(room, json, principal, true);
	}

	public ArrayList<ChatMessage> loadOtherMessage(Long room, Map<String, String> json, Principal principal,
			boolean filesOnly) {
		String dateMsStr = json.get("date");
		Long dateMs = null;
		if (dateMsStr != null && dateMsStr.length() > 0)
			dateMs = Long.parseLong(dateMsStr);
		Date date = (dateMs == null) ? null : new Date(dateMs);
		CurrentStatusUserRoomStruct struct = ChatController.isMyRoom(room, principal, userService, chatUsersService,
				chatRoomsService);// Control room from LP
		if (struct == null)
			throw new ChatUserNotInRoomException("");
		String searchQuery = json.get("searchQuery");
		ChatUser chatUser = chatUsersService.getChatUser(principal);
		Date clearDate = roomHistoryService.getHistoryClearDate(struct.getRoom().getId(), chatUser.getId());
		ArrayList<UserMessage> messages = userMessageService.getMessages(struct.getRoom().getId(), date, clearDate,
				searchQuery, filesOnly, 20);
		if (messages.size() == 0)
			return null;
		ArrayList<ChatMessage> messagesAfter = ChatMessage.getAllfromUserMessages(messages);

		if (messagesAfter.size() == 0)
			return null;

		return messagesAfter;
	}

	public UserMessage filterMessage(Long roomStr, ChatMessage message, Principal principal) {
		CurrentStatusUserRoomStruct struct = ChatController.isMyRoom(roomStr, principal, userService, chatUsersService,
				chatRoomsService);// Control room from LP
		if (struct == null || !struct.room.isActive() || message.getMessage().trim().isEmpty())// cant
																								// add
																								// msg
			return null;

		UserMessage messageToSave = new UserMessage(struct.user, struct.room, message);
		return messageToSave;
	}

	public UserMessage filterMessageWithoutFakeObj(ChatUser chatUser, ChatMessage message, Room room) {
		if (!room.isActive() || (message.getMessage().trim().isEmpty() && message.getAttachedFiles().isEmpty()))// cant
																												// add
																												// msg
			return null;

		UserMessage messageToSave = new UserMessage(chatUser, room, message);
		return messageToSave;
	}

	public synchronized void addMessageToBuffer(Long roomId, UserMessage message, ChatMessage cMessage) {
		synchronized (messagesBuffer) {
			Queue<UserMessage> list = messagesBuffer.get(roomId.toString());
			if (list == null) {
				list = new ConcurrentLinkedQueue<>();
				messagesBuffer.put(roomId.toString(), list);
			}
			list.add(message);
		}

		HashMap payload = new HashMap();
		payload.put(roomId, cMessage);
		Room chatRoom = chatRoomsService.getRoom(roomId);
		// send message to WS users
		for (ChatUser user : chatRoom.getUsers()) {
			simpMessagingTemplate.convertAndSend("/topic/" + user.getId() + "/must/get.room.num/chat.message", payload);
		}
		simpMessagingTemplate
				.convertAndSend("/topic/" + chatRoom.getAuthor().getId() + "/must/get.room.num/chat.message", payload);
		addFieldToInfoMap("newMessage", roomId);
	}

	private void addUserRequiredTrainer(Long roomId, ChatUser chatUserTrainer, ChatUser chatUser, String lastMessage) {

		LoginEvent studentLE = new LoginEvent(chatUser);
		UserWaitingForTrainer user = new UserWaitingForTrainer(roomId, lastMessage, studentLE);

		usersRequiredTrainers.add(user);

		simpMessagingTemplate.convertAndSend("/topic/chat/room.private/room_require_trainer.add", user);
		log.info("added room (" + roomId + ") required trainer " + chatUserTrainer.getId());
	}

	@SubscribeMapping("/chat/room.private/room_require_trainer")
	public List<UserWaitingForTrainer> retrievePrivateRoomsRequiredTrainersSubscribeMapping(Principal principal) {
		return usersRequiredTrainers;
	}

	@MessageMapping("/{room}/chat.message")
	public ChatMessage filterMessageWS(@DestinationVariable("room") Long roomId, @Payload ChatMessage message,
			Principal principal) {
		CurrentStatusUserRoomStruct struct = ChatController.isMyRoom(roomId, principal, userService, chatUsersService,
				chatRoomsService);// Control room from LP
		if (struct == null)
			return null;

		Long currentChatUserId = Long.parseLong(principal.getName());
		ChatUser user = chatUsersService.getChatUser(currentChatUserId); // chatUsersService.isMyRoom(roomStr,
																			// principal.getName());
		if (user == null)

			return null;

		Room o_room = struct.getRoom();
		UserMessage messageToSave = filterMessageWithoutFakeObj(user, message, o_room);// filterMessage(roomStr,
																						// message,
																						// principal);
		OperationStatus operationStatus = new OperationStatus(OperationType.SEND_MESSAGE_TO_ALL, true,
				"SENDING MESSAGE TO ALL USERS");
		String subscriptionStr = "/topic/users/" + principal.getName() + "/status";
		if (messageToSave != null) {
			ChatUser tenantIsWaitedByCurrentUser = chatRoomsService.isRoomHasStudentWaitingForTrainer(roomId,
					chatUsersService.getChatUser(principal));
			if (tenantIsWaitedByCurrentUser != null) {
				addUserRequiredTrainer(roomId, tenantIsWaitedByCurrentUser, user, message.getMessage());
			}
			addMessageToBuffer(roomId, messageToSave, message);

			simpMessagingTemplate.convertAndSend(subscriptionStr, operationStatus);
			return message;
		}
		simpMessagingTemplate.convertAndSend(subscriptionStr, operationStatus);
		return null;
	}

	@RequestMapping(value = "/{roomId}/chat/message", method = RequestMethod.POST)
	@ResponseBody
	public void filterMessageLP(@PathVariable("roomId") Long roomId, @RequestBody ChatMessage message,
			Principal principal) {
		// checkProfanityAndSanitize(message);//@NEED WEBSOCKET@
		UserMessage messageToSave = filterMessage(roomId, message, principal);
		ChatUser chatUser = chatUsersService.getChatUser(principal);
		if (messageToSave != null) {
			ChatUser trainerIsWaitedByCurrentUser = chatRoomsService.isRoomHasStudentWaitingForTrainer(roomId,
					chatUsersService.getChatUser(principal));
			if (trainerIsWaitedByCurrentUser != null) {
				addUserRequiredTrainer(roomId, trainerIsWaitedByCurrentUser, chatUser, message.getMessage());
			}
			addMessageToBuffer(roomId, messageToSave, message);
			simpMessagingTemplate.convertAndSend(("/topic/" + roomId.toString() + "/chat.message"), message);
		}
	}

	public void filterMessageBot(Long room, ChatMessage message, UserMessage to_save) {
		if (to_save != null) {
			addMessageToBuffer(room, to_save, message);
			simpMessagingTemplate.convertAndSend(("/topic/" + room.toString() + "/chat.message"), message);
		}
	}

	@RequestMapping(value = "/{room}/chat/message/update", method = RequestMethod.POST, produces = "application/json; charset=UTF-8")
	@ResponseBody
	public DeferredResult<String> updateMessageLP(@PathVariable("room") Long room) throws JsonProcessingException {
		Long timeOut = 60000L;
		DeferredResult<String> result = new DeferredResult<String>(timeOut, "{}");
		Queue<DeferredResult<String>> queue = responseBodyQueue.get(room.toString());
		if (queue == null) {
			queue = new ConcurrentLinkedQueue<DeferredResult<String>>();
			responseBodyQueue.put(room.toString(), queue);
		}
		queue.add(result);
		return result;
	}

	@Scheduled(fixedDelay = 600L)
	public void processMessage() {
		for (String roomId : messagesBuffer.keySet()) {
			Queue<UserMessage> array = messagesBuffer.get(roomId);
			Queue<DeferredResult<String>> responseList = responseBodyQueue.get(roomId);
			if (responseList != null) {
				String str = "";
				try {
					str = mapper.writeValueAsString(ChatMessage
							.getAllfromUserMessages(userMessageService.wrapBotMessages(new ArrayList<>(array), "ua")));// @BAG@//dont
																														// save
																														// user
																														// lang
																														// for
																														// bot
				} catch (JsonProcessingException e) {
					e.printStackTrace();
				}
				for (DeferredResult<String> response : responseList) {
					if (!response.isSetOrExpired())
						response.setResult(str);
				}
				responseList.clear();
			}

			boolean ok = userMessageService.addMessages(array);
			messagesBuffer.remove(roomId);
		}
	}

	@RequestMapping(value = "/chat/global/lp/info", method = RequestMethod.POST)
	@ResponseBody
	public DeferredResult<String> updateGlobalInfoLP(Principal principal) throws JsonProcessingException {
		int presenceIndexGrowth = 3;
		Long timeOut = 15000L;
		String chatIdStr = principal.getName();
		Long chatId = Long.parseLong(chatIdStr);
		participantRepository.addParticipantPresenceByLastConnectionTime(chatId);
		DeferredResult<String> result = new DeferredResult<String>(timeOut, "{}");
		globalInfoResult.put(result, principal.getName());
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
				propagateRemovingTenantsFromList(tenantsToRemove);
		}
	}

	@Scheduled(fixedDelay = 10000L)
	public void processGlobalInfo() {
		String result;
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

	// NOT TEST!!!
	@MessageMapping("/{room}/chat.private.{username}")
	public void filterPrivateMessage(@DestinationVariable Long room, @Payload ChatMessage message,
			@DestinationVariable("username") String username, Principal principal) {
		checkProfanityAndSanitize(message);
		Long chatUserId = 0L;
		chatUserId = Long.parseLong(principal.getName());
		message.setUsername(principal.getName());
		OperationStatus operationStatus = new OperationStatus(OperationType.SEND_MESSAGE_TO_USER, true,
				"SENDING MESSAGE TO USER");
		String subscriptionStr = "/topic/users/" + chatUserId + "/status";
		simpMessagingTemplate.convertAndSend(subscriptionStr, operationStatus);

		simpMessagingTemplate.convertAndSend("/user/" + username + "/exchange/amq.direct/" + room + "/chat.message",
				message);
	}

	/*
	 * Go into room
	 */

	@MessageMapping("/chat.go.to.dialog/{roomId}")
	public void userGoToDialogListener(@DestinationVariable("roomId") Long roomId, Principal principal) {
		// checkProfanityAndSanitize(message);
		CurrentStatusUserRoomStruct struct = isMyRoom(roomId, principal, userService, chatUsersService,
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
		
		Map <String, Object> result = new HashMap<>(); 
		result.put("roomId", roomId);
		result.put("chatUserId", user.getId());
		result.put("type", "roomRead");
		
		addFieldToInfoMap("roomRead", result);
		simpMessagingTemplate.convertAndSend("/topic/users/info", result);

		// return
		// chatRoomsService.getSimpleModelByUserPermissionsForRoom(struct.user,
		// 0 , new Date().toString(), room,
		// userMessageService.getLastUserMessageByRoom(room));
	}

	@RequestMapping(value = "/chat.go.to.dialog/{roomId}", method = RequestMethod.POST)
	@ResponseBody
	public void userGoToDialogListenerLP(@PathVariable("roomId") Long roomid, Principal principal) {
		userGoToDialogListener(roomid, principal);
	}

	@RequestMapping(value = "/chat/get_students/", method = RequestMethod.GET)
	@ResponseBody
	public Set<LoginEvent> getTrainerStudents(Principal principal) {
		ChatUser user = chatUsersService.getChatUser(principal);
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

	@RequestMapping(value = "/chat/get_students/{id}", method = RequestMethod.GET)
	@ResponseBody
	public Set<LoginEvent> getTrainerStudentsById(@PathVariable Long trainerId, Principal principal) {
		ChatUser user = chatUsersService.getChatUser(principal);
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
	 * Out from room
	 */
	@MessageMapping("/chat.go.to.dialog.list/{roomId}")
	public void userGoToDialogListListener(@DestinationVariable("roomId") Long roomId, Principal principal,
			@RequestBody Map<String, Object> params) {
		// checkProfanityAndSanitize(message);

		CurrentStatusUserRoomStruct struct = isMyRoom(roomId, principal, userService, chatUsersService,
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

		updateRoomsByUser(user, (HashMap<String, Object>) params.get("roomForUpdate"));

	}

	public void updateRoomsByUser(ChatUser user, HashMap<String, Object> roomsId) {
		ArrayList<Room> roomForUpdate = new ArrayList<>();

		// HashMap<String, Object> roomsId = (HashMap<String, Object>)
		// params.get("roomForUpdate");
		Set<String> set = roomsId.keySet();

		if (roomsId.size() > 0) {
			for (String string : set) {
				try {
					roomForUpdate.add(new Room(Long.parseLong(string)));
				} catch (Exception e) {
					System.out.println(e.getMessage());
				}
			}
		}
		sendMessageForUpdateRoomsByUser(user, roomForUpdate);
	}

	public void sendMessageForUpdateRoomsByUser(ChatUser user, ArrayList<Room> roomForUpdate) {
		RoomController
				.addFieldToSubscribedtoRoomsUsersBuffer(new SubscribedtoRoomsUsersBufferModal(user, roomForUpdate));
		simpMessagingTemplate.convertAndSend("/topic/chat/rooms/user." + user.getId(),
				new RoomController.UpdateRoomsPacketModal(
						chatRoomsService.getRoomsByChatUserAndList(user, roomForUpdate), false));
	}

	public void updateRoomByUser(ChatUser user, Room room) {
		ArrayList<Room> roomForUpdate = new ArrayList<>();
		roomForUpdate.add(room);
		sendMessageForUpdateRoomsByUser(user, roomForUpdate);
	}

	@RequestMapping(value = "/chat/update/dialog_list", method = RequestMethod.POST)
	@ResponseBody
	public void updateRoomsByList(Principal principal, @RequestBody Map<String, Object> params) {
		ChatUser user = chatUsersService.getChatUser(principal);
		if (user != null) {
			updateRoomsByUser(user, (HashMap<String, Object>) params.get("roomForUpdate"));
		}
	}

	@RequestMapping(value = "/chat.go.to.dialog.list/{roomId}", method = RequestMethod.POST)
	@ResponseBody
	public void userGoToDialogListListenerLP(@PathVariable("roomId") Long roomId, Principal principal,
			@RequestBody Map<String, Object> params) {

		userGoToDialogListListener(roomId, principal, params);
	}

	/*
	 * Work only on WS
	 */
	private void checkProfanityAndSanitize(ChatMessage message) {
		long profanityLevel = profanityFilter.getMessageProfanity(message.getMessage());
		profanity.increment(profanityLevel);
		message.setMessage(profanityFilter.filter(message.getMessage()));
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
			emails = userService.getUsersEmailsFist5(login, room_emails);
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
			usersResult = new ArrayList(userService.getUsersFist5(login, roomUsers));
		} else
			usersResult = new ArrayList(userService.getUsersFist5(login));
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
			usersResult = new ArrayList(userService.getUsersFist5(login, roomUsers));
		} else
			usersResult = new ArrayList(userService.getUsersFist5(login));
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
	public String getGroupRoomsByTrainer(@RequestParam(required = true) Long trainerChatId, Principal principal)
			throws JsonProcessingException {
		User intitaUser = userService.getUserFromChat(trainerChatId);
		ArrayList<Room> roomsResult = subGroupService.getTrainerGroupRooms(intitaUser.getId());
		ArrayList<RoomModelSimple> roomsModels = new ArrayList<RoomModelSimple>();
		for (Room room : roomsResult) {
			CurrentStatusUserRoomStruct struct = ChatController.isMyRoom(room.getId(), principal, userService,
					chatUsersService, chatRoomsService);
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
	public List<RoomModelSimple> getChatUsersLike(@RequestParam String query, Principal principal)
			throws JsonProcessingException {
		ChatUser chatUser = chatUsersService.getChatUser(principal);
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
	public Set<LoginEvent> getNickNamesLike2(@RequestParam String nickName) throws JsonProcessingException {
		List<ChatUser> nicks = chatUsersService.getChatUsersLike(nickName);
		Set<LoginEvent> usersData = new HashSet<LoginEvent>();
		for (ChatUser nick : nicks) {
			usersData.add(new LoginEvent(nick.getId(), nick.getNickName()));// participantRepository.isOnline(""+nick.getId())));
		}
		return usersData;
	}

	@RequestMapping(value = "/chatTemplate.html", method = RequestMethod.GET)
	public String getChatTemplate(HttpRequest request, Model model, Principal principal) {
		ChatUser user = chatUsersService.getChatUser(principal);
		User iUser = user.getIntitaUser();
		if (iUser != null) {
			Long intitaUserId = user.getIntitaUser().getId();
		}
		return commonController.getTeachersTemplate(request, "chatTemplate", model, principal);
	}

	@RequestMapping(value = "/get_room_messages", method = RequestMethod.GET)
	@ResponseBody
	public String getRoomMessages(@RequestParam Long roomId, Principal principal) throws JsonProcessingException {
		mapper.disable(MapperFeature.DEFAULT_VIEW_INCLUSION);
		boolean isAdmin = userService.isAdmin(Long.parseLong(principal.getName()));
		if (!isAdmin)
			return null;
		return mapper.writerWithView(Views.Public.class)
				.writeValueAsString(userMessageService.getUserMessagesByRoomId(roomId));
	}

	@RequestMapping(value = "/chat/room/{roomId}/get_messages_contains", method = RequestMethod.POST)
	@ResponseBody
	public ArrayList<ChatMessage> getRoomMessagesContains(@PathVariable("roomId") Long roomId,
			@RequestBody(required = false) String searchQuery, Principal principal) throws JsonProcessingException {
		ChatUser chatUser = chatUsersService.getChatUser(principal);
		Date clearDate = roomHistoryService.getHistoryClearDate(roomId, chatUser.getId());
		ArrayList<UserMessage> userMessages = userMessageService.getMessages(roomId, null, clearDate, searchQuery,
				false, 20);
		ArrayList<ChatMessage> chatMessages = ChatMessage.getAllfromUserMessages(userMessages);
		return chatMessages;
	}

	@RequestMapping(value = "/chat/room/{roomId}/clear_history", method = RequestMethod.POST)
	@ResponseBody
	public boolean clearRoomHistory(@PathVariable("roomId") Long roomId, Principal principal)
			throws JsonProcessingException {
		ChatUser chatUser = chatUsersService.getChatUser(principal);
		Room room = chatRoomsService.getRoom(roomId);
		// if (room.getAuthor().equals(chatUser))
		roomHistoryService.clearRoomHistory(room, chatUser);
		return true;
	}

	@RequestMapping(value = "/chat/user/send_new_messages_notification", method = RequestMethod.GET)
	@ResponseBody
	public boolean sendNewMessageNotifications(Principal principal) throws JsonProcessingException {
		User user = userService.getUser(principal);
		log.info("sending email to:" + user.getEmail());
		try {
			mailService.sendUnreadedMessageToIntitaUser(user);
		} catch (Exception e) {
			log.info("sending failed");
		}
		return true;
	}

	private void sendAllNewMessageNotificationsFromLast24Hours() {
		Set<ChatTenant> tenants = chatTenantService.getUniqueTenants();
		log.info("sending emails to users:");
		for (ChatTenant tenant : tenants) {
			User user = tenant.getChatUser().getIntitaUser();
			log.info("sending to " + user.getEmail());
			try {
				mailService.sendUnreadedMessageToIntitaUser(user);
			} catch (Exception e) {
				log.info("sending failed: \n" + e.getMessage());
			}
		}

	}

	boolean isEmailSendingRequired = true;

	@Scheduled(fixedDelay = 3600000L) // every 1 hour
	public void notificateUsersByEmail() {
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