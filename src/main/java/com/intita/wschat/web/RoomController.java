package com.intita.wschat.web;

import java.io.Serializable;
import java.security.Principal;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletRequest;

import org.hibernate.bytecode.buildtime.spi.ExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpRequest;
import org.springframework.messaging.MessageDeliveryException;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageExceptionHandler;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.annotation.SendToUser;
import org.springframework.messaging.simp.annotation.SubscribeMapping;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.context.request.async.DeferredResult;

import com.fasterxml.jackson.annotation.JsonView;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.intita.wschat.config.FlywayMigrationStrategyCustom;
import com.intita.wschat.domain.ChatMessage;
import com.intita.wschat.domain.SessionProfanity;
import com.intita.wschat.event.LoginEvent;
import com.intita.wschat.event.ParticipantRepository;
import com.intita.wschat.exception.ChatUserNotInRoomException;
import com.intita.wschat.exception.RoomNotFoundException;
import com.intita.wschat.exception.TooMuchProfanityException;
import com.intita.wschat.models.BotCategory;
import com.intita.wschat.models.BotDialogItem;
import com.intita.wschat.models.ChatUser;
import com.intita.wschat.models.ConfigParam;
import com.intita.wschat.models.OperationStatus;
import com.intita.wschat.models.OperationStatus.OperationType;
import com.intita.wschat.models.PrivateRoomInfo;
import com.intita.wschat.models.Room;
import com.intita.wschat.models.Room.RoomType;
import com.intita.wschat.models.RoomModelSimple;
import com.intita.wschat.models.RoomPermissions;
import com.intita.wschat.models.User;
import com.intita.wschat.models.UserMessage;
import com.intita.wschat.services.BotCategoryService;
import com.intita.wschat.services.ChatLangService;
import com.intita.wschat.services.ChatTenantService;
import com.intita.wschat.services.ChatUserLastRoomDateService;
import com.intita.wschat.services.ChatUsersService;
import com.intita.wschat.services.ConfigParamService;
import com.intita.wschat.services.ConsultationsService;
import com.intita.wschat.services.LecturesService;
import com.intita.wschat.services.OfflineStudentsGroupService;
import com.intita.wschat.services.RoomPermissionsService;
import com.intita.wschat.services.RoomsService;
import com.intita.wschat.services.UserMessageService;
import com.intita.wschat.services.UsersService;
import com.intita.wschat.services.ChatLangService.ChatLangEnum;
import com.intita.wschat.util.ProfanityChecker;
import com.intita.wschat.web.BotController.BotParam;
import com.intita.wschat.web.ChatController.CurrentStatusUserRoomStruct;
import com.intita.wschat.web.RoomController.ROLE;
import com.intita.wschat.web.RoomController.UpdateRoomsPacketModal;

import jsonview.Views;
//import scala.annotation.meta.setter;

@Controller
public class RoomController {
	final String DIALOG_NAME_PREFIX = "DIALOG_";
	private final static Logger log = LoggerFactory.getLogger(RoomController.class);

	@Autowired(required = true)
	private HttpServletRequest request;

	@Autowired
	private ProfanityChecker profanityFilter;

	@Autowired
	private SessionProfanity profanity;

	@Autowired
	private ParticipantRepository participantRepository;
	@Autowired
	private LecturesService lecturesService;

	@Autowired
	private SimpMessagingTemplate simpMessagingTemplate;
	@Autowired
	private ConsultationsService chatIntitaConsultationService;

	@Autowired
	private RoomsService roomService;
	@Autowired
	private ChatUserLastRoomDateService chatLastRoomDateService;
	@Autowired
	private UsersService userService;
	@Autowired private UserMessageService userMessageService;
	@Autowired private ChatUsersService chatUserServise;
	@Autowired private ChatTenantService chatTenantService;
	@Autowired private ChatUserLastRoomDateService chatUserLastRoomDateService;
	// @Autowired private IntitaConsultationsService
	// chatIntitaConsultationService;
	@Autowired private ConfigParamService configService;
	@Autowired private ChatController chatController;

	@Autowired private BotCategoryService botCategoryService;
	@Autowired private BotController botController;
	@Autowired private FlywayMigrationStrategyCustom flyWayStategy;
	@Autowired private RoomPermissionsService roomPermissionsService;
	@Autowired private OfflineStudentsGroupService offlineStudentsGroupService;
	@Autowired private ChatLangService chatLangService;

	public static class ROLE {
		public static final int ADMIN = 256;
	}

	static final private ObjectMapper mapper = new ObjectMapper();

	private static final Queue<SubscribedtoRoomsUsersBufferModal> subscribedtoRoomsUsersBuffer = new ConcurrentLinkedQueue<SubscribedtoRoomsUsersBufferModal>();// key
	// =>
	// roomId
	private final Map<Long, ConcurrentLinkedQueue<DeferredResult<String>>> responseRoomBodyQueue = new ConcurrentHashMap<Long, ConcurrentLinkedQueue<DeferredResult<String>>>();// key
	// =>
	// roomId

	private ArrayList<Room> roomsArray;

	private final Map<String, Queue<DeferredResult<String>>> responseBodyQueueForParticipents = new ConcurrentHashMap<String, Queue<DeferredResult<String>>>();// key
	// =>
	// roomId

	@PostConstruct
	private void postFunction() {
		// configService.getParam("chatBotEnable");
	}

	@RequestMapping(value = "/chat/rooms/create/with_bot/", method = RequestMethod.POST)
	@ResponseBody
	public void createDialogWithBotRequesr(@RequestBody String roomName, Principal principal) {
		createDialogWithBot(roomName, principal);
	}

