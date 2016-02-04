package com.intita.wschat.web;

import java.security.Principal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpRequest;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageExceptionHandler;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.annotation.SendToUser;
import org.springframework.messaging.simp.annotation.SubscribeMapping;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
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
import com.intita.wschat.models.Room;
import com.intita.wschat.models.User;
import com.intita.wschat.models.UserMessage;
import com.intita.wschat.models.OperationStatus.OperationType;
import com.intita.wschat.models.ChatUserLastRoomDate;
import com.intita.wschat.services.ChatTenantService;
import com.intita.wschat.models.OperationStatus;
import com.intita.wschat.services.ChatUserLastRoomDateService;
import com.intita.wschat.services.ChatUsersService;
import com.intita.wschat.services.RoomsService;
import com.intita.wschat.services.RoomsService.StringIntDate;
import com.intita.wschat.services.UserMessageService;
import com.intita.wschat.services.UsersService;
import com.intita.wschat.util.ProfanityChecker;


@Controller
public class RoomController {

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

	private ArrayList<Room> roomsArray; 

	@SubscribeMapping("/chat.login/{username}")
	public Map<String, Long> login(Principal principal)//Control user page after auth 
	{
		Map<String, Long> result = new HashMap<>();
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
					result.put("nextWindow", (long) -1);
					return result;
				}
				int k = new Random().nextInt(countTenant.size());
				ChatTenant t_user = countTenant.get(k);//choose method
				
