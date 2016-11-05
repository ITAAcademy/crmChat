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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.oauth2.resource.UserInfoRestTemplateCustomizer;
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
import com.intita.wschat.domain.ChatMessage;
import com.intita.wschat.domain.SessionProfanity;
import com.intita.wschat.event.LoginEvent;
import com.intita.wschat.event.ParticipantRepository;
import com.intita.wschat.exception.ChatUserNotInRoomException;
import com.intita.wschat.exception.RoomNotFoundException;
import com.intita.wschat.exception.TooMuchProfanityException;
import com.intita.wschat.models.BotCategory;
import com.intita.wschat.models.BotDialogItem;
import com.intita.wschat.models.ChatTenant;
import com.intita.wschat.models.ChatUser;
import com.intita.wschat.models.ConfigParam;
import com.intita.wschat.models.OperationStatus;
import com.intita.wschat.models.OperationStatus.OperationType;
import com.intita.wschat.models.PrivateRoomInfo;
import com.intita.wschat.models.Room;
import com.intita.wschat.models.Room.RoomType;
import com.intita.wschat.models.RoomModelSimple;
import com.intita.wschat.models.User;
import com.intita.wschat.models.UserMessage;
import com.intita.wschat.services.BotCategoryService;
import com.intita.wschat.services.ChatTenantService;
import com.intita.wschat.services.ChatUserLastRoomDateService;
import com.intita.wschat.services.ChatUsersService;
import com.intita.wschat.services.ConfigParamService;
import com.intita.wschat.services.ConsultationsService;
import com.intita.wschat.services.LecturesService;
import com.intita.wschat.services.RoomsService;
import com.intita.wschat.services.UserMessageService;
import com.intita.wschat.services.UsersService;
import com.intita.wschat.util.ProfanityChecker;
import com.intita.wschat.web.BotController.BotParam;
import com.intita.wschat.web.ChatController.CurrentStatusUserRoomStruct;

import jsonview.Views;
//import scala.annotation.meta.setter;


@Controller
public class RoomController {
	final String DIALOG_NAME_PREFIX = "DIALOG_";
	private final static Logger log = LoggerFactory.getLogger(RoomController.class);

	@Autowired(required=true) private HttpServletRequest request;

	@Autowired private ProfanityChecker profanityFilter;

	@Autowired private SessionProfanity profanity;

	@Autowired private ParticipantRepository participantRepository;
	@Autowired private  LecturesService lecturesService;

	@Autowired private SimpMessagingTemplate simpMessagingTemplate;
	@Autowired private ConsultationsService chatIntitaConsultationService;

	@Autowired private RoomsService roomService;
	@Autowired private UsersService userService;
	@Autowired private UserMessageService userMessageService;
	@Autowired private ChatUsersService chatUserServise;
	@Autowired private ChatTenantService chatTenantService;
	@Autowired private ChatUserLastRoomDateService chatUserLastRoomDateService;
	//@Autowired private IntitaConsultationsService chatIntitaConsultationService;
	@Autowired private ConfigParamService configService; 
	@Autowired private ChatController chatController;

	@Autowired private BotCategoryService botCategoryService;
	@Autowired private BotController botController;


	public static class ROLE
	{
		public static final int ADMIN = 256;
	}

	static final private ObjectMapper mapper = new ObjectMapper();

	private static final Queue<SubscribedtoRoomsUsersBufferModal> subscribedtoRoomsUsersBuffer = new ConcurrentLinkedQueue<SubscribedtoRoomsUsersBufferModal>();// key => roomId
	private final Map<Long,ConcurrentLinkedQueue<DeferredResult<String>>> responseRoomBodyQueue =  new ConcurrentHashMap<Long,ConcurrentLinkedQueue<DeferredResult<String>>>();// key => roomId

	private ArrayList<Room> roomsArray; 

	private final Map<String,Queue<DeferredResult<String>>> responseBodyQueueForParticipents =  new ConcurrentHashMap<String,Queue<DeferredResult<String>>>();// key => roomId

	@PostConstruct
	private void postFunction()
	{
		//configService.getParam("chatBotEnable");
	}

	@RequestMapping(value = "/chat/rooms/create/with_bot/", method = RequestMethod.POST)
	@ResponseBody
	public void createDialogWithBotRequesr(@RequestBody String roomName, Principal principal)
	{
		createDialogWithBot(roomName,principal);
	}

