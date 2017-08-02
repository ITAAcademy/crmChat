package com.intita.wschat.web;

import java.io.Serializable;
import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;

import javax.annotation.PostConstruct;

import com.intita.wschat.config.ChatPrincipal;
import com.intita.wschat.domain.*;
import com.intita.wschat.dto.model.UserMessageWithLikesDTO;
import com.intita.wschat.models.*;
import com.intita.wschat.services.*;
import com.intita.wschat.services.common.UsersOperationsService;
import org.hibernate.bytecode.buildtime.spi.ExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.HttpRequest;
import org.springframework.messaging.MessageDeliveryException;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageExceptionHandler;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.annotation.SendToUser;
import org.springframework.messaging.simp.annotation.SubscribeMapping;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.stereotype.Service;
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
import com.intita.wschat.dto.mapper.DTOMapper;
import com.intita.wschat.dto.model.ChatUserDTO;
import com.intita.wschat.event.LoginEvent;
import com.intita.wschat.event.ParticipantRepository;
import com.intita.wschat.exception.ChatUserNotInRoomException;
import com.intita.wschat.exception.RoomNotFoundException;
import com.intita.wschat.exception.TooMuchProfanityException;
import com.intita.wschat.models.OperationStatus.OperationType;
import com.intita.wschat.util.ProfanityChecker;
import com.intita.wschat.web.BotController.BotParam;
import com.intita.wschat.web.ChatController.CurrentStatusUserRoomStruct;

import jsonview.Views;
//import scala.annotation.meta.setter;

@Service
@Controller
public class RoomController {
	final String DIALOG_NAME_PREFIX = "DIALOG_";
	private final static Logger log = LoggerFactory.getLogger(RoomController.class);

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

	@Autowired private BotCategoryService botCategoryService;
	@Autowired private FlywayMigrationStrategyCustom flyWayStategy;
	@Autowired private RoomPermissionsService roomPermissionsService;
	@Autowired private OfflineStudentsGroupService offlineStudentsGroupService;
	@Autowired private ChatLangService chatLangService;
	@Autowired private NotificationsService notificationsService;
	
	@Autowired private DTOMapper dtoMapper;
	@Autowired private ChatLikeStatusService chatLikeStatusService;

	@Autowired
	@Lazy
	private UsersOperationsService usersOperationsService;

	private final int PARTICIPANTS_INITIAL_COUNT = 10;

	public static class ROLE {
		public static final int ADMIN = 256;
	}

	static final private ObjectMapper mapper = new ObjectMapper();

	// =>
	// roomId
	// =>
	// roomId

	private ArrayList<Room> roomsArray;

	// =>
	// roomId

	@PostConstruct
	private void postFunction() {
		// configService.getParam("chatBotEnable");
	}

	@RequestMapping(value = "/chat/rooms/create/with_bot/", method = RequestMethod.POST)
	@ResponseBody
	public void createDialogWithBotRequest(@RequestBody String roomName, Authentication auth) {
		usersOperationsService.createDialogWithBot(roomName, auth);
	}

	@RequestMapping(value = "/chat/participants/load_other", method = RequestMethod.GET)
	@ResponseBody
	public Set<LoginEvent> loadOtherParticipants(@RequestParam Long roomId,@RequestParam(required = false) Long lastParticipant) {
		Room room = roomService.getRoom(roomId);
		return usersOperationsService.getParticipants(room,lastParticipant,PARTICIPANTS_INITIAL_COUNT);
	}


	/**********************
	 * what doing with new auth user
	 **********************/
	@SubscribeMapping("/chat.login")
	public LoginResponseData login(Authentication authentication) {// Control user page
		// after auth
		return login(authentication, null);
	}

	/**
	 * Returns chat rooms, user, user authorities
	 */
	@SubscribeMapping("/chat.login/{demandedChatUserIdStr}")
	public LoginResponseData loginMapping(Authentication authentication, @DestinationVariable String demandedChatUserIdStr){
		Long demandedChatUserId = null;
		try{
			demandedChatUserId = Long.parseLong(demandedChatUserIdStr);
		}
		catch(Exception e){

		}
		return login(authentication,demandedChatUserId);
	}