				room = roomService.register(t_user.getId() + "_" + principal.getName() + "_" + new Date().toString(), t_user.getChatUser());
				roomService.addUserToRoom(user, room);
			}
			
			simpMessagingTemplate.convertAndSend("/topic/chat/rooms/user." + user.getId(), roomService.getRoomsByChatUser(user));
			result.put("nextWindow", room.getId());
		}
		else
		{
			result.put("nextWindow", (long) 0);
		}
		return result;
	}
	
	/***************************
	 * GET PARTICIPANTS AND LOAD MESSAGE
	 ***************************/

	private Set<LoginEvent> GetParticipants(Room room_o)
	{
		Set<LoginEvent> userList = new HashSet<>();
		//Set<Long> chatUsersIds = new HashSet<>();
		LoginEvent currentChatUserLoginEvent = new LoginEvent(room_o.getAuthor().getId(),
				room_o.getAuthor().getNickName());
		userList.add(currentChatUserLoginEvent);
		for(ChatUser user : room_o.getUsers())
		{
			userList.add(new LoginEvent(user.getId(),user.getNickName()));
		}
		return  userList;
	}
	
	@SubscribeMapping("/{room}/chat.participants")
	public Map<String, Object> retrieveParticipantsSubscribe(@DestinationVariable("room") String room) {//ONLY FOR TEST NEED FIX

		System.out.println("@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@");//@LOG@
		System.out.println(Long.parseLong(room));//@LOG@
		Room room_o = roomService.getRoom(Long.parseLong(room));
		
		ArrayList<ChatMessage> messagesHistory = ChatMessage.getAllfromUserMessages(userMessageService.getUserMessagesByRoom(room_o));
		HashMap<String, Object> map = new HashMap();
		map.put("participants", GetParticipants(room_o));
		map.put("messages", messagesHistory);

		return map;

	}
	
	@MessageMapping("/{room}/chat.participants")
	public Map<String, Object> retrieveParticipantsMessage(@DestinationVariable String room) {
		Room room_o = roomService.getRoom(Long.parseLong(room));
		HashMap<String, Object> map = new HashMap();
		map.put("participants", GetParticipants(room_o));
		return map;
	}
	
	@RequestMapping(value = "/{room}/chat/participants", method = RequestMethod.POST)
	@ResponseBody
	public String retrieveParticipantsLP(@PathVariable("room") String room) throws JsonProcessingException {
		return new ObjectMapper().writeValueAsString(retrieveParticipantsSubscribe(room));
	}
	
	@RequestMapping(value = "/{room}/chat/participants/update", method = RequestMethod.POST)
	@ResponseBody
	public String retrieveParticipantsUpdateLP(@PathVariable("room") String room) throws JsonProcessingException {
		return new ObjectMapper().writeValueAsString(retrieveParticipantsMessage(room));
	}


	/***************************
	 * GET/ADD ROOMS
	 ***************************/
	
	@SubscribeMapping("/chat/rooms/user.{username}")
	//@SendToUser(value = "/exchange/amq.direct/errors", broadcast = false)
	public Map<Long, StringIntDate> getRoomsByAuthorSubscribe(Principal principal) { //000
		System.out.println("Okkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkk");//@LOG@
		System.out.println(principal.getName());//@LOG@

		return roomService.getRoomsByChatUser(chatUserServise.getChatUser(Long.parseLong(principal.getName())));
	}
	
	@RequestMapping(value="/chat/rooms/user.{username}", method = RequestMethod.GET)
	@ResponseBody
	@MessageMapping("/chat/rooms/user.{username}")
	public Map<Long, StringIntDate> getRoomsByAuthorMessage(Principal principal) {
		return getRoomsByAuthorSubscribe(principal);
	}

	@MessageMapping("/chat/rooms/add.{name}")
	//@SendToUser(value = "/exchange/amq.direct/errors", broadcast = false)
	public void addRoomByAuthor( @DestinationVariable("name") String name, Principal principal) {
		System.out.println("OkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkAdd");//@LOG@

		System.out.println(principal.getName());//@LOG@
		Long chatUserId = Long.parseLong(principal.getName());
		ChatUser user = chatUserServise.getChatUser(chatUserId);
		roomService.register(name, user);
		simpMessagingTemplate.convertAndSend("/topic/chat/rooms/user." + chatUserId, getRoomsByAuthorSubscribe(principal));

		OperationStatus operationStatus = new OperationStatus(OperationType.ADD_ROOM,true,"ADD ROOM");
		String subscriptionStr = "/topic/users/" + chatUserId + "/status";
		simpMessagingTemplate.convertAndSend(subscriptionStr, operationStatus);
	}

	
	/***************************
	 * REMOVE/ADD USERS FROM ROOMS
	 ***************************/
	@MessageMapping("/chat/rooms.{room}/user.add.{nickName}")
	//@SendToUser(value = "/exchange/amq.direct/errors", broadcast = false)
	public void addUserToRoom( @DestinationVariable("nickName") String nickName, @DestinationVariable("room") String room, Principal principal) {
		System.out.println("OkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkAddUser");//@LOG@

		//System.out.println(login);//@LOG@
		System.out.println(room);//@LOG@

		Long chatUserId = 0L;
		chatUserId = Long.parseLong(principal.getName());
		Room room_o = roomService.getRoom(Long.parseLong(room));
		ChatUser user_o = chatUserServise.getChatUserFromIntitaEmail(nickName, false);//INTITA USER SEARCH
		Long chatUserAuthorId = Long.parseLong(principal.getName());
		ChatUser authorUser = chatUserServise.getChatUser(chatUserAuthorId);
		
		if(room_o == null || user_o == null || authorUser.getId() != room_o.getAuthor().getId())
		{
			OperationStatus operationStatus = new OperationStatus(OperationType.ADD_USER_TO_ROOM,false,"ADD USER TO ROOM");
			String subscriptionStr = "/topic/users/"+chatUserId+"/status";
			simpMessagingTemplate.convertAndSend(subscriptionStr, operationStatus);
			return;
		}

		roomService.addUserToRoom(user_o, room_o);
		//System.out.println(getRoomsByAuthor(user_o.getLogin()).size() + "  " + Boolean.toString(roomService.addUserToRoom(user_o, room_o)));
		simpMessagingTemplate.convertAndSend("/topic/" + room + "/chat.participants", retrieveParticipantsMessage(room));
		simpMessagingTemplate.convertAndSend("/topic/chat/rooms/user." + user_o.getId(), roomService.getRoomsByChatUser(user_o));

		OperationStatus operationStatus = new OperationStatus(OperationType.ADD_USER_TO_ROOM,true,"ADD USER TO ROOM");
		String subscriptionStr = "/topic/users/"+chatUserId+"/status";
		simpMessagingTemplate.convertAndSend(subscriptionStr, operationStatus);

	}
	
	@RequestMapping(value = "/chat/rooms.{room}/user.add.{nickName}", method = RequestMethod.POST)
	public 	@ResponseBody DeferredResult<String> updateMessageLP(@PathVariable("room") String roomStr, @PathVariable("nickName") String nickName, Principal principal, HttpRequest req) throws InterruptedException {
		/*Long timeOut = 1000000L;
		Queue<UserMessage> list = messagesBuffer.get(roomStr);
		if(list == null)
		{
			list = new ConcurrentLinkedQueue<>();
			//	messagesBuffer.put(Long.parseLong(roomStr), list);
		}
		DeferredResult<String> result = new DeferredResult<String>(timeOut);
		Queue<DeferredResult<String>> queue = responseBodyQueue.get(roomStr);
		if(queue == null)
		{
			queue = new ConcurrentLinkedQueue<DeferredResult<String>>();
		}
		while(responseBodyQueue.putIfAbsent(roomStr, queue) == null);		
		queue.add(result);
		//Thread.sleep(l);
		return result;*/
		return null;
	}

	@MessageMapping("/chat/rooms.{room}/user.remove.{login}")
	public boolean removeUserFromRoom( @DestinationVariable("login") String login, @DestinationVariable("room") Long room, Principal principal) {
		System.out.println("OkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkDeleteUser");//@LOG@

		System.out.println(login);//@LOG@
		System.out.println(room);//@LOG@

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
	
	/*@Scheduled(fixedRate=600L)
	public void processQueues() throws JsonProcessingException {
		ObjectMapper mapper = new ObjectMapper();

		for(String roomId : messagesBuffer.keySet())
		{
			Queue<UserMessage> array = messagesBuffer.get(roomId);
			Queue<DeferredResult<String>> responseList = responseBodyQueue.get(roomId);
			for(DeferredResult<String> response : responseList)
			{
				if(responseList != null)
				{
					String str = mapper.writeValueAsString(ChatMessage.getAllfromUserMessages(array));
					response.setResult(str);
				}
			}
			responseList.clear();
			userMessageService.addMessages(array);
		}
		messagesBuffer.clear();;
		//this.responseBodyQueue.clear();
	}*/
	
	@MessageExceptionHandler
	@SendToUser(value = "/exchange/amq.direct/errors", broadcast = false)
	public String handleProfanity(TooMuchProfanityException e) {
		return e.getMessage();
	}

}