	public Room createDialogWithBot(String roomName, Principal principal) {
		if (roomName.isEmpty())
			return null;

		ChatUser bot = chatUserServise.getChatUser(BotParam.BOT_ID);

		Room room = roomService.register(roomName, bot);
		ChatUser guest = chatUserServise.getChatUser(principal);
		roomService.addUserToRoom(guest, room);

		// send to user about room apearenced
		Long chatUserId = Long.parseLong(principal.getName());
		simpMessagingTemplate.convertAndSend("/topic/chat/rooms/user." + chatUserId,
				getRoomsByAuthorSubscribe(principal, Long.parseLong(principal.getName())));
		// this said ti author that he nust update room`s list
		addFieldToSubscribedtoRoomsUsersBuffer(new SubscribedtoRoomsUsersBufferModal(guest));

		String containerString = "Good day. Please choose the category that interests you:\n";
		ArrayList<BotCategory> allCategories = botCategoryService.getAll();
		BotDialogItem mainContainer = BotDialogItem.createFromCategories(allCategories);
		mainContainer.setBody(containerString + mainContainer.getBody());
		try {
			containerString = mapper.writeValueAsString(mainContainer);
		} catch (JsonProcessingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		UserMessage msg = new UserMessage(bot, room, containerString);
		chatController.filterMessageWS(room.getId(), new ChatMessage(msg), BotParam.getBotPrincipal());

		return room;
	}

	public Room createRoomWithTenant(Principal principal) {

		// boolean botEnable =
		// Boolean.parseBoolean(configService.getParam("botEnable").getValue());
		/*
		 * ChatTenant greeTenante = chatTenantService.getFreeTenant(); if
		 * (greeTenante == null) return null;
		 * 
		 * ChatUser roomAuthor = greeTenante.getChatUser();
		 * 
		 * chatTenantService.setTenantBusy(greeTenante);
		 */
		// getRandomTenant().getChatUser();
		ChatUser guest = chatUserServise.getChatUser(principal);
		String roomName = " " + guest.getNickName().substring(0, 16) + " " + new Date().toString();
		Room room = roomService.register(roomName, guest);

		// roomService.addUserToRoom(guest, room);

		// send to user about room apearenced
		Long chatUserId = Long.parseLong(principal.getName());
		simpMessagingTemplate.convertAndSend("/topic/chat/rooms/user." + chatUserId,
				getRoomsByAuthorSubscribe(principal, Long.parseLong(principal.getName())));
		// this said ti author that he nust update room`s list
		addFieldToSubscribedtoRoomsUsersBuffer(new SubscribedtoRoomsUsersBufferModal(guest));
		botController.register(room, chatUserId);
		botController.runUsersAskTenantsTimer(room);
		return room;
	}

	/**********************
	 * what doing with new auth user
	 **********************/
	@SubscribeMapping("/chat.login")
	public Map<String, String> login(Principal principal) {// Control user page
		// after auth
		return login(principal, null);
	}

	@SubscribeMapping("/chat.login/{demandedChatUserId}")
	public Map<String, String> login(Principal principal, @DestinationVariable Long demandedChatUserId)// Control
	// user
	// page
	// after
	// auth
	{
		Map<String, String> result = new HashMap<>();
		demandedChatUserId = (long) 48;
		// if (demandedChatUserId != null && demandedChatUser==null) return
		// null;//return null if demanded user is not excist

		Long realChatUserId = Long.parseLong(principal.getName());
		ChatUser realChatUser = chatUserServise.getChatUser(realChatUserId);
		User realIntitaUser = realChatUser.getIntitaUser();
		ChatUser activeChatUser = demandedChatUserId == null ? realChatUser : chatUserServise.getChatUser(demandedChatUserId);

		if (activeChatUser == null || realChatUserId.longValue() != activeChatUser.getId().longValue()) {
			// ChatUser user_real =
			// chatUserServise.getChatUser(Long.parseLong(principal.getName()));
			if (realIntitaUser == null || !userService.isAdmin(realIntitaUser.getId()))
				return null;
		}
		User activeIntitaUser = activeChatUser.getIntitaUser();

		if (realIntitaUser == null) {
			Room room;
			if (activeChatUser.getChatUserLastRoomDate().iterator().hasNext()) {
				room = realChatUser.getChatUserLastRoomDate().iterator().next().getLastRoom();
			} else {
				/*
				 * ADD BOT TO CHAT
				 */
				// boolean botEnable =
				// Boolean.parseBoolean(configService.getParam("botEnable").getValue());
				boolean botEnable = true;
				ConfigParam s_botEnable = configService.getParam("chatBotEnable");
				if (s_botEnable != null)
					botEnable = Boolean.parseBoolean(s_botEnable.getValue());
				if (botEnable) {
					room = createDialogWithBot("BotSys_" + realChatUser.getId() + "_" + new Date().toString(),
							principal);
				} else
					room = createRoomWithTenant(principal);

				// test - no free tenant
				// room = null;

				// send msg about go to guest room if you is tenant with current
				// Id
				/*
				 * if (room != null)
				 * chatController.addFieldToInfoMap("newGuestRoom",
				 * room.getId());
				 */
			}
			// simpMessagingTemplate.convertAndSend("/topic/chat/rooms/user." +
			// user.getId(), roomService.getRoomsModelByChatUser(user));

			if (room != null)
				result.put("nextWindow", room.getId().toString());
			else
				result.put("nextWindow", "-1");
		} else {
			// subscribedtoRoomsUsersBuffer.add(user);
			result.put("nextWindow", "0");
			result.put("chat_user_avatar", activeIntitaUser == null ? "noname.png" : activeIntitaUser.getAvatar());
		}
		String activeUsersJson = null;
		Set<Long> activeUsers = participantRepository.getActiveUsers();
		try {
			activeUsersJson = mapper.writeValueAsString(activeUsers);
		} catch (JsonProcessingException e) {
			e.printStackTrace();
		}
		result.put("onlineUsersIdsJson", activeUsersJson);
		result.put("chat_id", activeChatUser.getId().toString());
		result.put("chat_user_nickname", activeChatUser.getNickName());

		Integer role = 0;
		if (activeIntitaUser != null) {
			if (userService.isAdmin(activeIntitaUser.getId())) {
				result.put("isAdmin", "true");
				role |= ROLE.ADMIN;
			} else
				result.put("isAdmin", "false");
			// check if tenant
			if (userService.isTenant(activeIntitaUser.getId()))
				result.put("isTenant", "true");
			else
				result.put("isTenant", "false");
			// check if trainer
			if (userService.isTrainer(activeIntitaUser.getId()))
				result.put("isTrainer", "true");
			else
				result.put("isTrainer", "false");

			if (userService.isStudent(activeIntitaUser.getId())) {
				result.put("isStudent", "true");
				User iTrainer = userService.getTrainer(activeIntitaUser.getId());
				result.put("trainer", null);
				if (iTrainer != null) {
					ChatUser chatTrainer = chatUserServise.getChatUserFromIntitaUser(iTrainer, false);
					if (chatTrainer != null)
						try {
							result.put("trainer",
									mapper.writeValueAsString(chatUserServise.getLoginEvent(chatTrainer)));
						} catch (JsonProcessingException e) {
							e.printStackTrace();
						}
				}
			} else
				result.put("isStudent", "false");
		}
		result.put("chat_user_role", role.toString());

		String rooms = "{}";
		try {
			rooms = mapper.writeValueAsString(
					new UpdateRoomsPacketModal(roomService.getRoomsModelByChatUser(activeChatUser)));
		} catch (JsonProcessingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		result.put("chat_rooms", rooms);
		Long activeIntitaUserId = null == activeIntitaUser ? null : activeIntitaUser.getId();
		/***
		 * @deprecated try { result.put("friends",
		 *             mapper.writeValueAsString(roomService.getPrivateLoginEvent(user)));
		 *             } catch (JsonProcessingException e1) { // TODO
		 *             Auto-generated catch block e1.printStackTrace(); }
		 */
		if (userService.isTrainer(activeIntitaUserId)) {
			ArrayList<LoginEvent> tenantsObjects = userService.getAllFreeTenantsLoginEvent(activeChatUser.getId());
			String tenantsJson = null;
			try {
				tenantsJson = mapper.writeValueAsString(tenantsObjects);
			} catch (JsonProcessingException e) {
				e.printStackTrace();
			}
			result.put("tenants", tenantsJson);
		}

		return result;
	}

	@RequestMapping(value = "/chat/login/{userId}", method = RequestMethod.POST)
	@ResponseBody
	public String retrieveParticipantsLP(Principal principal, @PathVariable("userId") Long userId)
			throws JsonProcessingException {
		if(userId == -1)
			userId = null;
		return mapper.writeValueAsString(login(principal, userId));
	}

	/***************************
	 * GET PARTICIPANTS AND LOAD MESSAGE
	 ***************************/

	private Set<LoginEvent> GetParticipants(Room room_o) {
		Set<LoginEvent> userList = new HashSet<>();
		Long intitaId = null;
		String avatar = "noname.png";
		User iUser = room_o.getAuthor().getIntitaUser();
		if (iUser != null) {
			intitaId = iUser.getId();
			avatar = iUser.getAvatar();
		}

		LoginEvent currentChatUserLoginEvent = new LoginEvent(intitaId, room_o.getAuthor().getId(),
				room_o.getAuthor().getNickName(), avatar);// participantRepository.isOnline(room_o.getAuthor().getId().toString())
		userList.add(currentChatUserLoginEvent);
		for (ChatUser user : room_o.getUsers()) {

			intitaId = null;
			avatar = "noname.png";
			iUser = user.getIntitaUser();
			// Bot avatar
			if (user.getId() == BotParam.BOT_ID)
				avatar = BotParam.BOT_AVATAR;

			if (iUser != null) {
				intitaId = iUser.getId();
				avatar = iUser.getAvatar();
			}

			userList.add(new LoginEvent(intitaId, user.getId(), user.getNickName(), avatar)); // participantRepository.isOnline(user.getId().toString())));
		}
		return userList;
	}

	public Map<String, Object> retrieveParticipantsSubscribeAndMessagesObj(Room room_o, String lang, ChatUser user) {

		Queue<UserMessage> buff = chatController.getMessagesBuffer().get(room_o.getId());
		ArrayList<UserMessage> userMessages = userMessageService.getFirst20UserMessagesByRoom(room_o, lang,user);
		if (buff != null)
			userMessages.addAll(buff);
		ArrayList<ChatMessage> messagesHistory = ChatMessage.getAllfromUserMessages(userMessages);

		HashMap<String, Object> map = new HashMap();
		map.put("participants", GetParticipants(room_o));
		map.put("messages", messagesHistory);
		map.put("type", room_o.getType());// 0-add; 1-private; 2-not my
		if(userMessages.isEmpty() == false)
			map.put("lastNonUserActivity", roomService.getLastMsgActivity(room_o, userMessages.get(0)));// 
		try {
			map.put("bot_param", mapper.writerWithView(Views.Public.class).writeValueAsString(room_o.getBotAnswers()));
		} catch (JsonProcessingException e) {
			// TODO Auto-generated catch block
			log.info("BOT PARAM PROBLEM: " + room_o.getBotAnswers());
			e.printStackTrace();

		} // 0-add; 1-private; 2-not my
		return map;
	}

	@SubscribeMapping("/{room}/chat.participants/{lang}")
	public Map<String, Object> retrieveParticipantsSubscribeAndMessages(@DestinationVariable("room") Long room,
			@DestinationVariable("lang") String lang, SimpMessageHeaderAccessor headerAccessor, Principal principal) {// ONLY
		// FOR
		// TEST
		// NEED
		// FIX
		CurrentStatusUserRoomStruct status = ChatController.isMyRoom(room, principal, userService, chatUserServise,
				roomService);
		ChatUser o_object = chatUserServise.getChatUser(principal);
		if (status == null) {
			if (o_object != null) {
				User iUser = o_object.getIntitaUser();

				if (iUser == null || !userService.isAdmin(iUser.getId())) {
					return new HashMap<String, Object>();
				}
			} else
				return new HashMap<String, Object>();
		}

		Room room_o = roomService.getRoom(room);
		return retrieveParticipantsSubscribeAndMessagesObj(room_o, lang,o_object);
	}

	@MessageMapping("/{room}/chat.participants")
	public Map<String, Object> retrieveParticipantsMessage(@DestinationVariable Long room) {
		Room room_o = roomService.getRoom(room);
		HashMap<String, Object> map = new HashMap();
		if (room_o != null)
			map.put("participants", GetParticipants(room_o));
		return map;
	}

	@SubscribeMapping("/chat.tenants")
	public ArrayList<LoginEvent> retrieveTenants(Principal principal) {
		ChatUser currentChatUser = chatUserServise.getChatUser(principal);
		ArrayList<LoginEvent> loginEvents = userService.getAllFreeTenantsLoginEvent();
		return loginEvents;
	}

	@MessageMapping("/chat.tenants")
	public ArrayList<LoginEvent> retrieveTenantsMesageMapping(Principal principal) {
		ChatUser currentChatUser = chatUserServise.getChatUser(principal);
		ArrayList<LoginEvent> loginEvents = userService.getAllFreeTenantsLoginEvent();
		return loginEvents;
	}

	@RequestMapping(value = "/{room}/chat/participants_and_messages", method = RequestMethod.POST)
	@ResponseBody
	public String retrieveParticipantsAndMessagesLP(@PathVariable("room") Long room, Principal principal)
			throws JsonProcessingException {
		CurrentStatusUserRoomStruct struct = ChatController.isMyRoom(room, principal, userService, chatUserServise,
				roomService);// Control room from LP
		if (struct == null)
			return "{}";
		ChatUser chatUser = chatUserServise.getChatUser(principal);
		String participantsAndMessages = mapper.writeValueAsString(
				retrieveParticipantsSubscribeAndMessagesObj(struct.getRoom(), chatLangService.getCurrentLang(),chatUser));

		return participantsAndMessages;
	}

	@RequestMapping(value = "/{room}/chat/set_name", method = RequestMethod.POST, produces = "text/plain;charset=UTF-8")
	@ResponseBody
	public String setRoomName(@PathVariable("room") Long roomId, @RequestParam String newName, Principal principal)
			throws JsonProcessingException {
		Room room = roomService.getRoom(roomId);

		if(room == null)
			throw new NullPointerException();

		log.info(room.getAuthor().getId().toString());
		if(!(chatUserServise.getChatUser(principal) == room.getAuthor()))
			throw new ExecutionException("Non author try change room name!!!");

		String nameAfterChanging = roomService.updateRoomName(room, newName);
		log.info("Room name changed:" + newName);
		return nameAfterChanging;
	}

	@RequestMapping(value = "/{room}/chat/participants/update", method = RequestMethod.POST, produces = "text/plain;charset=UTF-8")
	@ResponseBody
	public DeferredResult<String> retrieveParticipantsUpdateLP(@PathVariable("room") Long room, Principal principal)
			throws JsonProcessingException {

		Long timeOut = 5000L;
		DeferredResult<String> result = new DeferredResult<String>(timeOut, "{}");
		Queue<DeferredResult<String>> queue = responseBodyQueueForParticipents.get(room);
		if (queue == null) {
			queue = new ConcurrentLinkedQueue<DeferredResult<String>>();
		}
		CurrentStatusUserRoomStruct struct = ChatController.isMyRoom(room, principal, userService, chatUserServise,
				roomService);// Control room from LP
		if (struct != null) {
			responseBodyQueueForParticipents.put(room.toString(), queue);
			queue.add(result);
		} else
			result.setErrorResult(new ChatUserNotInRoomException(""));

		return result;
	}

	/*
	 * call only if is need
	 */
	@Scheduled(fixedDelay = 15000L)
	public void updateParticipants() {
		for (String key : responseBodyQueueForParticipents.keySet()) {
			Long longKey = 0L;
			boolean status = true;
			try {
				longKey = Long.parseLong(key);
			} catch (NumberFormatException e) {
				log.info("Participants update error:" + e.getMessage());
				status = false;
			}
			Room room_o = null;
			HashMap<String, Object> result = null;
			if (status) {
				room_o = roomService.getRoom(longKey);
				result = new HashMap();
			}
			if (room_o != null)
				result.put("participants", GetParticipants(room_o));

			for (DeferredResult<String> response : responseBodyQueueForParticipents.get(key)) {
				// response.setResult("");
				try {
					if (!response.isSetOrExpired())
						response.setResult(mapper.writeValueAsString(result));
					else
						response.setResult("{}");
				} catch (JsonProcessingException e) {
					// TODO Auto-generated catch block
					response.setResult("");
					e.printStackTrace();
				}

			}
			responseBodyQueueForParticipents.remove(key);
		}

	}

	/***************************
	 * GET/ADD ROOMS
	 *
	 * @throws JsonProcessingException
	 ***************************/

	@RequestMapping(value = "/chat/rooms/roomInfo/{roomID}", method = RequestMethod.POST)
	@ResponseBody
	public String getRoomInfo(@PathVariable("roomID") Long roomId, Principal principal) throws JsonProcessingException {
		CurrentStatusUserRoomStruct struct = ChatController.isMyRoom(roomId, principal, userService, chatUserServise,
				roomService);// Control room from LP
		if (struct == null)
			return "{}";
		RoomModelSimple sb = roomService.getSimpleModelByUserPermissionsForRoom(struct.getUser(), 0,
				new Date().toString(), struct.getRoom(), userMessageService.getLastUserMessageByRoom(struct.getRoom()));
		return mapper.writeValueAsString(sb);
	}

	/*********************
	 * Generation/return of new special room for two users Tenatn/Trainer more
	 * prefered as authoror
	 *
	 * @return id of room
	 * @exception RoomNotFoundException
	 *                (users not founded)
	 ********************/
	public Room getPrivateRoom(ChatUser chatUser, ChatUser privateCharUser) {
		if (privateCharUser == null || chatUser == null)
			throw new RoomNotFoundException("privateChatUser or chatUser is null");

		if (chatUser.getIntitaUser() == null || privateCharUser.getIntitaUser() == null)
			throw new RoomNotFoundException("Intita use is null");

		Room room = roomService.getPrivateRoom(chatUser, privateCharUser);

		ChatUser author = chatUser;
		ChatUser other = privateCharUser;
		User iPrivateUser = privateCharUser.getIntitaUser();
		if (userService.isTenant(iPrivateUser.getId()) || userService.isTrainer(iPrivateUser.getId())) {
			author = privateCharUser;
			other = chatUser;
		}

		if (room == null) {
			room = roomService.registerPrivate(author, other);// private room
			// type => 1

			simpMessagingTemplate.convertAndSend("/topic/chat/rooms/user." + chatUser.getId(),
					new UpdateRoomsPacketModal(roomService.getRoomsModelByChatUser(chatUser)));
			if (chatUser.getId() != privateCharUser.getId())
				simpMessagingTemplate.convertAndSend("/topic/chat/rooms/user." + privateCharUser.getId(),
						new UpdateRoomsPacketModal(roomService.getRoomsModelByChatUser(privateCharUser)));

			OperationStatus operationStatus = new OperationStatus(OperationType.ADD_ROOM_FROM_TENANT, true,
					"" + room.getId());
			String subscriptionStr = "/topic/users/" + chatUser.getId() + "/status";
			simpMessagingTemplate.convertAndSend(subscriptionStr, operationStatus);
		}
		return room;
	}

	@RequestMapping(value = "/chat/get/rooms/private/{userID}", method = { RequestMethod.GET, RequestMethod.POST })
	@ResponseBody
	public Long getPrivateRoomRequest(@PathVariable("userID") Long userId,
			@RequestParam(required = false, name = "isChatId", defaultValue = "false") Boolean isChatId,
			Principal principal) throws JsonProcessingException {
		log.info("getPrivateRoom");
		ChatUser privateCharUser;
		if (isChatId)
			privateCharUser = chatUserServise.getChatUser(userId);
		else
			privateCharUser = chatUserServise.getChatUserFromIntitaId(userId, false);
		ChatUser chatUser = chatUserServise.getChatUser(principal);
		Room room = getPrivateRoom(chatUser, privateCharUser);
		return room.getId();// @BAG@
	}

	@RequestMapping(value = "/chat/go/rooms/private/trainer", method = RequestMethod.GET)
	public String goPrivateRoomWithTrainer(Principal principal) throws JsonProcessingException {
		ChatUser principalChatUser = chatUserServise.getChatUser(principal);

		User iPrincipalUser = principalChatUser.getIntitaUser();
		ChatUser trainer = null;
		try {
			if (iPrincipalUser == null)
				throw new RoomNotFoundException("is west!!!");
			User iTrainer = userService.getTrainer(iPrincipalUser.getId());
			if (iTrainer == null)
				throw new RoomNotFoundException("user dont have trainer!!!");
			trainer = chatUserServise.getChatUserFromIntitaUser(iTrainer, false);
			return "redirect:/#/dialog_view/" + getPrivateRoom(trainer, principalChatUser).getId();
		} catch (RoomNotFoundException ex) {
			log.info("goPrivateRoomWithTrainer ::: " + ex.getMessage());
			return "redirect:/";
		}
	}

	@RequestMapping(value = "/chat/go/rooms/private/{userId}", method = RequestMethod.GET)
	public String goPrivateRoom(@PathVariable Long userId,
			@RequestParam(required = false, name = "isChatId", defaultValue = "false") Boolean isChatId,
			Principal principal) throws JsonProcessingException {
		Long id = getPrivateRoomRequest(userId, isChatId, principal);
		return "redirect:/#/dialog_view/" + id;
	}

	// @SubscribeMapping("/chat/rooms/user.{userId}")
	public UpdateRoomsPacketModal getRoomsByAuthorSubscribe(Principal principal, @DestinationVariable Long userId) { // 000
		ChatUser user = chatUserServise.getChatUser(userId);

		if (user == null || Long.parseLong(principal.getName()) != user.getId().longValue()) {
			ChatUser user_real = chatUserServise.getChatUser(Long.parseLong(principal.getName()));
			if (user_real.getIntitaUser() == null || !userService.isAdmin(user_real.getIntitaUser().getId()))
				return null;
		}

		return new UpdateRoomsPacketModal(roomService.getRoomsModelByChatUser(user));
	}

	// LONG POLLING PART

	@RequestMapping(value = "/chat/rooms/adddialogwithuser", method = RequestMethod.POST)
	@ResponseBody
	public Long addDialog(Principal principal, @RequestBody Long chatUserId) {
		ChatUser auth = chatUserServise.getChatUser(principal);
		if (auth == null)
			return -1L;
		User authorOfDialog = auth.getIntitaUser();
		if (authorOfDialog == null)
			return -1L;
		User interlocutor = userService.getUserFromChat(chatUserId);
		if (interlocutor == null)
			return -1L;
		String roomName = DIALOG_NAME_PREFIX + authorOfDialog.getLogin() + "_" + interlocutor.getLogin();
		Room room = roomService.register(roomName, auth);
		return room.getId();
	}

	@RequestMapping(value = "/chat/rooms/user/{username}", method = RequestMethod.POST)
	@ResponseBody
	public DeferredResult<String> getRooms(Principal principal) {
		if (principal == null)
			return null;
		Long timeOut = 1000000L;
		DeferredResult<String> deferredResult = new DeferredResult<String>(timeOut, "NULL");
		Long chatUserId = Long.parseLong(principal.getName());

		ConcurrentLinkedQueue<DeferredResult<String>> queue = responseRoomBodyQueue.get(chatUserId);
		if (queue == null) {
			queue = new ConcurrentLinkedQueue<DeferredResult<String>>();
		}

		responseRoomBodyQueue.put(chatUserId, queue);
		queue.add(deferredResult);
		// System.out.println("responseRoomBodyQueue
		// queue_count:"+queue.size());

		return deferredResult;
	}

	@Scheduled(fixedDelay = 2500L)
	public void processRoomsQueues() throws JsonProcessingException {
		for (SubscribedtoRoomsUsersBufferModal modal : subscribedtoRoomsUsersBuffer) {
			if (modal == null || modal.chatUser == null) {
				System.out.println("WARNING: NULL USER");
				continue;
			}
			Queue<DeferredResult<String>> responseList = responseRoomBodyQueue.get(modal.chatUser.getId());
			if (responseList == null) {
				// System.out.println("WARNING: RESPONSE LIST IS CLEAR");
				continue;
			}
			for (DeferredResult<String> response : responseList) {

				String str;
				if (modal.replace)
					str = mapper.writeValueAsString(new UpdateRoomsPacketModal(
							roomService.getRoomsModelByChatUser(modal.chatUser), modal.replace));
				else
					str = mapper.writeValueAsString(new UpdateRoomsPacketModal(
							roomService.getRoomsByChatUserAndList(modal.chatUser, modal.roomsForUpdate),
							modal.replace));

				if (!response.isSetOrExpired())
					response.setResult(str);
			}
			responseRoomBodyQueue.remove(modal.chatUser.getId());
			subscribedtoRoomsUsersBuffer.remove(modal);
		}
	}

	@RequestMapping(value = "/chat/rooms/add", method = RequestMethod.POST)
	@ResponseBody
	// @SendToUser(value = "/exchange/amq.direct/errors", broadcast = false)
	public Long addRoomByAuthorLP(@RequestParam(name = "name") String roomName,
			@RequestBody(required = false) ArrayList<Long> userIds, Principal principal) {
		boolean operationSuccess = true;
		Room room = null;
		Long chatUserId = Long.parseLong(principal.getName());
		ChatUser author = chatUserServise.getChatUser(chatUserId);
		ArrayList<ChatUser> users = chatUserServise.getUsers(userIds);
		if (userIds.size() == users.size())
			room = roomService.register(roomName, author, users);

		if (room == null)
			operationSuccess = false;
		else {
			// users.add(author);
			simpMessagingTemplate.convertAndSend("/topic/chat/rooms/user." + author.getId(),
					new UpdateRoomsPacketModal(roomService.getRoomsModelByChatUser(author)));
			addFieldToSubscribedtoRoomsUsersBuffer(new SubscribedtoRoomsUsersBufferModal(author));

			for (ChatUser chatUser : users) {
				simpMessagingTemplate.convertAndSend("/topic/chat/rooms/user." + chatUser.getId(),
						new UpdateRoomsPacketModal(roomService.getRoomsModelByChatUser(chatUser)));
				addFieldToSubscribedtoRoomsUsersBuffer(new SubscribedtoRoomsUsersBufferModal(chatUser));
			}
		}
		// send to user about room apearenced
		OperationStatus operationStatus = new OperationStatus(OperationType.ADD_ROOM, operationSuccess, "ADD ROOM");
		String subscriptionStr = "/topic/users/" + chatUserId + "/status";
		// send to user that operation success
		simpMessagingTemplate.convertAndSend(subscriptionStr, operationStatus);
		return room == null ? null : room.getId();
	}

	/***************************
	 * REMOVE/ADD USERS FROM/TO ROOMS
	 ***************************/

	boolean removeUserFromRoomFully(ChatUser user_o, Room room_o, Principal principal, boolean ignoreAuthor) {
		/*if (room_o.getType() == RoomType.STUDENTS_GROUP)
			return false;*/
		ChatUser authorUser = chatUserServise.getChatUser(principal);
		boolean haveNullObj = room_o == null || user_o == null;
		boolean isAuthor = user_o.getId().longValue() == room_o.getAuthor().getId().longValue();
		boolean currentUserIsAuthor = authorUser.getId().longValue() == room_o.getAuthor().getId().longValue();
		boolean permitions = (roomPermissionsService.getPermissionsOfUser(room_o, authorUser)
				& RoomPermissions.Permission.REMOVE_USER.getValue()) == RoomPermissions.Permission.REMOVE_USER
				.getValue();
		if (haveNullObj || isAuthor || (!(permitions || currentUserIsAuthor) || !room_o.isActive()) && !ignoreAuthor) {
			return false;
		}
		return removeUserFromRoomFullyWithoutCheckAuthorization(user_o, room_o);
	}

	/*
	 * Only for remove self from room
	 */
	public boolean removeUserFromRoomFullyWithoutCheckAuthorization(ChatUser user_o, Room room_o) {
		// check for BOT
		if (user_o.getId() == BotParam.BOT_ID)
			return false;
		// check for private room
		if (room_o.getType() == RoomType.PRIVATE) {
			PrivateRoomInfo info = roomService.getPrivateRoomInfo(room_o);
			if (user_o == info.getFirtsUser() || user_o == info.getSecondUser())
				return false;
		}

		roomService.removeUserFromRoom(user_o, room_o);
		// chatUserLastRoomDateService.removeUserLastRoomDate(user_o, room_o);

		addFieldToSubscribedtoRoomsUsersBuffer(new SubscribedtoRoomsUsersBufferModal(user_o));
		updateParticipants();// force update
		try {
			processRoomsQueues();
		} catch (JsonProcessingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		simpMessagingTemplate.convertAndSend("/topic/" + room_o.getId().toString() + "/chat.participants",
				retrieveParticipantsMessage(room_o.getId()));
		simpMessagingTemplate.convertAndSend("/topic/chat/rooms/user." + user_o.getId(),
				new UpdateRoomsPacketModal(roomService.getRoomsModelByChatUser(user_o)));
		return true;
	}

	@Transactional
	public boolean changeAuthor(ChatUser newAuthor, Room room, boolean savePreviusAuthorAsUser, Principal principal,
			boolean ignoreAuthor) {
		if (room == null || newAuthor == null)
			return false;
		ChatUser author = room.getAuthor();
		if (author.equals(newAuthor))
			return true;

		boolean contain = false;
		if (room.getUsers().contains(newAuthor)) {
			// delete from LIST of add users && check for remove from update
			room.removeUser(newAuthor);
			contain = true;
		}
		roomService.setAuthor(newAuthor, room);
		if (roomService.update(room) == null)
			return false;

		if (savePreviusAuthorAsUser) {
			addUserToRoom(author, room, principal, true);
			if (!contain) {
				addFieldToSubscribedtoRoomsUsersBuffer(new SubscribedtoRoomsUsersBufferModal(newAuthor));
				updateParticipants();

				simpMessagingTemplate.convertAndSend("/topic/" + newAuthor.getId().toString() + "/chat.participants",
						retrieveParticipantsMessage(room.getId()));
				simpMessagingTemplate.convertAndSend("/topic/chat/rooms/user." + newAuthor.getId(),
						new UpdateRoomsPacketModal(roomService.getRoomsModelByChatUser(newAuthor)));

			}
		} else {
			chatLastRoomDateService.removeUserLastRoomDate(author, room);
		}
		return true;
	}

	boolean addUserToRoom(ChatUser user_o, Room room_o, Principal principal, boolean ignoreAuthor) {
		/*if (room_o.getType() == RoomType.STUDENTS_GROUP)
			return false;*/
		Long chatUserAuthorId = Long.parseLong(principal.getName());
		ChatUser authorUser = chatUserServise.getChatUser(chatUserAuthorId);

		boolean haveNullObj = room_o == null || user_o == null;
		boolean isAuthor = user_o.getId().longValue() == room_o.getAuthor().getId().longValue();
		boolean currentUserIsAuthor = authorUser.getId().longValue() == room_o.getAuthor().getId().longValue();
		Integer permissionBitSetPrimitive = roomPermissionsService.getPermissionsOfUser(room_o, authorUser);
		boolean operationPermitted = RoomPermissions.Permission.ADD_USER
				.checkNumberForThisPermission(permissionBitSetPrimitive);
		if (haveNullObj || isAuthor
				|| (!(operationPermitted || currentUserIsAuthor) || !room_o.isActive()) && !ignoreAuthor) {
			return false;
		}
		/*
		 * Set<Room> all = user_o.getRoomsFromUsers();
		 * all.addAll(user_o.getRootRooms()); if(all.contains(room_o)) return
		 * false;
		 */

		if (room_o.getType() == RoomType.STUDENTS_GROUP)
		{
			roomPermissionsService.addPermissionsToUser(room_o, user_o, RoomPermissions.Permission.INVITED_USER.getValue());
		}

		if (roomService.addUserToRoom(user_o, room_o) == false)
			return false;

		addFieldToSubscribedtoRoomsUsersBuffer(new SubscribedtoRoomsUsersBufferModal(user_o));
		updateParticipants();

		simpMessagingTemplate.convertAndSend("/topic/" + room_o.getId().toString() + "/chat.participants",
				retrieveParticipantsMessage(room_o.getId()));
		simpMessagingTemplate.convertAndSend("/topic/chat/rooms/user." + user_o.getId(),
				new UpdateRoomsPacketModal(roomService.getRoomsModelByChatUser(user_o)));
		return true;
	}

	boolean addUserToRoomFn(String nickName, Long room, Principal principal, boolean ws) {
		Room room_o = roomService.getRoom(room);
		ChatUser user_o = chatUserServise.getChatUserFromIntitaEmail(nickName, false);
		return addUserToRoom(user_o, room_o, principal, false);
	}

	boolean addChatUserToRoomFn(Long chatUserId, Long room, Principal principal, boolean ws) {
		Room room_o = roomService.getRoom(room);
		ChatUser user_o = chatUserServise.getChatUser(chatUserId);
		return addUserToRoom(user_o, room_o, principal, false);
	}

	boolean addIntitaUserToRoomFn(Long intitaUserId, Long room, Principal principal, boolean ws) {
		Room room_o = roomService.getRoom(room);
		ChatUser user_o = chatUserServise.getChatUserFromIntitaId(intitaUserId, false);
		return addUserToRoom(user_o, room_o, principal, false);
	}

	@RequestMapping(value = "/chat/rooms.{room}/user/add", method = RequestMethod.POST)
	public @ResponseBody String addUserToRoomLP(@PathVariable("room") Long roomId,
			@RequestParam(name = "chatId", required = false) Long chatId,
			@RequestParam(name = "email", required = false) String email, Principal principal, HttpRequest req)
					throws InterruptedException, JsonProcessingException {
		if (chatId != null)
			return mapper.writeValueAsString(addChatUserToRoomFn(chatId, roomId, principal, false));
		if (email != null)
			return mapper.writeValueAsString(addUserToRoomFn(email, roomId, principal, false));
		return null;
	}

	@RequestMapping(value = "/chat/rooms/{room}/remove", method = RequestMethod.POST)
	@ResponseBody
	public boolean removeRoomFromList(@PathVariable("room") Long room, Principal principal) {
		Room room_o = roomService.getRoom(room);
		ChatUser user_o = chatUserServise.getChatUser(principal);
		if (room_o == null || user_o == null)
			return false;
		if (room_o.getAuthor().getId().equals(user_o.getId())) {
			room_o.setActive(true);
			for (ChatUser user : room_o.cloneChatUsers()) {
				removeUserFromRoomFully(user, room_o, principal, false);
			}
			room_o.setActive(false);
			roomService.update(room_o);
		}
		return removeUserFromRoomFullyWithoutCheckAuthorization(user_o, room_o);

	}

	@RequestMapping(value = "/chat/rooms.{room}/user.remove/{id}", method = RequestMethod.POST)
	@ResponseBody
	public boolean removeUserFromRoomRequest(@PathVariable("id") Long id, @PathVariable("room") Long room,
			Principal principal) {
		// TODO unsubscribe user from room
		Room room_o = roomService.getRoom(room);
		ChatUser user_o = chatUserServise.getChatUser(id);
		boolean isRemoved = removeUserFromRoomFully(user_o, room_o, principal, false);
		;
		if (isRemoved)
			simpMessagingTemplate.convertAndSend(String.format("/topic/chat/rooms/%s/remove_user/%s", room, id), "");
		return isRemoved;
	}

	public static void addFieldToSubscribedtoRoomsUsersBuffer(SubscribedtoRoomsUsersBufferModal modal) {
		subscribedtoRoomsUsersBuffer.add(modal);
	}

	private Map<Long, String> convertToNameList(ArrayList<Room> list) {
		Map<Long, String> res = new HashMap<Long, String>();
		for (Room r : list) {
			res.put(r.getId(), r.getName());
		}
		return res;
	}

	/**
	 * 
	 * @return login events from private rooms by current user
	 */
	@RequestMapping(value = "/chat/user/friends", method = RequestMethod.POST)
	@ResponseBody
	public String userFriends(Principal principal) {
		ChatUser user = chatUserServise.getChatUser(principal);
		try {
			return mapper.writeValueAsString(roomService.getPrivateRoomsLoginEvent(user));
		} catch (JsonProcessingException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		return "{}";
	}


	@RequestMapping(value = "/chat/rooms/find", method = RequestMethod.GET)
	@ResponseBody
	public Map<Long, String> findRoomByName(@RequestParam(name="name") String nameLike, Principal principal) {
		Map<Long, String> roomSimples = new HashMap<>();
		
		ArrayList<Room> rooms = roomService.getRoomsWithNameLike("%" + nameLike + "%");
		for(Room room : rooms)
		{
			roomSimples.put(room.getId(), room.getName());
		}
		return roomSimples;
	}



	@MessageExceptionHandler
	@SendToUser(value = "/exchange/amq.direct/errors", broadcast = false)
	public String handleProfanity(TooMuchProfanityException e) {
		return e.getMessage();
	}

	@MessageExceptionHandler(MessageDeliveryException.class)
	public String handleMessageDeliveryException(MessageDeliveryException e) {
		log.error("MessageDeliveryException handler executed");
		return e.getMessage();
	}

	@MessageExceptionHandler(NumberFormatException.class)
	public String handleMessageNumberFormatException(Exception ex) {
		log.error("NumberFormatException handler executed:" + ex.getMessage());
		return "NumberFormatException handler executed:" + ex.getMessage();
	}

	@MessageExceptionHandler(Exception.class)
	public String handleMessageException(Exception ex) {
		log.error("Exception handler executed:");
		ex.printStackTrace();
		return "NumberFormatException handler executed";
	}

	public static class UpdateRoomsPacketModal implements Serializable {
		/**
		 * For serialization
		 */
		private static final long serialVersionUID = -3202901391346608368L;

		@JsonView(Views.Public.class)
		List<RoomModelSimple> list;
		@JsonView(Views.Public.class)
		boolean replace;

		public UpdateRoomsPacketModal(List<RoomModelSimple> list) {
			this.list = list;
			replace = true;
		}

		public UpdateRoomsPacketModal(List<RoomModelSimple> list, boolean needReplace) {
			this.list = list;
			replace = needReplace;
		}
	}

	public static class SubscribedtoRoomsUsersBufferModal {
		ChatUser chatUser;
		boolean replace = true;
		ArrayList<Room> roomsForUpdate;

		public SubscribedtoRoomsUsersBufferModal() {
			chatUser = null;
		}

		public SubscribedtoRoomsUsersBufferModal(ChatUser chatUser, ArrayList<Room> arr) {
			this.chatUser = chatUser;
			replace = false;
			roomsForUpdate = arr;

		}

		public SubscribedtoRoomsUsersBufferModal(ChatUser chatUser) {
			this.chatUser = chatUser;
			replace = true;
		}
	}

	@PostConstruct
	private void PostConstructor() {
		participantRepository.addParticipantPresenceByLastConnectionTime(BotParam.BOT_ID);// BOT
		// online
		// OK
	}
}