	public LoginResponseData login(Authentication auth, Long demandedChatUserId)
	{
		LoginResponseData responseData = new LoginResponseData();
		long startTime = System.nanoTime();
		ChatPrincipal chatPrincipal = (ChatPrincipal)auth.getPrincipal();

		Long realChatUserId = chatPrincipal.getChatUser().getId();
		ChatUser realChatUser = chatPrincipal.getChatUser();
		User realIntitaUser = chatPrincipal.getIntitaUser();
		ChatUser activeChatUser = demandedChatUserId == null ? realChatUser : chatUserServise.getChatUser(demandedChatUserId);

		if (activeChatUser == null || !Objects.equals(realChatUserId,activeChatUser.getId()) ) {
			if (!userService.isAdmin(realIntitaUser))
				return null;
		}
		User activeIntitaUser = activeChatUser.getIntitaUser();

		boolean userIsNotAuthorized = realIntitaUser == null;

		if (userIsNotAuthorized) {
			/*
			Room room;
			
			if (chatLastRoomDateService.getUserLastRoomDates(activeChatUser).iterator().hasNext()) {
				room = chatLastRoomDateService.getUserLastRoomDates(realChatUser).iterator().next().getLastRoom();
			} else {

				boolean botEnable = true;
				ConfigParam s_botEnable = configService.getParam("chatBotEnable");
				if (s_botEnable != null)
					botEnable = Boolean.parseBoolean(s_botEnable.getValue());
				if (botEnable) {
					room = createDialogWithBot("BotSys_" + realChatUser.getId() + "_" + new Date().toString(),
							auth);
				} else
					room = createRoomWithTenant(auth);

			}

			if (room != null)
				responseData.setNextWindow(room.getId().toString());
			else
				responseData.setNextWindow("-1");
				*/
		} else {
			// subscribedtoRoomsUsersBuffer.add(user);
			responseData.setNextWindow("0");
		}
		ChatUserDTO chatUserDTO = dtoMapper.map(activeChatUser);
		Set<Long> activeUsers = participantRepository.getActiveUsers();
		responseData.setActiveUsers(activeUsers);
		responseData.setNotifications(notificationsService.generationNotification());

		Set<UserRole> userRoles = userService.getAllRoles(activeIntitaUser);

		if (activeIntitaUser != null) {
			LoginEvent event = null;
			if (userRoles.contains(UserRole.STUDENT)) {
				User iTrainer = userService.getTrainer(activeIntitaUser.getId());
				if (iTrainer != null) {
					ChatUser chatTrainer = chatUserServise.getChatUserFromIntitaUser(iTrainer, false);
					if (chatTrainer != null) {
						ChatUserDTO chatTrainerDTO = dtoMapper.map(chatTrainer);
						responseData.setTrainer(chatTrainerDTO);
					}

				}
			}
		}
		chatUserDTO.setRoles(userService.getAllRoles(activeIntitaUser));
		responseData.setChatUser(chatUserDTO);
		List<RoomModelSimple> roomModels = activeChatUser.getId()==null ? new ArrayList<RoomModelSimple>() : roomService.getRoomsModelByChatUser(activeChatUser);
		responseData.setRoomModels(roomModels);
		/***
		 * @deprecated try { result.put("friends",
		 *             mapper.writeValueAsString(roomService.getPrivateLoginEvent(user)));
		 *             } catch (JsonProcessingException e1) { // TODO
		 *             Auto-generated catch block e1.printStackTrace(); }
		 */
		if (userRoles.contains(UserRole.TRAINER)) {
			ArrayList<ChatUserDTO> tenantsObjects = userService.getAllFreeTenantsDTO(activeChatUser.getId());
			String tenantsJson = null;
			responseData.setTenants(tenantsObjects);
		}

		long endTime = System.nanoTime();
		double duration = (endTime - startTime)/ 1000000000.0;  //divide by 1000000 to get milliseconds.
		log.info("Login duration: " + duration);
		
		return responseData;
	}

