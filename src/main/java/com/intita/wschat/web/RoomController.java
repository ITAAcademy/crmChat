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
import java.util.Random;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpRequest;
import org.springframework.messaging.MessageDeliveryException;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageExceptionHandler;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.annotation.SendToUser;
import org.springframework.messaging.simp.annotation.SubscribeMapping;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.context.request.async.DeferredResult;
import org.springframework.web.servlet.ModelAndView;

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
import com.intita.wschat.models.OperationStatus;
import com.intita.wschat.models.OperationStatus.OperationType;
import com.intita.wschat.models.Room;
import com.intita.wschat.models.RoomModelSimple;
import com.intita.wschat.models.User;
import com.intita.wschat.models.UserMessage;
import com.intita.wschat.services.BotCategoryService;
import com.intita.wschat.services.ChatTenantService;
import com.intita.wschat.services.ChatUserLastRoomDateService;
import com.intita.wschat.services.ChatUsersService;
import com.intita.wschat.services.RoomsService;
import com.intita.wschat.services.UserMessageService;
import com.intita.wschat.services.UsersService;
import com.intita.wschat.util.HtmlUtility;
import com.intita.wschat.util.ProfanityChecker;
import com.intita.wschat.web.BotController.BotParam;
import com.intita.wschat.web.ChatController.CurrentStatusUserRoomStruct;

import jsonview.Views;


@Controller
public class RoomController {
	final String DIALOG_NAME_PREFIX = "DIALOG_";
	private final static Logger log = LoggerFactory.getLogger(RoomController.class);

	@Autowired private ProfanityChecker profanityFilter;

	@Autowired private SessionProfanity profanity;

	@Autowired private ParticipantRepository participantRepository;

	@Autowired private SimpMessagingTemplate simpMessagingTemplate;

	@Autowired private RoomsService roomService;
	@Autowired private UsersService userService;
	@Autowired private UserMessageService userMessageService;
	@Autowired private ChatUsersService chatUserServise;
	@Autowired private ChatTenantService chatTenantService;
	@Autowired private ChatUserLastRoomDateService chatUserLastRoomDateService;

	@Autowired private ChatController chatController;
	
	@Autowired private BotCategoryService botCategoryService;

	
	public static class ROLE
	{
		public static final int ADMIN = 256;
	}

	static final private ObjectMapper mapper = new ObjectMapper();

	private static final Queue<SubscribedtoRoomsUsersBufferModal> subscribedtoRoomsUsersBuffer = new ConcurrentLinkedQueue<SubscribedtoRoomsUsersBufferModal>();// key => roomId
	private final Map<Long,ConcurrentLinkedQueue<DeferredResult<String>>> responseRoomBodyQueue =  new ConcurrentHashMap<Long,ConcurrentLinkedQueue<DeferredResult<String>>>();// key => roomId

	private ArrayList<Room> roomsArray; 

