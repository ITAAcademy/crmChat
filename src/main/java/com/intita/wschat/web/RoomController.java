package com.intita.wschat.web;

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

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpRequest;
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
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.context.request.async.DeferredResult;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.intita.wschat.domain.ChatMessage;
import com.intita.wschat.domain.SessionProfanity;
import com.intita.wschat.event.LoginEvent;
import com.intita.wschat.event.ParticipantRepository;
import com.intita.wschat.exception.TooMuchProfanityException;
import com.intita.wschat.models.ChatTenant;
import com.intita.wschat.models.ChatUser;
import com.intita.wschat.models.OperationStatus;
import com.intita.wschat.models.OperationStatus.OperationType;
import com.intita.wschat.models.Room;
import com.intita.wschat.models.User;
import com.intita.wschat.models.UserMessage;
import com.intita.wschat.services.ChatTenantService;
import com.intita.wschat.services.ChatUserLastRoomDateService;
import com.intita.wschat.services.ChatUsersService;
import com.intita.wschat.services.RoomsService;
import com.intita.wschat.services.RoomsService.StringIntDate;
import com.intita.wschat.services.UserMessageService;
import com.intita.wschat.services.UsersService;
import com.intita.wschat.util.ProfanityChecker;
import com.intita.wschat.web.ChatController.CurrentStatusUserRoomStruct;


@Controller
public class RoomController {
	final String DIALOG_NAME_PREFIX = "DIALOG_";
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
	
	static final private ObjectMapper mapper = new ObjectMapper();

	private final Queue<ChatUser> subscribedtoRoomsUsersBuffer = new ConcurrentLinkedQueue<ChatUser>();// key => roomId
	private final Map<Long,ConcurrentLinkedQueue<DeferredResult<String>>> responseRoomBodyQueue =  new ConcurrentHashMap<Long,ConcurrentLinkedQueue<DeferredResult<String>>>();// key => roomId

	private ArrayList<Room> roomsArray; 

	private final Map<String,Queue<DeferredResult<String>>> responseBodyQueueForParticipents =  new ConcurrentHashMap<String,Queue<DeferredResult<String>>>();// key => roomId
	