	@RequestMapping(value = "/chat/login/{userId}", method = RequestMethod.POST)
	@ResponseBody
	public String retrieveParticipantsLP(Authentication auth, @PathVariable("userId") Long userId)
			throws JsonProcessingException {
		if(userId == -1)
			userId = null;
		return mapper.writeValueAsString(login( auth, userId));
	}

	@RequestMapping(value = "/chat/logintest", method = RequestMethod.POST)
	@ResponseBody
	public String loginTest(Authentication auth)
			throws JsonProcessingException {
		return mapper.writeValueAsString(login(auth, null));
	}

	@RequestMapping(value = "/chat/rooms/all", method = RequestMethod.GET)
	@ResponseBody
	public List<RoomModelSimple> retrieveAllRooms(Authentication auth)
			throws JsonProcessingException {
		ChatPrincipal chatPrincipal = (ChatPrincipal)auth.getPrincipal();
		ChatUser activeChatUser = chatPrincipal.getChatUser();
		List<RoomModelSimple> roomModels = roomService.getRoomsModelByChatUser(activeChatUser);
		return roomModels;
	}

	/***************************
	 * GET PARTICIPANTS AND LOAD MESSAGE
	 ***************************/



	public Map<String, Object> retrieveParticipantsSubscribeAndMessagesObj(Room room_o, String lang, ChatUser user) {

		Queue<UserMessage> buff = usersOperationsService.getMessagesBuffer().get(room_o.getId());
		ArrayList<UserMessage> userMessages = userMessageService.getFirst20UserMessagesByRoom(room_o, lang,user);
		if (buff != null)
			userMessages.addAll(buff);
		List<UserMessageWithLikesDTO> messagesDTO = dtoMapper.mapListUserMessagesWithLikes(userMessages);
		//ArrayList<ChatMessage> messagesHistory = ChatMessage.getAllfromUserMessages(userMessages);
		Set<Long> likedMessages = chatLikeStatusService.getLikedMessagesIdsByUserInRoom(user.getId(),room_o.getId());
		Set<Long> dislikedMessages = chatLikeStatusService.getDislikedMessagesIdsByUserInRoom(user.getId(),room_o.getId());


		HashMap<String, Object> map = new HashMap();
		map.put("participants", usersOperationsService.getParticipants(room_o,null,PARTICIPANTS_INITIAL_COUNT));
		map.put("messages", messagesDTO);
		map.put("likedMessagesIds",likedMessages);
		map.put("dislikedMessagesIds",dislikedMessages);
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
			@DestinationVariable("lang") String lang, SimpMessageHeaderAccessor headerAccessor, Authentication auth) {// ONLY
		ChatPrincipal chatPrincipal = (ChatPrincipal)auth.getPrincipal();
		CurrentStatusUserRoomStruct status = ChatController.isMyRoom(room, chatPrincipal,
				roomService);
		ChatUser o_object = chatPrincipal.getChatUser();
		if (status == null) {
			if (o_object != null) {
				User iUser = chatPrincipal.getIntitaUser();

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
		return usersOperationsService.retrieveParticipantsMessage(room,PARTICIPANTS_INITIAL_COUNT);
	}

	@SubscribeMapping("/chat.tenants")
	public ArrayList<LoginEvent> retrieveTenants(Authentication auth) {
		ChatPrincipal chatPrincipal = (ChatPrincipal)auth.getPrincipal();

		ChatUser currentChatUser = chatPrincipal.getChatUser();
		ArrayList<LoginEvent> loginEvents = userService.getAllFreeTenantsLoginEvent();
		return loginEvents;
	}

	@MessageMapping("/chat.tenants")
	public ArrayList<LoginEvent> retrieveTenantsMesageMapping(Authentication auth) {
		ChatPrincipal chatPrincipal = (ChatPrincipal)auth.getPrincipal();
		ChatUser currentChatUser = chatPrincipal.getChatUser();
		ArrayList<LoginEvent> loginEvents = userService.getAllFreeTenantsLoginEvent();
		return loginEvents;
	}

	@RequestMapping(value = "/{room}/chat/participants_and_messages", method = RequestMethod.POST)
	@ResponseBody
	public String retrieveParticipantsAndMessagesLP(@PathVariable("room") Long room, Authentication auth)
			throws JsonProcessingException {
		ChatPrincipal chatPrincipal = (ChatPrincipal)auth.getPrincipal();

		CurrentStatusUserRoomStruct struct = ChatController.isMyRoom(room, chatPrincipal,
				roomService);// Control room from LP
		if (struct == null)
			return "{}";
		ChatUser chatUser = chatPrincipal.getChatUser();
		String participantsAndMessages = mapper.writeValueAsString(
				retrieveParticipantsSubscribeAndMessagesObj(struct.getRoom(), chatLangService.getCurrentLang(),chatUser));

		return participantsAndMessages;
	}

	@RequestMapping(value = "/{room}/chat/set_name", method = RequestMethod.POST, produces = "text/plain;charset=UTF-8")
	@ResponseBody
	public String setRoomName(@PathVariable("room") Long roomId, @RequestParam String newName, Authentication auth)
			throws JsonProcessingException {
		Room room = roomService.getRoom(roomId);

		if(room == null)
			throw new NullPointerException();

		log.info(room.getAuthor().getId().toString());
		ChatPrincipal chatPrincipal = (ChatPrincipal)auth.getPrincipal();
		ChatUser chatUser = chatPrincipal.getChatUser();
		if(!(chatUser.equals(room.getAuthor())))
			throw new ExecutionException("Non author try change room name!!!");

		String nameAfterChanging = roomService.updateRoomName(room, newName);
		log.info("Room name changed:" + newName);
		return nameAfterChanging;
	}

	@RequestMapping(value = "/{room}/chat/participants/update", method = RequestMethod.POST, produces = "text/plain;charset=UTF-8")
	@ResponseBody
	public DeferredResult<String> retrieveParticipantsUpdateLP(@PathVariable("room") Long room, Authentication auth)
			throws JsonProcessingException {
		ChatPrincipal chatPrincipal = (ChatPrincipal)auth.getPrincipal();
		Long timeOut = 5000L;
		DeferredResult<String> result = new DeferredResult<String>(timeOut, "{}");
		Queue<DeferredResult<String>> queue = usersOperationsService.getResponseBodyQueueForParticipents().get(room);
		if (queue == null) {
			queue = new ConcurrentLinkedQueue<DeferredResult<String>>();
		}
		CurrentStatusUserRoomStruct struct = ChatController.isMyRoom(room, chatPrincipal,
				roomService);// Control room from LP
		if (struct != null) {
			usersOperationsService.getResponseBodyQueueForParticipents().put(room.toString(), queue);
			queue.add(result);
		} else
			result.setErrorResult(new ChatUserNotInRoomException(""));

		return result;
	}

	/*
	 * call only if is need
	 */


	/***************************
	 * GET/ADD ROOMS
	 *
	 * @throws JsonProcessingException
	 ***************************/

	@RequestMapping(value = "/chat/rooms/roomInfo/{roomID}", method = RequestMethod.POST)
	@ResponseBody
	public String getRoomInfo(@PathVariable("roomID") Long roomId, Authentication auth) throws JsonProcessingException {
		ChatPrincipal chatPrincipal = (ChatPrincipal)auth.getPrincipal();
		CurrentStatusUserRoomStruct struct = ChatController.isMyRoom(roomId, chatPrincipal,
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
			Authentication auth) throws JsonProcessingException {
		ChatPrincipal chatPrincipal = (ChatPrincipal)auth.getPrincipal();
		log.info("getPrivateRoom");
		ChatUser privateCharUser;
		if (isChatId)
			privateCharUser = chatUserServise.getChatUser(userId);
		else
			privateCharUser = chatUserServise.getChatUserFromIntitaId(userId, false);
		ChatUser chatUser = chatPrincipal.getChatUser();
		Room room = getPrivateRoom(chatUser, privateCharUser);
		return room.getId();// @BAG@
	}

	@RequestMapping(value = "/chat/go/rooms/private/trainer", method = RequestMethod.GET)
	public String goPrivateRoomWithTrainer(Authentication auth) throws JsonProcessingException {
		ChatPrincipal chatPrincipal = (ChatPrincipal)auth.getPrincipal();

		ChatUser principalChatUser = chatPrincipal.getChatUser();

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
			Authentication auth) throws JsonProcessingException {
		Long id = getPrivateRoomRequest(userId, isChatId, auth);
		return "redirect:/#/dialog_view/" + id;
	}

	// @SubscribeMapping("/chat/rooms/user.{userId}")

	// LONG POLLING PART

	@RequestMapping(value = "/chat/rooms/adddialogwithuser", method = RequestMethod.POST)
	@ResponseBody
	public Long addDialog(Authentication auth, @RequestBody Long chatUserId) {
		ChatPrincipal chatPrincipal = (ChatPrincipal)auth.getPrincipal();
		ChatUser chatUser = chatPrincipal.getChatUser();
		if (chatUser == null)
			return -1L;
		User authorOfDialog = chatUser.getIntitaUser();
		if (authorOfDialog == null)
			return -1L;
		User interlocutor = userService.getUserFromChat(chatUserId);
		if (interlocutor == null)
			return -1L;
		String roomName = DIALOG_NAME_PREFIX + authorOfDialog.getLogin() + "_" + interlocutor.getLogin();
		Room room = roomService.register(roomName, chatUser);
		return room.getId();
	}

	@RequestMapping(value = "/chat/rooms/user/{username}", method = RequestMethod.POST)
	@ResponseBody
	public DeferredResult<String> getRooms(Authentication auth) {
		if (auth == null)
			return null;
		ChatPrincipal chatPrincipal = (ChatPrincipal)auth.getPrincipal();

		Long timeOut = 1000000L;
		DeferredResult<String> deferredResult = new DeferredResult<String>(timeOut, "NULL");
		Long chatUserId = chatPrincipal.getChatUser().getId();

		ConcurrentLinkedQueue<DeferredResult<String>> queue = chatUserId==null ? null : usersOperationsService.getResponseRoomBodyQueue().get(chatUserId);
		if (queue == null) {
			queue = new ConcurrentLinkedQueue<DeferredResult<String>>();
		}

		usersOperationsService.getResponseRoomBodyQueue().put(chatUserId, queue);
		queue.add(deferredResult);
		// System.out.println("responseRoomBodyQueue
		// queue_count:"+queue.size());

		return deferredResult;
	}



	@RequestMapping(value = "/chat/rooms/add", method = RequestMethod.POST)
	@ResponseBody
	// @SendToUser(value = "/exchange/amq.direct/errors", broadcast = false)
	public Long addRoomByAuthorLP(@RequestParam(name = "name") String roomName,
			@RequestBody(required = false) ArrayList<Long> userIds, Authentication auth) {
		return usersOperationsService.addRoomByAuthorLP(roomName,userIds,auth);
	}

	/***************************
	 * REMOVE/ADD USERS FROM/TO ROOMS
	 ***************************/

	boolean removeUserFromRoomFully(ChatUser user_o, Room room_o, Authentication auth, boolean ignoreAuthor) {
		/*if (room_o.getType() == RoomType.STUDENTS_GROUP)
			return false;*/
		ChatPrincipal chatPrincipal = (ChatPrincipal)auth.getPrincipal();

		ChatUser authorUser = chatPrincipal.getChatUser();
		boolean haveNullObj = room_o == null || user_o == null;
		boolean isAuthor = user_o.getId().longValue() == room_o.getAuthor().getId().longValue();
		boolean currentUserIsAuthor = authorUser.getId().longValue() == room_o.getAuthor().getId().longValue();
		boolean permitions = (roomPermissionsService.getPermissionsOfUser(room_o, authorUser)
				& RoomPermissions.Permission.REMOVE_USER.getValue()) == RoomPermissions.Permission.REMOVE_USER
				.getValue();
		if (haveNullObj || isAuthor || (!(permitions || currentUserIsAuthor) || !room_o.isActive()) && !ignoreAuthor) {
			return false;
		}
		return usersOperationsService.removeUserFromRoomFullyWithoutCheckAuthorization(user_o, room_o);
	}

	/*
	 * Only for remove self from room
	 */


	@Transactional
	public boolean changeAuthor(ChatUser newAuthor, Room room, boolean savePreviusAuthorAsUser, Authentication auth,
			boolean ignoreAuthor) {
		if (room == null || newAuthor == null)
			return false;
		ChatUser author = room.getAuthor();

		System.out.println("author id:"+author.getId()+" newAuthor id:"+newAuthor.getId());
		boolean contain = false;
		if (room.getUsers().contains(newAuthor)) {
			// delete from LIST of add users && check for remove from update
			room.removeUser(newAuthor);
			contain = true;
		}
		roomService.setAuthor(newAuthor, room);
		if (roomService.update(room,true) == null)
			return false;

		if (savePreviusAuthorAsUser) {
			addUserToRoom(author, room, auth, true);
			if (!contain) {
				usersOperationsService.addFieldToSubscribedtoRoomsUsersBuffer(new SubscribedtoRoomsUsersBufferModal(newAuthor));
				usersOperationsService.updateParticipants();

				simpMessagingTemplate.convertAndSend("/topic/" + newAuthor.getId().toString() + "/chat.participants",
						retrieveParticipantsMessage(room.getId()));
				simpMessagingTemplate.convertAndSend("/topic/chat/rooms/user." + newAuthor.getId(),
						new UpdateRoomsPacketModal(roomService.getRoomsModelByChatUser(newAuthor)));

			}
		} else if( !author.equals(newAuthor)) {
			chatLastRoomDateService.removeUserLastRoomDate(author, room);
		}
		return true;
	}

	boolean addUserToRoom(ChatUser user_o, Room room_o, Authentication auth, boolean ignoreAuthor) {
		/*if (room_o.getType() == RoomType.STUDENTS_GROUP)
			return false;*/
		ChatPrincipal chatPrincipal = (ChatPrincipal)auth.getPrincipal();

		Long chatUserAuthorId =chatPrincipal.getChatUser().getId();
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

		if (room_o.getTypeEnum() == ChatRoomType.STUDENTS_GROUP)
		{
			roomPermissionsService.addPermissionsToUser(room_o, user_o, RoomPermissions.Permission.INVITED_USER.getValue());
		}

		if (roomService.addUserToRoom(user_o, room_o) == false)
			return false;

		usersOperationsService.addFieldToSubscribedtoRoomsUsersBuffer(new SubscribedtoRoomsUsersBufferModal(user_o));
		usersOperationsService.updateParticipants();

		simpMessagingTemplate.convertAndSend("/topic/" + room_o.getId().toString() + "/chat.participants",
				retrieveParticipantsMessage(room_o.getId()));
		simpMessagingTemplate.convertAndSend("/topic/chat/rooms/user." + user_o.getId(),
				new UpdateRoomsPacketModal(roomService.getRoomsModelByChatUser(user_o)));
		return true;
	}

	boolean addUserToRoomFn(String nickName, Long room, Authentication auth, boolean ws) {
		Room room_o = roomService.getRoom(room);
		ChatUser user_o = chatUserServise.getChatUserFromIntitaEmail(nickName, false);
		return addUserToRoom(user_o, room_o, auth, false);
	}

	boolean addChatUserToRoomFn(Long chatUserId, Long room, Authentication auth, boolean ws) {
		Room room_o = roomService.getRoom(room);
		ChatUser user_o = chatUserServise.getChatUser(chatUserId);
		return addUserToRoom(user_o, room_o, auth, false);
	}
	boolean addChatUsersToRoomFn(List<Long> chatUserIds, Long room, Authentication auth, boolean ws) {
		Room room_o = roomService.getRoom(room);
		List<ChatUser> users = chatUserServise.getUsers(chatUserIds);
		for (ChatUser user : users){
			boolean result = addUserToRoom(user, room_o, auth, false);
		}
		return true;
	}

	boolean addIntitaUserToRoomFn(Long intitaUserId, Long room, Authentication auth, boolean ws) {
		Room room_o = roomService.getRoom(room);
		ChatUser user_o = chatUserServise.getChatUserFromIntitaId(intitaUserId, false);
		return addUserToRoom(user_o, room_o, auth, false);
	}

	@RequestMapping(value = "/chat/rooms.{room}/user/add", method = RequestMethod.POST)
	public @ResponseBody String addUserToRoomLP(@PathVariable("room") Long roomId,
			@RequestParam(name = "chatId", required = false) Long chatId,
			@RequestParam(name = "email", required = false) String email, Authentication auth, HttpRequest req)
					throws InterruptedException, JsonProcessingException {
		if (chatId != null)
			return mapper.writeValueAsString(addChatUserToRoomFn(chatId, roomId, auth, false));
		if (email != null)
			return mapper.writeValueAsString(addUserToRoomFn(email, roomId, auth, false));
		return null;
	}
	@RequestMapping(value = "/chat/rooms.{room}/user/add_all", method = RequestMethod.POST)
	public @ResponseBody String addUserToRoomLP(@PathVariable("room") Long roomId,Authentication auth,
												@RequestBody List<Long> chatUserIds)
			throws InterruptedException, JsonProcessingException {
		addChatUsersToRoomFn(chatUserIds,roomId,auth,false);
		return null;
	}

	@RequestMapping(value = "/chat/rooms/{room}/remove", method = RequestMethod.POST)
	@ResponseBody
	public boolean removeRoomFromList(@PathVariable("room") Long room, Authentication auth) {
		Room room_o = roomService.getRoom(room);
		ChatPrincipal chatPrincipal = (ChatPrincipal)auth.getPrincipal();
		ChatUser user_o = chatPrincipal.getChatUser();
		if (room_o == null || user_o == null)
			return false;
		if (room_o.getAuthor().getId().equals(user_o.getId())) {
			room_o.setActive(true);
			for (ChatUser user : room_o.cloneChatUsers()) {
				removeUserFromRoomFully(user, room_o, auth, false);
			}
			room_o.setActive(false);
			roomService.update(room_o,true);
		}
		return usersOperationsService.removeUserFromRoomFullyWithoutCheckAuthorization(user_o, room_o);

	}

	@RequestMapping(value = "/chat/rooms.{room}/user.remove/{id}", method = RequestMethod.POST)
	@ResponseBody
	public boolean removeUserFromRoomRequest(@PathVariable("id") Long id, @PathVariable("room") Long room,
			Authentication auth) {
		// TODO unsubscribe user from room
		Room room_o = roomService.getRoom(room);
		ChatUser user_o = chatUserServise.getChatUser(id);
		boolean isRemoved = removeUserFromRoomFully(user_o, room_o, auth, false);
		;
		if (isRemoved)
			simpMessagingTemplate.convertAndSend(String.format("/topic/chat/rooms/%s/remove_user/%s", room, id), "");
		return isRemoved;
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
	public String userFriends(Authentication auth) {
		ChatPrincipal chatPrincipal = (ChatPrincipal)auth.getPrincipal();
		ChatUser user = chatPrincipal.getChatUser();
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
	public Map<Long, String> findRoomByName(@RequestParam(name="name") String nameLike) {
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


	@PostConstruct
	private void PostConstructor() {
		participantRepository.addParticipantPresenceByLastConnectionTime(BotParam.BOT_ID);// BOT
		// online
		// OK
	}
}