	public Room createDialogWithBot(String roomName, Principal principal)
	{
		if(roomName.isEmpty())
			return null;

		ChatUser bot = chatUserServise.getChatUser(BotParam.BOT_ID);

		Room room = roomService.register(roomName, bot);
		ChatUser guest = chatUserServise.getChatUser(principal);
		roomService.addUserToRoom(guest, room);

		//send to user about room apearenced
		Long chatUserId = Long.parseLong(principal.getName());		
		simpMessagingTemplate.convertAndSend("/topic/chat/rooms/user." + chatUserId,getRoomsByAuthorSubscribe(principal, Long.parseLong(principal.getName() )));
		//this said ti author that he nust update room`s list
		addFieldToSubscribedtoRoomsUsersBuffer(new SubscribedtoRoomsUsersBufferModal(guest));

		String containerString = "Good day. Please choose the category that interests you:\n";
		ArrayList<BotCategory> allCategories = botCategoryService.getAll();
		BotDialogItem mainContainer = BotDialogItem.createFromCategories(allCategories);
		mainContainer.setBody( containerString + mainContainer.getBody());
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

		//boolean botEnable = Boolean.parseBoolean(configService.getParam("botEnable").getValue());
		/*ChatTenant greeTenante = chatTenantService.getFreeTenant();
		if (greeTenante == null)
			return null;

		ChatUser roomAuthor = greeTenante.getChatUser();			

		chatTenantService.setTenantBusy(greeTenante);
		 */
		//getRandomTenant().getChatUser();
		ChatUser guest = chatUserServise.getChatUser(principal);
		String roomName =" " + guest.getNickName().substring(0,16)+" "+ new Date().toString();
		Room room = roomService.register(roomName, guest);

		//roomService.addUserToRoom(guest, room);

		//send to user about room apearenced
		Long chatUserId = Long.parseLong(principal.getName());		
		simpMessagingTemplate.convertAndSend("/topic/chat/rooms/user." + chatUserId,getRoomsByAuthorSubscribe(principal, Long.parseLong(principal.getName() )));
		//this said ti author that he nust update room`s list
		addFieldToSubscribedtoRoomsUsersBuffer(new SubscribedtoRoomsUsersBufferModal(guest));		
		botController.register(room, chatUserId);
		botController.runUsersAskTenantsTimer(room);
		return room;
	}