	/**********************
	 * what doing with new auth user 
	 **********************/
	@SubscribeMapping("/chat.login/{username}")
	public Map<String, String> login(Principal principal)//Control user page after auth 
	{
		Map<String, String> result = new HashMap<>();
		ChatUser user = chatUserServise.getChatUser(Long.parseLong(principal.getName()));
		if(user.getIntitaUser() == null)
		{
			Room room;
			if(user.getRoomsFromUsers().iterator().hasNext())
				room = user.getRoomsFromUsers().iterator().next();
			else
			{
				ArrayList<ChatTenant> countTenant = chatTenantService.getTenants();
				if(countTenant.isEmpty())
				{
					result.put("nextWindow", "-1");
					return result;
				}
				int k = new Random().nextInt(countTenant.size());
				ChatTenant t_user = countTenant.get(k);//choose method

				room = roomService.register(t_user.getId() + "_" + principal.getName() + "_" + new Date().toString(), t_user.getChatUser());
				roomService.addUserToRoom(user, room);

				//subscribedtoRoomsUsersBuffer.add(user);//Is need?
				subscribedtoRoomsUsersBuffer.add(t_user.getChatUser());
			}

			simpMessagingTemplate.convertAndSend("/topic/chat/rooms/user." + user.getId(), roomService.getRoomsByChatUser(user));
			result.put("nextWindow", room.getId().toString());
		}
		else
		{
			//subscribedtoRoomsUsersBuffer.add(user);
			result.put("nextWindow", "0");
		}
		
		result.put("chat_id", principal.getName());
		result.put("chat_user_nickname", user.getNickName());
		String rooms = "{}";
		try {
			rooms = mapper.writeValueAsString(roomService.getRoomsByChatUser(user));
		} catch (JsonProcessingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		result.put("chat_rooms", rooms);

		return result;
	}

	@RequestMapping(value = "/chat/login/{username}", method = RequestMethod.POST)
	@ResponseBody
	public String retrieveParticipantsLP(Principal principal) throws JsonProcessingException {
		return mapper.writeValueAsString(login(principal));
	}

	/***************************
	 * GET PARTICIPANTS AND LOAD MESSAGE
	 ***************************/

	
	private Set<LoginEvent> GetParticipants(Room room_o)
	{
		Set<LoginEvent> userList = new HashSet<>();
		//Set<Long> chatUsersIds = new HashSet<>();
		LoginEvent currentChatUserLoginEvent = new LoginEvent(room_o.getAuthor().getId(),
				room_o.getAuthor().getNickName(),  participantRepository.isOnline(room_o.getAuthor().getId().toString()));
		userList.add(currentChatUserLoginEvent);
		for(ChatUser user : room_o.getUsers())
		{
			userList.add(new LoginEvent(user.getId(),user.getNickName(), participantRepository.isOnline(user.getId().toString())));
		}
		return  userList;
	}
	
	public Map<String, Object> retrieveParticipantsSubscribeAndMessagesObj(Room room_o) {

		Queue<UserMessage> buff = ChatController.messagesBuffer.get(room_o.getId());
		ArrayList<UserMessage> userMessages = userMessageService.getUserMessagesByRoom(room_o);
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
	public Map<String, Object> retrieveParticipantsSubscribeAndMessages(@DestinationVariable("room") String room) {//ONLY FOR TEST NEED FIX

		Room room_o = roomService.getRoom(Long.parseLong(room));
		return retrieveParticipantsSubscribeAndMessagesObj(room_o);
	}

	@MessageMapping("/{room}/chat.participants")
	public Map<String, Object> retrieveParticipantsMessage(@DestinationVariable String room) {
		Room room_o = roomService.getRoom(Long.parseLong(room));
		HashMap<String, Object> map = new HashMap();
		if(room_o != null)
			map.put("participants", GetParticipants(room_o));
		return map;
	}

	@RequestMapping(value = "/{room}/chat/participants_and_messages", method = RequestMethod.POST)
	@ResponseBody
	public String retrieveParticipantsAndMessagesLP(@PathVariable("room") String room, Principal principal) throws JsonProcessingException {
		CurrentStatusUserRoomStruct struct = ChatController.isMyRoom(room, principal, chatUserServise, roomService);//Control room from LP
		if( struct == null)
			return "{}";
		return mapper.writeValueAsString(retrieveParticipantsSubscribeAndMessagesObj(struct.getRoom()));
	}

	@RequestMapping(value = "/{room}/chat/participants/update", method = RequestMethod.POST)
	@ResponseBody
	public DeferredResult<String> retrieveParticipantsUpdateLP(@PathVariable("room") String room) throws JsonProcessingException {

		Long timeOut = 100000L;
		DeferredResult<String> result = new DeferredResult<String>(timeOut, "{}");
		Queue<DeferredResult<String>> queue = responseBodyQueueForParticipents.get(room);
		if(queue == null)
		{
			queue = new ConcurrentLinkedQueue<DeferredResult<String>>();
		}
		responseBodyQueueForParticipents.put(room, queue);		
		queue.add(result);
		return result;
	}

	/*
	 * call only if is need
	 */
	@Scheduled(fixedDelay=3000L)
	public void updateParticipants() {
		for(String key : responseBodyQueueForParticipents.keySet())
		{
			Room room_o = roomService.getRoom(Long.parseLong(key));
			HashMap<String, Object> result = new HashMap();
			if(room_o != null)
				result.put("participants", GetParticipants(room_o));
			
			for(DeferredResult<String> response : responseBodyQueueForParticipents.get(key))
			{
				try {
					if(!response.isSetOrExpired())
						response.setResult(mapper.writeValueAsString(result));
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

	@RequestMapping(value="/chat/rooms/private/{userID}", method=RequestMethod.POST)
	@ResponseBody
	public String getPrivateRoom( @PathVariable("userID") String userId, Principal principal) throws JsonProcessingException {

		ChatUser privateCharUser = chatUserServise.getChatUserFromIntitaId(Long.parseLong(userId), false);
		if(privateCharUser == null)
			return "-1";
		ChatUser chatUser = chatUserServise.getChatUser(principal);
		if(chatUser.getIntitaUser() == null)
			return "-1";
		
		Room room  = roomService.getPrivateRoom(chatUser, privateCharUser);
		if(room == null)
			room  = roomService.getPrivateRoom(privateCharUser, chatUser);
		
		if(room == null)
		{
			room = roomService.register(chatUser.getNickName() + "_" + privateCharUser.getNickName(), chatUser, (short) 1);// private room type => 1
			roomService.addUserToRoom(privateCharUser, room);
		}
		return mapper.writeValueAsString(room.getId());//@BAG@

	}

	@SubscribeMapping("/chat/rooms/user.{username}")
	public List<StringIntDate> getRoomsByAuthorSubscribe(Principal principal) { //000
		return roomService.getRoomsByChatUser(chatUserServise.getChatUser(Long.parseLong(principal.getName())));
	}

	@MessageMapping("/chat/rooms/add.{name}")
	public void addRoomByAuthor( @DestinationVariable("name") String name, Principal principal) {
		Long chatUserId = Long.parseLong(principal.getName());
		ChatUser user = chatUserServise.getChatUser(chatUserId);
		roomService.register(name, user);
		simpMessagingTemplate.convertAndSend("/topic/chat/rooms/user." + chatUserId, getRoomsByAuthorSubscribe(principal));

		OperationStatus operationStatus = new OperationStatus(OperationType.ADD_ROOM,true,"ADD ROOM");
		String subscriptionStr = "/topic/users/" + chatUserId + "/status";
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
		for(ChatUser chatUser : subscribedtoRoomsUsersBuffer)
		{
			if (chatUser==null){
				System.out.println("WARNING: NULL USER");
				continue;
			}
			Queue<DeferredResult<String>> responseList = responseRoomBodyQueue.get(chatUser.getId());
			if (responseList==null){
				//System.out.println("WARNING: RESPONSE LIST IS CLEAR");
				continue;
			}
			for(DeferredResult<String> response : responseList)
			{
					String str = mapper.writeValueAsString(roomService.getRoomsByChatUser(chatUser));
					if(!response.isSetOrExpired())
						response.setResult(str);
			}
			responseRoomBodyQueue.remove(chatUser.getId());
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
		roomService.register(roomName, author);
		boolean s = subscribedtoRoomsUsersBuffer.add(author);
	}
	/***************************
	 * REMOVE/ADD USERS FROM ROOMS
	 ***************************/

	boolean addUserToRoomFn( String nickName, String room, Principal principal,boolean ws)
	{
		Room room_o = roomService.getRoom(Long.parseLong(room));
		ChatUser user_o = chatUserServise.getChatUserFromIntitaEmail(nickName, false);//INTITA USER SEARCH
		Long chatUserAuthorId = Long.parseLong(principal.getName());
		ChatUser authorUser = chatUserServise.getChatUser(chatUserAuthorId);

		if(room_o == null || user_o == null || authorUser.getId().longValue() != room_o.getAuthor().getId().longValue())
		{
			return false;
		}
		roomService.addUserToRoom(user_o, room_o);
		
		updateParticipants();
		simpMessagingTemplate.convertAndSend("/topic/" + room + "/chat.participants", retrieveParticipantsMessage(room));
		simpMessagingTemplate.convertAndSend("/topic/chat/rooms/user." + user_o.getId(), roomService.getRoomsByChatUser(user_o));
		return true;
	}


	@MessageMapping("/chat/rooms.{room}/user.add.{nickName}")
	public void addUserToRoom( @DestinationVariable("nickName") String nickName, @DestinationVariable("room") String room, Principal principal) {
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
	public 	@ResponseBody String addUserToRoomLP(@PathVariable("room") String roomStr, @PathVariable("nickName") String nickName, Principal principal, HttpRequest req) throws InterruptedException, JsonProcessingException {
		ChatUser user_o = chatUserServise.getChatUserFromIntitaEmail(nickName, false);
		subscribedtoRoomsUsersBuffer.add(user_o);
		return mapper.writeValueAsString(addUserToRoomFn(nickName, roomStr, principal,false));
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
}