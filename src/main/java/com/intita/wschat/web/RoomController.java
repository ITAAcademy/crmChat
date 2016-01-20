package com.intita.wschat.web;

import java.security.Principal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageExceptionHandler;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.annotation.SendToUser;
import org.springframework.messaging.simp.annotation.SubscribeMapping;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;

import com.intita.wschat.domain.ChatMessage;
import com.intita.wschat.domain.SessionProfanity;
import com.intita.wschat.event.LoginEvent;
import com.intita.wschat.event.ParticipantRepository;
import com.intita.wschat.exception.TooMuchProfanityException;
import com.intita.wschat.models.ChatTenant;
import com.intita.wschat.models.ChatUser;
import com.intita.wschat.models.Room;
import com.intita.wschat.models.User;
import com.intita.wschat.models.OperationStatus.OperationType;
import com.intita.wschat.models.ChatUserLastRoomDate;
import com.intita.wschat.services.ChatTenantService;
import com.intita.wschat.models.OperationStatus;
import com.intita.wschat.services.ChatUserLastRoomDateService;
import com.intita.wschat.services.ChatUsersService;
import com.intita.wschat.services.RoomsService;
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
	@Autowired private ChatTenantService ChatTenantService;
	@Autowired private ChatUserLastRoomDateService chatUserLastRoomDateService;
	
	public class StringInt {
		public String string;
		public Integer nums;
		
		public StringInt() {
			string = "";
			nums = 0;
		}
		
		public StringInt(String string, Integer nums) {
			this.string = string;
			this.nums = nums;
		}
	}

	private ArrayList<Room> roomsArray; 

	@SubscribeMapping("/chat.login/{username}")
	public Map<String, Long> login(Principal principal)//Control user page after auth 
	{
		Map<String, Long> result = new HashMap<>();
		ChatUser user = chatUserServise.getChatUser(Long.parseLong(principal.getName()));
		if(user.getIntitaUser() == null)
		{
			ChatTenant t_user = ChatTenantService.getChatTenant((long) 1);//choose method
			Room room;
			
			if(user.getRoomsFromUsers().iterator().hasNext())
				room = user.getRoomsFromUsers().iterator().next();
			else
				room = roomService.register(t_user.getId() + "_" + principal.getName() + "_" + new Date().toString(), t_user.getChatUser());
			roomService.addUserToRoom(user, room);
			simpMessagingTemplate.convertAndSend("/topic/chat/rooms/user." + user.getId(), getRoomsByChatUserId(user.getId()));
			result.put("nextWindow", room.getId());
		}
		else
		{
			result.put("nextWindow", (long) 0);
		}
		return result;
	}
	
	@SubscribeMapping("/{room}/chat.participants")
	public Map<String, Object> retrieveParticipantsSubscribe(@DestinationVariable String room) {//ONLY FOR TEST NEED FIX
		
		//	return participantRepository.getActiveSessions().values();
			
			System.out.println("@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@");//@LOG@
			System.out.println(Long.parseLong(room));//@LOG@
			Room room_o = roomService.getRoom(Long.parseLong(room));
			Set<LoginEvent> userList = new HashSet<>();
			//Set<Long> chatUsersIds = new HashSet<>();
			LoginEvent currentChatUserLoginEvent = new LoginEvent(room_o.getAuthor().getId(),
					room_o.getAuthor().getNickName());
			userList.add(currentChatUserLoginEvent);
			for(ChatUser user : room_o.getUsers())
			{
				userList.add(new LoginEvent(user.getId(),user.getNickName()));
			}
			ArrayList<ChatMessage> messagesHistory = ChatMessage.getAllfromUserMessages(userMessageService.getUserMessagesByRoom(room_o));
			HashMap<String, Object> map = new HashMap();
			map.put("participants", userList);
			map.put("messages", messagesHistory);
			return map;

		}
	@MessageMapping("/{room}/chat.participants")
	public Map<String, Object> retrieveParticipantsMessage(@DestinationVariable String room) {//ONLY FOR TEST NEED FIX
		return retrieveParticipantsSubscribe(room);
	}
	
	@SubscribeMapping("/chat/rooms/user.{username}")
	//@SendToUser(value = "/exchange/amq.direct/errors", broadcast = false)
	public Map<Long, StringInt> getRoomsByAuthorSubscribe(Principal principal) { //000
		System.out.println("Okkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkk");//@LOG@
		System.out.println(principal.getName());//@LOG@

		return getRoomsByChatUserId(Long.parseLong(principal.getName()));
	}
	private Map<Long, StringInt> getRoomsByChatUserId(Long chatUserId) {
		
		ChatUser currentUser = chatUserServise.getChatUser(chatUserId);
		ArrayList<Room> list = new ArrayList<>();
		if(currentUser != null)
		{
			list.addAll(currentUser.getRootRooms());
			list.addAll(currentUser.getRoomsFromUsers());
		}
		Map<Long, String>  lista = convertToNameList(list);
		Map<Long, StringInt> result = new HashMap <Long, StringInt> ();
		int i = 0;
		for (Map.Entry<Long, String> entry : lista.entrySet())
		{
			Long key = entry.getKey();
			
			
			//roo
			ChatUser chatUser = chatUserServise.getChatUser(chatUserId);
			ChatUserLastRoomDate userLastRoomDate = chatUserLastRoomDateService.getUserLastRoomDate(list.get(i++), chatUser);
			Room room = userLastRoomDate.getLastRoom();
			int messages_cnt = 0;
			
			/*if (userLastRoomDate == null)
			{
				userLastRoomDate = new ChatUserLastRoomDate(chatUser.getId(),new Date(), room );
				messages_cnt =  userMessageService.getUserMesagges().size();
			}
			else
			{*/
				Date date = userLastRoomDate.getLastLogout();
				messages_cnt =  userMessageService.getMessagesByDate(date).size();
		//	}				
			
			chatUserLastRoomDateService.updateUserLastRoomDateInfo(userLastRoomDate);						
			
			StringInt sb = new StringInt(entry.getValue(), messages_cnt );
			result.put(key,sb);
		}
		return result;				
	}
	@MessageMapping("/chat/rooms/user.{username}")
	public Map<Long, StringInt> getRoomsByAuthorMessage(Principal principal) {
		return getRoomsByAuthorSubscribe(principal);
	}
	
	@MessageMapping("/chat/rooms/add.{name}")
	//@SendToUser(value = "/exchange/amq.direct/errors", broadcast = false)
	public boolean addRoomByAuthor( @DestinationVariable("name") String name, Principal principal) {
		System.out.println("OkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkAdd");//@LOG@

		System.out.println(principal.getName());//@LOG@
		Long chatUserId = Long.parseLong(principal.getName());
		ChatUser user = chatUserServise.getChatUser(chatUserId);
		roomService.register(name, user);
		simpMessagingTemplate.convertAndSend("/topic/chat/rooms/user." + chatUserId, getRoomsByAuthorMessage(principal));
		return true;
	}
	
	@MessageMapping("/chat/rooms.{room}/user.add.{nickName}")
	//@SendToUser(value = "/exchange/amq.direct/errors", broadcast = false)
	public void addUserToRoom( @DestinationVariable("nickName") String nickName, @DestinationVariable("room") String room, Principal principal) {
		System.out.println("OkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkAddUser");//@LOG@

		//System.out.println(login);//@LOG@
		System.out.println(room);//@LOG@
		Long chatUserId = 0L;
		chatUserId = Long.parseLong(principal.getName());
		Room room_o = roomService.getRoom(Long.parseLong(room));
		if(room_o == null)
		{
			OperationStatus operationStatus = new OperationStatus(OperationType.ADD_USER_TO_ROOM,false,"ADD USER TO ROOM");
			String subscriptionStr = "/topic/users/"+chatUserId+"/status";
			simpMessagingTemplate.convertAndSend(subscriptionStr, operationStatus);
			return;
		}
	
		ChatUser user_o = chatUserServise.getChatUserFromIntitaEmail(nickName, false);//INTITA USER SEARCH
		if(user_o == null)
		{
			OperationStatus operationStatus = new OperationStatus(OperationType.ADD_USER_TO_ROOM,false,"ADD USER TO ROOM");
			String subscriptionStr = "/topic/users/"+chatUserId+"/status";
			simpMessagingTemplate.convertAndSend(subscriptionStr, operationStatus);
			return;
		}
		
		Long chatUserAuthorId = Long.parseLong(principal.getName());
		ChatUser authorUser = chatUserServise.getChatUser(chatUserAuthorId);
		
		if(authorUser.getId() != room_o.getAuthor().getId())
		{
			OperationStatus operationStatus = new OperationStatus(OperationType.ADD_USER_TO_ROOM,false,"ADD USER TO ROOM");
			String subscriptionStr = "/topic/users/"+chatUserId+"/status";
			simpMessagingTemplate.convertAndSend(subscriptionStr, operationStatus);
			return;
		}
		roomService.addUserToRoom(user_o, room_o);
		//System.out.println(getRoomsByAuthor(user_o.getLogin()).size() + "  " + Boolean.toString(roomService.addUserToRoom(user_o, room_o)));
		simpMessagingTemplate.convertAndSend("/topic/" + room + "/chat.participants", retrieveParticipantsMessage(room));
		simpMessagingTemplate.convertAndSend("/topic/chat/rooms/user." + user_o.getId(), getRoomsByChatUserId(user_o.getId()));
		
		OperationStatus operationStatus = new OperationStatus(OperationType.ADD_USER_TO_ROOM,true,"ADD USER TO ROOM");
		String subscriptionStr = "/topic/users/"+chatUserId+"/status";
		simpMessagingTemplate.convertAndSend(subscriptionStr, operationStatus);
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
	@MessageExceptionHandler
	@SendToUser(value = "/exchange/amq.direct/errors", broadcast = false)
	public String handleProfanity(TooMuchProfanityException e) {
		return e.getMessage();
	}
}