	/**********************
	 * what doing with new auth user 
	 **********************/
	@SubscribeMapping("/chat.login/{userId}")
	public Map<String, String> login(Principal principal, @DestinationVariable("userId") Long userId)//Control user page after auth 
	{

		Map<String, String> result = new HashMap<>();
		if(userId == -1)
			userId = Long.parseLong(principal.getName());

		ChatUser user = chatUserServise.getChatUser(userId);

		if(user == null || Long.parseLong(principal.getName()) != user.getId().longValue())
		{
			ChatUser user_real = chatUserServise.getChatUser(Long.parseLong(principal.getName()));
			if(user_real.getIntitaUser() == null || !userService.isAdmin(user_real.getIntitaUser().getId()))
				return null;
		}
		User iUser = user.getIntitaUser();
		if(iUser == null)
		{
			Room room;
			if(user.getChatUserLastRoomDate().iterator().hasNext()){
				room = user.getChatUserLastRoomDate().iterator().next().getLastRoom();
			}
			else
			{
				/*
				 * ADD BOT TO CHAT
				 */
				//boolean botEnable = Boolean.parseBoolean(configService.getParam("botEnable").getValue());
				boolean botEnable = true;
				ConfigParam s_botEnable = configService.getParam("chatBotEnable");
				if(s_botEnable != null)
					botEnable = Boolean.parseBoolean(s_botEnable.getValue());
				if (botEnable){
					room = createDialogWithBot("BotSys_" + userId + "_" + new Date().toString(), principal);
				}
				else 
					room = createRoomWithTenant(principal);

				//test - no free tenant
				//room = null;

				//send msg about go to guest room if you is tenant with current Id
				/*if (room != null)
					chatController.addFieldToInfoMap("newGuestRoom", room.getId());*/
			}
			//simpMessagingTemplate.convertAndSend("/topic/chat/rooms/user." + user.getId(), roomService.getRoomsModelByChatUser(user));

			if (room != null)
				result.put("nextWindow", room.getId().toString());
			else
				result.put("nextWindow", "-1");
		}
		else
		{
			//subscribedtoRoomsUsersBuffer.add(user);
			result.put("nextWindow", "0");
			result.put("chat_user_avatar", iUser.getAvatar());
		}

		result.put("chat_id", userId.toString());
		result.put("chat_user_nickname", user.getNickName());
		
		

		Integer role = 0;
		if(user.getIntitaUser() != null )
		{
			if (userService.isAdmin(user.getIntitaUser().getId()))
			role |= ROLE.ADMIN;
			//check if tenant
			if (userService.isTenant(user.getIntitaUser().getId()))
			result.put("isTenant", "true");
			else 
			result.put("isTenant", "false");
			//check if trainer
			if (userService.isTrainer(user.getIntitaUser().getId()))
				result.put("isTrainer", "true");
				else 
				result.put("isTrainer", "false");
		}
		result.put("chat_user_role", role.toString());

		String rooms = "{}";
		try {
			rooms = mapper.writeValueAsString(new UpdateRoomsPacketModal(roomService.getRoomsModelByChatUser(user)));
		} catch (JsonProcessingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		result.put("chat_rooms", rooms);
		Long intitaUserId = null==user.getIntitaUser() ? null : user.getIntitaUser().getId();

		try {
			result.put("friends", mapper.writeValueAsString(roomService.getPrivateLoginEvent(user)));
		} catch (JsonProcessingException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

		if(userService.isTrainer(intitaUserId))
		{
			ArrayList<LoginEvent> tenantsObjects =  userService.getAllFreeTenantsLoginEvent(user.getId());
			String tenantsJson = null;
			try {
				tenantsJson = mapper.writeValueAsString(tenantsObjects);
			} catch (JsonProcessingException e) {
				e.printStackTrace();
			}
			result.put("tenants",tenantsJson );
		}

		return result;
	}

	@RequestMapping(value = "/chat/login/{userId}", method = RequestMethod.POST)
	@ResponseBody
	public String retrieveParticipantsLP(Principal principal, @PathVariable("userId") Long userId) throws JsonProcessingException {
		return mapper.writeValueAsString(login(principal, userId));
	}

	/***************************
	 * GET PARTICIPANTS AND LOAD MESSAGE
	 ***************************/


	private Set<LoginEvent> GetParticipants(Room room_o)
	{
		Set<LoginEvent> userList = new HashSet<>();
		Long intitaId = null;
		String avatar = "noname.png";
		User iUser = room_o.getAuthor().getIntitaUser();
		if(iUser != null)
		{
			intitaId =  iUser.getId();
			avatar = iUser.getAvatar();
		}

		LoginEvent currentChatUserLoginEvent = new LoginEvent(intitaId, room_o.getAuthor().getId(),
				room_o.getAuthor().getNickName(), avatar, participantRepository.isOnline(room_o.getAuthor().getId().toString()));
		userList.add(currentChatUserLoginEvent);
		for(ChatUser user : room_o.getUsers())
		{

			intitaId = null;
			avatar = "noname.png";
			iUser = user.getIntitaUser();
			//Bot avatar
			if(user.getId() == BotParam.BOT_ID)
				avatar = BotParam.BOT_AVATAR;

			if(iUser != null)
			{
				intitaId =  iUser.getId();
				avatar = iUser.getAvatar();
			}

			userList.add(new LoginEvent(intitaId, user.getId(),user.getNickName(), avatar, participantRepository.isOnline(user.getId().toString())));
		}
		return  userList;
	}

	public Map<String, Object> retrieveParticipantsSubscribeAndMessagesObj(Room room_o, String lang) {

		Queue<UserMessage> buff = chatController.getMessagesBuffer().get(room_o.getId());
		ArrayList<UserMessage> userMessages = userMessageService.getFirst20UserMessagesByRoom(room_o, lang);
		if(buff != null)
			userMessages.addAll(buff);
		ArrayList<ChatMessage> messagesHistory = ChatMessage.getAllfromUserMessages(userMessages);

		HashMap<String, Object> map = new HashMap();
		map.put("participants", GetParticipants(room_o));
		map.put("messages", messagesHistory);
		map.put("type", room_o.getType());//0-add; 1-private; 2-not my
		try {
			map.put("bot_param", mapper.writerWithView(Views.Public.class).writeValueAsString(room_o.getBotAnswers()));
		} catch (JsonProcessingException e) {
			// TODO Auto-generated catch block
			log.info("BOT PARAM PROBLEM: " + room_o.getBotAnswers());
			e.printStackTrace();

		}//0-add; 1-private; 2-not my
		return map;
	}

	@SubscribeMapping("/{room}/chat.participants/{lang}")
	public Map<String, Object> retrieveParticipantsSubscribeAndMessages(@DestinationVariable("room") Long room, @DestinationVariable("lang") String lang, SimpMessageHeaderAccessor headerAccessor, Principal principal) {//ONLY FOR TEST NEED FIX
		CurrentStatusUserRoomStruct status = ChatController.isMyRoom(room, principal, userService, chatUserServise, roomService);
		if(status == null)
		{

			ChatUser o_object = chatUserServise.getChatUser(principal);
			if(o_object != null)
			{
				User iUser = o_object.getIntitaUser();

				if( iUser == null || !userService.isAdmin(iUser.getId()))
				{
					return new HashMap<String, Object>();
				}
			}
			else
				return new HashMap<String, Object>();
		}

		Room room_o = roomService.getRoom(room);
		return retrieveParticipantsSubscribeAndMessagesObj(room_o, lang);
	}

	@MessageMapping("/{room}/chat.participants")
	public Map<String, Object> retrieveParticipantsMessage(@DestinationVariable Long room) {
		Room room_o = roomService.getRoom(room);
		HashMap<String, Object> map = new HashMap();
		if(room_o != null)
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
	public String retrieveParticipantsAndMessagesLP(@PathVariable("room") Long room, Principal principal) throws JsonProcessingException {
		CurrentStatusUserRoomStruct struct = ChatController.isMyRoom(room, principal, userService, chatUserServise, roomService);//Control room from LP
		if( struct == null)
			return "{}";
		String participantsAndMessages = mapper.writeValueAsString(retrieveParticipantsSubscribeAndMessagesObj(struct.getRoom(), ChatController.getCurrentLang()));
		log.info("P&M:"+participantsAndMessages);
		return participantsAndMessages;
	}

	@RequestMapping(value = "/{room}/chat/participants/update", method = RequestMethod.POST,produces = "text/plain;charset=UTF-8")
	@ResponseBody
	public DeferredResult<String> retrieveParticipantsUpdateLP(@PathVariable("room") Long room, Principal principal) throws JsonProcessingException {

		Long timeOut = 5000L;
		DeferredResult<String> result = new DeferredResult<String>(timeOut, "{}");
		Queue<DeferredResult<String>> queue = responseBodyQueueForParticipents.get(room);
		if(queue == null)
		{
			queue = new ConcurrentLinkedQueue<DeferredResult<String>>();
		}
		CurrentStatusUserRoomStruct struct = ChatController.isMyRoom(room, principal, userService, chatUserServise, roomService);//Control room from LP
		if(struct != null)
		{
			responseBodyQueueForParticipents.put(room.toString(), queue);		
			queue.add(result);
		}
		else result.setErrorResult(new ChatUserNotInRoomException(""));

		return result;
	}

	/*
	 * call only if is need
	 */
	@Scheduled(fixedDelay=15000L)
	public void updateParticipants() {
		for(String key : responseBodyQueueForParticipents.keySet())
		{
			Long longKey = 0L;
			boolean status = true;
			try{
				longKey = Long.parseLong(key);
			}
			catch(NumberFormatException e){
				log.info("Participants update error:"+e.getMessage());
				status = false;
			}
			Room room_o = null;
			HashMap<String, Object> result = null;
			if (status)
			{
				room_o=roomService.getRoom(longKey);
				result = new HashMap();
			}
			if(room_o != null)
				result.put("participants", GetParticipants(room_o));

			for(DeferredResult<String> response : responseBodyQueueForParticipents.get(key))
			{
				//response.setResult("");
				try {
					if(!response.isSetOrExpired())
						response.setResult(mapper.writeValueAsString(result));
					else response.setResult("{}");
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
	 * @throws JsonProcessingException 
	 ***************************/

	@RequestMapping(value="/chat/rooms/roomInfo/{roomID}", method=RequestMethod.POST)
	@ResponseBody
	public String getRoomInfo( @PathVariable("roomID") Long roomId, Principal principal) throws JsonProcessingException {
		CurrentStatusUserRoomStruct struct = ChatController.isMyRoom(roomId, principal, userService, chatUserServise, roomService);//Control room from LP
		if( struct == null)
			return "{}";
		RoomModelSimple sb = new RoomModelSimple(struct.getUser(), 0 , new Date().toString(), struct.getRoom(),userMessageService.getLastUserMessageByRoom(struct.getRoom()));
		return mapper.writeValueAsString(sb);
	}

	/*********************
	 * Generation/return of new special room for two users 
	 * Tenatn/Trainer more prefered as authoror
	 * @return id of room
	 * @exception RoomNotFoundException (users not founded)
	 ********************/
	public Room getPrivateRoom(ChatUser chatUser, ChatUser privateCharUser)
	{
		if(privateCharUser == null || chatUser == null)
			throw new RoomNotFoundException("privateChatUser or chatUser is null");

		if(chatUser.getIntitaUser() == null || privateCharUser.getIntitaUser() == null)
			throw new RoomNotFoundException("Intita use is null");

		Room room  = roomService.getPrivateRoom(chatUser, privateCharUser);

		ChatUser author = chatUser;
		ChatUser other = privateCharUser;
		User iPrivateUser = privateCharUser.getIntitaUser(); 
		if(userService.isTenant(iPrivateUser.getId()) || userService.isTrainer(iPrivateUser.getId()))
		{
			author = privateCharUser;
			other = chatUser;
		}

		if(room == null)
		{
			room = roomService.registerPrivate(author, other);// private room type => 1

			simpMessagingTemplate.convertAndSend("/topic/chat/rooms/user." + chatUser.getId(), new UpdateRoomsPacketModal(roomService.getRoomsModelByChatUser(chatUser)));
			if (chatUser.getId()!= privateCharUser.getId())
				simpMessagingTemplate.convertAndSend("/topic/chat/rooms/user." + privateCharUser.getId(), new UpdateRoomsPacketModal(roomService.getRoomsModelByChatUser(privateCharUser)));	

			OperationStatus operationStatus = new OperationStatus(OperationType.ADD_ROOM_FROM_TENANT, true, ""+room.getId());
			String subscriptionStr = "/topic/users/" + chatUser.getId() + "/status";
			simpMessagingTemplate.convertAndSend(subscriptionStr, operationStatus);
		}
		return room;
	}

	@RequestMapping(value="/chat/rooms/private/{userID}", method=RequestMethod.POST)
	@ResponseBody
	public String getPrivateRoomRequest( @PathVariable("userID") Long userId, Principal principal) throws JsonProcessingException {
		log.info("getPrivateRoom");
		ChatUser privateCharUser = chatUserServise.getChatUserFromIntitaId(userId, false);
		ChatUser chatUser = chatUserServise.getChatUser(principal);
		Room room = getPrivateRoom(chatUser, privateCharUser);
		return mapper.writeValueAsString(room.getId());//@BAG@
	}

	@RequestMapping(value="/chat/go/rooms/private/trainer", method=RequestMethod.GET)
	public String goPrivateRoomWithTrainer(Principal principal) throws JsonProcessingException {
		ChatUser principalChatUser = chatUserServise.getChatUser(principal);

		User iPrincipalUser = principalChatUser.getIntitaUser();
		ChatUser trainer = null;
		try{
			if(iPrincipalUser == null)
				throw new RoomNotFoundException("is west!!!");
			User iTrainer = userService.getTrainer(iPrincipalUser.getId());
			if(iTrainer == null)
				throw new RoomNotFoundException("user dont have trainer!!!");
			trainer = chatUserServise.getChatUserFromIntitaUser(iTrainer, false);
			return "redirect:/#/dialog_view/" + getPrivateRoom(trainer, principalChatUser).getId();
		}
		catch (RoomNotFoundException ex){
			log.info("goPrivateRoomWithTrainer ::: " + ex.getMessage());
			return "redirect:/";
		}
	}
	
	@RequestMapping(value="/chat/go/rooms/private/{userId}", method=RequestMethod.GET)
	public String goPrivateRoom(@PathVariable Long userId, @RequestParam(required = false, name = "isChatId") Boolean isChatId,   Principal principal) throws JsonProcessingException {
		ChatUser principalChatUser = chatUserServise.getChatUser(principal);

		ChatUser cUser = null;
		if(isChatId != null && isChatId == true)
		{
			cUser = chatUserServise.getChatUser(userId);
		}
		else{
			User iTargetUser = userService.getById(userId);
			cUser = chatUserServise.getChatUserFromIntitaUser(iTargetUser, false);
		}

		try{
			if(cUser == null)
			{
				throw new RoomNotFoundException("target user not registered!!!");
			}
			return "redirect:/#/dialog_view/" + getPrivateRoom(cUser, principalChatUser).getId();
		}
		catch (RoomNotFoundException ex){
			log.info("goPrivateRoomWithUser ::: " + ex.getMessage());
			return "redirect:/";
		}
	}

	// @SubscribeMapping("/chat/rooms/user.{userId}")
	public UpdateRoomsPacketModal getRoomsByAuthorSubscribe(Principal principal, @DestinationVariable Long userId) { //000
		ChatUser user = chatUserServise.getChatUser(userId);

		if(user == null || Long.parseLong(principal.getName()) != user.getId().longValue())
		{
			ChatUser user_real = chatUserServise.getChatUser(Long.parseLong(principal.getName()));
			if(user_real.getIntitaUser() == null || !userService.isAdmin(user_real.getIntitaUser().getId()))
				return null;
		}

		return new UpdateRoomsPacketModal(roomService.getRoomsModelByChatUser(user));
	}

	@MessageMapping("/chat/rooms/add.{name}")
	public void addRoomByAuthor( @DestinationVariable("name") String name, Principal principal) {
		Long chatUserId = Long.parseLong(principal.getName());
		ChatUser user = chatUserServise.getChatUser(chatUserId);
		Room room = roomService.register(name, user);
		//send to user about room apearenced
		simpMessagingTemplate.convertAndSend("/topic/chat/rooms/user." + chatUserId,getRoomsByAuthorSubscribe(principal, Long.parseLong(principal.getName() )));
		boolean operationSuccess = true;
		if (room == null)
			operationSuccess = false;
		OperationStatus operationStatus = new OperationStatus(OperationType.ADD_ROOM,operationSuccess,"ADD ROOM");
		String subscriptionStr = "/topic/users/" + chatUserId + "/status";
		//send to user that operation success
		simpMessagingTemplate.convertAndSend(subscriptionStr, operationStatus);
	}


	//LONG POLLING PART

	@RequestMapping(value="/chat/rooms/adddialogwithuser",method=RequestMethod.POST)
	@ResponseBody
	public Long addDialog(Principal principal,@RequestBody Long chatUserId){
		ChatUser auth = chatUserServise.getChatUser(principal);
		if (auth==null)return -1L;
		User authorOfDialog = auth.getIntitaUser();
		if (authorOfDialog==null) return -1L;
		User interlocutor = userService.getUserFromChat(chatUserId);
		if (interlocutor==null) return -1L;
		String roomName = DIALOG_NAME_PREFIX+authorOfDialog.getLogin()+"_"+interlocutor.getLogin();
		Room room = roomService.register(roomName, auth);
		return room.getId();
	}

	@RequestMapping(value="/chat/rooms/user/{username}",method=RequestMethod.POST)
	@ResponseBody
	public DeferredResult<String> getRooms(Principal principal) {
		if(principal == null)
			return null;
		Long timeOut = 1000000L;
		DeferredResult<String> deferredResult = new DeferredResult<String>(timeOut, "NULL");
		Long chatUserId = Long.parseLong(principal.getName());

		ConcurrentLinkedQueue<DeferredResult<String>> queue = responseRoomBodyQueue.get(chatUserId);
		if(queue == null)
		{
			queue = new ConcurrentLinkedQueue<DeferredResult<String>>();		
		}

		responseRoomBodyQueue.put(chatUserId, queue);		
		queue.add(deferredResult);
		//	System.out.println("responseRoomBodyQueue queue_count:"+queue.size());

		return deferredResult;
	}

	@Scheduled(fixedDelay=2500L)
	public void processRoomsQueues() throws JsonProcessingException {
		for(SubscribedtoRoomsUsersBufferModal modal : subscribedtoRoomsUsersBuffer)
		{
			if (modal == null || modal.chatUser==null){
				System.out.println("WARNING: NULL USER");
				continue;
			}
			Queue<DeferredResult<String>> responseList = responseRoomBodyQueue.get(modal.chatUser.getId());
			if (responseList==null){
				//System.out.println("WARNING: RESPONSE LIST IS CLEAR");
				continue;
			}
			for(DeferredResult<String> response : responseList)
			{

				String str;
				if(modal.replace)
					str = mapper.writeValueAsString(new UpdateRoomsPacketModal(roomService.getRoomsModelByChatUser(modal.chatUser), modal.replace));
				else
					str = mapper.writeValueAsString(new UpdateRoomsPacketModal(roomService.getRoomsByChatUserAndList(modal.chatUser, modal.roomsForUpdate), modal.replace));

				if(!response.isSetOrExpired())
					response.setResult(str);
			}
			responseRoomBodyQueue.remove(modal.chatUser.getId());
			subscribedtoRoomsUsersBuffer.remove(modal);
		}
		//System.out.println("responseRoomBodyQueue queue_count:"+responseRoomBodyQueue.size());
		//subscribedtoRoomsUsersBuffer.clear();//!!!

	}

	@RequestMapping(value="/chat/rooms/add",method=RequestMethod.POST)
	@ResponseBody
	//@SendToUser(value = "/exchange/amq.direct/errors", broadcast = false)
	public void addRoomByAuthorLP( @RequestBody String roomName, Principal principal) {
		System.out.println("OkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkAdd");//@LOG@

		System.out.println(principal.getName());//@LOG@
		Long chatUserId = Long.parseLong(principal.getName());
		ChatUser author = chatUserServise.getChatUser(chatUserId);
		Room room = roomService.register(roomName, author);
		addFieldToSubscribedtoRoomsUsersBuffer(new SubscribedtoRoomsUsersBufferModal(author));
	}
	/***************************
	 * REMOVE/ADD USERS FROM/TO ROOMS
	 ***************************/

	boolean removeUserFromRoomFully( ChatUser user_o, Room room_o, Principal principal, boolean ignoreAuthor)
	{
		ChatUser authorUser = chatUserServise.getChatUser(principal);
		boolean haveNullObj = room_o == null || user_o == null;
		boolean isAuthor = user_o.getId().longValue() == room_o.getAuthor().getId().longValue();
		boolean currentUserIsAuthor = authorUser.getId().longValue() == room_o.getAuthor().getId().longValue();
		boolean permitions = (room_o.getPermissions(authorUser) & Room.Permissions.REMOVE) == Room.Permissions.REMOVE;
		if( haveNullObj || isAuthor || (!( permitions || currentUserIsAuthor) || !room_o.isActive()) && !ignoreAuthor)
		{
			return false;
		}
		return removeUserFromRoomFullyWithoutCheckAuthorization(user_o, room_o);
	}

	/*
	 * Only for remove self from room
	 */
	boolean removeUserFromRoomFullyWithoutCheckAuthorization( ChatUser user_o, Room room_o){
		//check for BOT
		if(user_o.getId() == BotParam.BOT_ID)
			return false;
		//check for private room
		if(room_o.getType() == RoomType.PRIVATE)
		{
			PrivateRoomInfo info = roomService.getPrivateRoomInfo(room_o);
			if(user_o == info.getFirtsUser() || user_o == info.getSecondUser())
				return false;
		}

		roomService.removeUserFromRoom(user_o, room_o);
		chatUserLastRoomDateService.removeUserLastRoomDate(user_o, room_o);

		addFieldToSubscribedtoRoomsUsersBuffer(new SubscribedtoRoomsUsersBufferModal(user_o));
		updateParticipants();//force update
		try {
			processRoomsQueues();
		} catch (JsonProcessingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		simpMessagingTemplate.convertAndSend("/topic/" + room_o.getId().toString() + "/chat.participants", retrieveParticipantsMessage(room_o.getId()));
		simpMessagingTemplate.convertAndSend("/topic/chat/rooms/user." + user_o.getId(), new UpdateRoomsPacketModal(roomService.getRoomsModelByChatUser(user_o)));
		return true;
	}

	@Transactional
	public boolean changeAuthor(ChatUser newAuthor, Room room, Principal principal,boolean ignoreAuthor) {
		if(room == null || newAuthor == null)
			return false;
		ChatUser author = room.getAuthor(); 
		if(author.equals(newAuthor))
			return true;

		boolean contain = false;
		if(room.getUsers().contains(newAuthor))
		{
			//delete from LIST of add users && check for remove from update
			room.removeUser(newAuthor);
			contain = true;
		}
		roomService.setAuthor(newAuthor, room);
		if(!roomService.update(room))
			return false;
		//remove room from cache author object
		author.getRootRooms().remove(room);

		addUserToRoom( author, room, principal, true);
		if(!contain)
		{
			addFieldToSubscribedtoRoomsUsersBuffer(new SubscribedtoRoomsUsersBufferModal(newAuthor));
			updateParticipants();

			simpMessagingTemplate.convertAndSend("/topic/" + newAuthor.getId().toString() + "/chat.participants", retrieveParticipantsMessage(room.getId()));
			simpMessagingTemplate.convertAndSend("/topic/chat/rooms/user." + newAuthor.getId(), new UpdateRoomsPacketModal(roomService.getRoomsModelByChatUser(newAuthor)));

		}
		return true;	

	}

	boolean addUserToRoom( ChatUser user_o, Room room_o, Principal principal,boolean ignoreAuthor)
	{
		Long chatUserAuthorId = Long.parseLong(principal.getName());
		ChatUser authorUser = chatUserServise.getChatUser(chatUserAuthorId);

		boolean haveNullObj = room_o == null || user_o == null;
		boolean isAuthor = user_o.getId().longValue() == room_o.getAuthor().getId().longValue();
		boolean currentUserIsAuthor = authorUser.getId().longValue() == room_o.getAuthor().getId().longValue();
		boolean permitions = (room_o.getPermissions(authorUser) & Room.Permissions.ADD) == Room.Permissions.ADD;
		if( haveNullObj || isAuthor || (!( permitions || currentUserIsAuthor) || !room_o.isActive()) && !ignoreAuthor)
		{
			return false;
		}

		Set<Room> all = user_o.getRoomsFromUsers();
		all.addAll(user_o.getRootRooms());
		if(all.contains(room_o))
			return false;

		roomService.addUserToRoom(user_o, room_o);

		addFieldToSubscribedtoRoomsUsersBuffer(new SubscribedtoRoomsUsersBufferModal(user_o));
		updateParticipants();

		simpMessagingTemplate.convertAndSend("/topic/" + room_o.getId().toString() + "/chat.participants", retrieveParticipantsMessage(room_o.getId()));
		simpMessagingTemplate.convertAndSend("/topic/chat/rooms/user." + user_o.getId(), new UpdateRoomsPacketModal(roomService.getRoomsModelByChatUser(user_o)));
		return true;
	}

	boolean addUserToRoomFn( String nickName, Long room, Principal principal,boolean ws)
	{
		Room room_o = roomService.getRoom(room);
		ChatUser user_o = chatUserServise.getChatUserFromIntitaEmail(nickName, false);//INTITA USER SEARCH
		addUserToRoom(user_o, room_o, principal, false);
		return true;
	}


	@MessageMapping("/chat/rooms.{room}/user.add.{email}")
	public void addUserToRoom( @DestinationVariable("email") String email, @DestinationVariable("room") Long room, Principal principal) {
		System.out.println("OkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkAddUser");//@LOG@

		//System.out.println(login);//@LOG@
		System.out.println(room);//@LOG@
		Long chatUserId = 0L;
		chatUserId = Long.parseLong(principal.getName());
		ChatUser user_o = chatUserServise.getChatUserFromIntitaEmail(email, false);// @BAG@

		if(!addUserToRoomFn(email, room, principal,true))
		{
			OperationStatus operationStatus = new OperationStatus(OperationType.ADD_USER_TO_ROOM,false,"ADD USER TO ROOM");
			String subscriptionStr = "/topic/users/"+chatUserId+"/status";
			simpMessagingTemplate.convertAndSend(subscriptionStr, operationStatus);
			return;
		}



		OperationStatus operationStatus = new OperationStatus(OperationType.ADD_USER_TO_ROOM,true,"ADD USER TO ROOM");
		String subscriptionStr = "/topic/users/"+chatUserId+"/status";
		simpMessagingTemplate.convertAndSend(subscriptionStr, operationStatus);

	}

	@RequestMapping(value = "/chat/rooms.{room}/user.add.{nickName}", method = RequestMethod.POST)
	public 	@ResponseBody String addUserToRoomLP(@PathVariable("room") Long roomId, @PathVariable("nickName") String nickName, Principal principal, HttpRequest req) throws InterruptedException, JsonProcessingException {
		return mapper.writeValueAsString(addUserToRoomFn(nickName, roomId, principal,false));
	}

	@RequestMapping(value="/chat/rooms/{room}/remove", method = RequestMethod.POST)
	@ResponseBody
	public boolean removeRoomFromList(@PathVariable("room") Long room, Principal principal) {
		Room room_o = roomService.getRoom(room);
		ChatUser user_o = chatUserServise.getChatUser(principal);
		if(room_o == null || user_o == null)
			return false;
		if(room_o.getAuthor().getId().equals(user_o.getId()))
		{
			room_o.setActive(true);
			for(ChatUser user : room_o.getChatUsers())
			{
				removeUserFromRoomFully(user, room_o, principal, false);
			}
			room_o.setActive(false);
			roomService.update(room_o);
		}
		return removeUserFromRoomFullyWithoutCheckAuthorization(user_o, room_o);

	}
	@RequestMapping(value="/chat/user/friends", method = RequestMethod.POST)
	@ResponseBody
	public String userFriends(Principal principal) {
		ChatUser user = chatUserServise.getChatUser(principal);
		try {
			return mapper.writeValueAsString(roomService.getPrivateLoginEvent(user));
		} catch (JsonProcessingException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		return "{}";
	}


	@RequestMapping(value="/chat/rooms.{room}/user.remove/{id}", method = RequestMethod.POST)
	@ResponseBody
	public boolean removeUserFromRoomRequest( @PathVariable("id") Long id, @PathVariable("room") Long room, Principal principal) {
		Room room_o = roomService.getRoom(room);
		ChatUser user_o = chatUserServise.getChatUser(id);

		return removeUserFromRoomFully(user_o, room_o, principal, false);
	}

	public static void addFieldToSubscribedtoRoomsUsersBuffer(SubscribedtoRoomsUsersBufferModal modal)
	{
		subscribedtoRoomsUsersBuffer.add(modal);
	}

	private Map<Long, String> convertToNameList(ArrayList<Room> list)
	{
		Map<Long, String> res = new HashMap<Long, String>();
		for(Room r :list)
		{
			res.put(r.getId(), r.getName());
		}
		return res;
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
		log.error("NumberFormatException handler executed:"+ex.getMessage());
		return "NumberFormatException handler executed:"+ex.getMessage();
	}
	@MessageExceptionHandler(Exception.class)
	public String handleMessageException(Exception ex) {
		log.error("Exception handler executed:");
		ex.printStackTrace();
		return "NumberFormatException handler executed";
	}

	public static class UpdateRoomsPacketModal implements Serializable{
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

	public static class SubscribedtoRoomsUsersBufferModal{
		ChatUser chatUser;
		boolean replace = true;
		ArrayList<Room> roomsForUpdate;

		public SubscribedtoRoomsUsersBufferModal() {
			chatUser = null;
		}
		public SubscribedtoRoomsUsersBufferModal(ChatUser chatUser, ArrayList<Room> arr)
		{
			this.chatUser = chatUser;
			replace = false;
			roomsForUpdate = arr;

		}
		public SubscribedtoRoomsUsersBufferModal(ChatUser chatUser)
		{
			this.chatUser = chatUser;
			replace = true;
		}
	}

	@PostConstruct
	private void PostConstructor()
	{
		participantRepository.addParticipantPresenceByLastConnectionTime("" + BotParam.BOT_ID);//BOT online OK
	}
}