	private final Map<String,Queue<DeferredResult<String>>> responseBodyQueueForParticipents =  new ConcurrentHashMap<String,Queue<DeferredResult<String>>>();// key => roomId

		
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
		roomService.addUserToRoom(chatUserServise.getChatUser(principal), room);
		OperationStatus operationStatus = new OperationStatus(OperationType.ADD_ROOM_ON_LOGIN,true,""+room.getId());
		String subscriptionStr = "/topic/users/" + bot.getId() + "/status";
		simpMessagingTemplate.convertAndSend(subscriptionStr, operationStatus);
		ArrayList<BotCategory> allCategories = botCategoryService.getAll();
		BotDialogItem mainContainer = BotDialogItem.createFromCategories(allCategories);
		String containerString = "";
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
			if(user_real.getIntitaUser() == null || !userService.isAdmin(user_real.getIntitaUser().getId().toString()))
				return null;
		}

		if(user.getIntitaUser() == null)
		{
			Room room;
			if(user.getRoomsFromUsers().iterator().hasNext()){
				room = user.getRoomsFromUsers().iterator().next();
			}
			else
			{
				/*
				 * ADD BOT TO CHAT
				 */

				room = createDialogWithBot("BotSys_" + userId + "_" + new Date().toString(), principal);
				chatController.addFieldToInfoMap("newGuestRoom", room.getId());
			}
			//simpMessagingTemplate.convertAndSend("/topic/chat/rooms/user." + user.getId(), roomService.getRoomsModelByChatUser(user));


			result.put("nextWindow", room.getId().toString());
		}
		else
		{
			//subscribedtoRoomsUsersBuffer.add(user);
			result.put("nextWindow", "0");
		}

		result.put("chat_id", userId.toString());
		result.put("chat_user_nickname", user.getNickName());

		Integer role = 0;
		if(user.getIntitaUser() != null && userService.isAdmin(user.getIntitaUser().getId().toString()))
		{
			role |= ROLE.ADMIN;
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

	public Map<String, Object> retrieveParticipantsSubscribeAndMessagesObj(Room room_o) {

		Queue<UserMessage> buff = chatController.getMessagesBuffer().get(room_o.getId());
		ArrayList<UserMessage> userMessages = userMessageService.getFirst20UserMessagesByRoom(room_o);
		if(buff != null)
			userMessages.addAll(buff);
		ArrayList<ChatMessage> messagesHistory = ChatMessage.getAllfromUserMessages(userMessages);

		HashMap<String, Object> map = new HashMap();
		map.put("participants", GetParticipants(room_o));
		map.put("messages", messagesHistory);
		map.put("type", room_o.getType());//0-add; 1-private; 2-not my
		return map;
	}

	@SubscribeMapping("/{room}/chat.participants")
	public Map<String, Object> retrieveParticipantsSubscribeAndMessages(@DestinationVariable("room") Long room, Principal principal) {//ONLY FOR TEST NEED FIX
		CurrentStatusUserRoomStruct status = ChatController.isMyRoom(room, principal, userService, chatUserServise, roomService); 
		if(status == null)
		{
			ChatUser o_object = chatUserServise.getChatUser(principal);
			if(o_object != null)
			{
				User iUser = o_object.getIntitaUser();

				if( iUser == null || !userService.isAdmin(iUser.getId().toString()))
				{
					return new HashMap<String, Object>();
				}
			}
			else
				return new HashMap<String, Object>();
		}

		Room room_o = roomService.getRoom(room);
		return retrieveParticipantsSubscribeAndMessagesObj(room_o);
	}

	@MessageMapping("/{room}/chat.participants")
	public Map<String, Object> retrieveParticipantsMessage(@DestinationVariable Long room) {
		Room room_o = roomService.getRoom(room);
		HashMap<String, Object> map = new HashMap();
		if(room_o != null)
			map.put("participants", GetParticipants(room_o));
		return map;
	}

	@RequestMapping(value = "/{room}/chat/participants_and_messages", method = RequestMethod.POST)
	@ResponseBody
	public String retrieveParticipantsAndMessagesLP(@PathVariable("room") Long room, Principal principal) throws JsonProcessingException {
		CurrentStatusUserRoomStruct struct = ChatController.isMyRoom(room, principal, userService, chatUserServise, roomService);//Control room from LP
		if( struct == null)
			return "{}";
		String participantsAndMessages = mapper.writeValueAsString(retrieveParticipantsSubscribeAndMessagesObj(struct.getRoom()));
		log.info("P&M:"+participantsAndMessages);
		return participantsAndMessages;
	}

	@RequestMapping(value = "/{room}/chat/participants/update", method = RequestMethod.POST)
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
	@Scheduled(fixedDelay=3000L)
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
		RoomModelSimple sb = new RoomModelSimple(0 , new Date().toString(), struct.getRoom(),userMessageService.getLastUserMessageByRoom(struct.getRoom()));
		return mapper.writeValueAsString(sb);
	}


	@RequestMapping(value="/chat/rooms/private/{userID}", method=RequestMethod.POST)
	@ResponseBody
	public String getPrivateRoom( @PathVariable("userID") Long userId, Principal principal) throws JsonProcessingException {
		log.info("getPrivateRoom");
		ChatUser privateCharUser = chatUserServise.getChatUserFromIntitaId(userId, false);
		if(privateCharUser == null)
			throw new RoomNotFoundException("privateChatUser is null");
		ChatUser chatUser = chatUserServise.getChatUser(principal);
		if(chatUser.getIntitaUser() == null)
			throw new RoomNotFoundException("Intita use is null");

		Room room  = roomService.getPrivateRoom(chatUser, privateCharUser);
		if(room == null)
			room  = roomService.getPrivateRoom(privateCharUser, chatUser);

		if(room == null)
		{
			room = roomService.register(chatUser.getNickName() + "_" + privateCharUser.getNickName(), chatUser, (short) 1);// private room type => 1
			if (chatUser.getId()!= privateCharUser.getId())
				roomService.addUserToRoom(privateCharUser, room);
			simpMessagingTemplate.convertAndSend("/topic/chat/rooms/user." + chatUser.getId(), new UpdateRoomsPacketModal(roomService.getRoomsModelByChatUser(chatUser)));
			if (chatUser.getId()!= privateCharUser.getId())
				simpMessagingTemplate.convertAndSend("/topic/chat/rooms/user." + privateCharUser.getId(), new UpdateRoomsPacketModal(roomService.getRoomsModelByChatUser(privateCharUser)));	

			OperationStatus operationStatus = new OperationStatus(OperationType.ADD_ROOM_FROM_TENANT,true,""+room.getId());
			String subscriptionStr = "/topic/users/" + userId + "/status";
			simpMessagingTemplate.convertAndSend(subscriptionStr, operationStatus);
		}

		return mapper.writeValueAsString(room.getId());//@BAG@

	}

	@SubscribeMapping("/chat/rooms/user.{userId}")
	public UpdateRoomsPacketModal getRoomsByAuthorSubscribe(Principal principal, @DestinationVariable Long userId) { //000
		ChatUser user = chatUserServise.getChatUser(userId);

		if(user == null || Long.parseLong(principal.getName()) != user.getId().longValue())
		{
			ChatUser user_real = chatUserServise.getChatUser(Long.parseLong(principal.getName()));
			if(user_real.getIntitaUser() == null || !userService.isAdmin(user_real.getIntitaUser().getId().toString()))
				return null;
		}

		return new UpdateRoomsPacketModal(roomService.getRoomsModelByChatUser(user));
	}

	@MessageMapping("/chat/rooms/add.{name}")
	public void addRoomByAuthor( @DestinationVariable("name") String name, Principal principal) {
		System.out.println("111111111111111111111111111111111");
		Long chatUserId = Long.parseLong(principal.getName());
		ChatUser user = chatUserServise.getChatUser(chatUserId);
		Room room = roomService.register(name, user);
		simpMessagingTemplate.convertAndSend("/topic/chat/rooms/user." + chatUserId,getRoomsByAuthorSubscribe(principal, Long.parseLong(principal.getName() )));
		boolean operationSuccess = true;
		if (room==null)operationSuccess = false;
		OperationStatus operationStatus = new OperationStatus(OperationType.ADD_ROOM,operationSuccess,"ADD ROOM");
		String subscriptionStr = "/topic/users/" + chatUserId + "/status";
		simpMessagingTemplate.convertAndSend(subscriptionStr, operationStatus);
	}


	//LONG POLLING PART

	@RequestMapping(value="/chat/rooms/adddialogwithuser",method=RequestMethod.POST)
	@ResponseBody
	public Long addDialog(Principal principal,@RequestBody Long chatUserId){
		System.out.println("2222222222222222222222222222");
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
		}
		//System.out.println("responseRoomBodyQueue queue_count:"+responseRoomBodyQueue.size());
		subscribedtoRoomsUsersBuffer.clear();//!!!

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
	 * REMOVE/ADD USERS FROM ROOMS
	 ***************************/

	
	boolean addUserToRoom( ChatUser user_o, Room room_o, Principal principal,boolean ws)
	{
		Long chatUserAuthorId = Long.parseLong(principal.getName());
		ChatUser authorUser = chatUserServise.getChatUser(chatUserAuthorId);

		if(room_o == null || user_o == null || authorUser.getId().longValue() != room_o.getAuthor().getId().longValue() || !room_o.isActive())
		{
			return false;
		}
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
		addUserToRoom(user_o, room_o, principal, ws);
		return true;
	}


	@MessageMapping("/chat/rooms.{room}/user.add.{nickName}")
	public void addUserToRoom( @DestinationVariable("nickName") String nickName, @DestinationVariable("room") Long room, Principal principal) {
		System.out.println("OkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkAddUser");//@LOG@

		//System.out.println(login);//@LOG@
		System.out.println(room);//@LOG@
		Long chatUserId = 0L;
		chatUserId = Long.parseLong(principal.getName());
		ChatUser user_o = chatUserServise.getChatUserFromIntitaEmail(nickName, false);// @BAG@

		if(!addUserToRoomFn(nickName, room, principal,true))
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

	/*****************
	 * dont use at now
	 ****************/
	@MessageMapping("/chat/rooms.{room}/user.remove.{login}")
	public boolean removeUserFromRoom( @DestinationVariable("login") String login, @DestinationVariable("room") Long room, Principal principal) {
		System.out.println("OkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkDeleteUser");//@LOG@

		Room room_o = roomService.getRoom(room);
		User user_o = userService.getUser(login);

		User user = userService.getUser(principal.getName());

		if(user.getId() != room_o.getAuthor().getId())
			return false;

		System.out.println(Boolean.toString(roomService.removeUserFromRoom(user_o, room_o)));
		return true;
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
		participantRepository.add("0");
	}
}