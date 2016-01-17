package com.wschat.web;

import java.security.Principal;
import java.util.ArrayList;
import java.util.Collection;
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

import com.wschat.domain.ChatMessage;
import com.wschat.domain.SessionProfanity;
import com.wschat.event.LoginEvent;
import com.wschat.event.ParticipantRepository;
import com.wschat.exception.TooMuchProfanityException;
import com.wschat.models.Room;
import com.wschat.models.User;
import com.wschat.services.RoomsService;
import com.wschat.services.UserMessageService;
import com.wschat.services.UsersService;
import com.wschat.util.ProfanityChecker;


@Controller
public class RoomController {

	@Autowired private ProfanityChecker profanityFilter;

	@Autowired private SessionProfanity profanity;

	@Autowired private ParticipantRepository participantRepository;

	@Autowired private SimpMessagingTemplate simpMessagingTemplate;

	@Autowired private RoomsService roomService;
	@Autowired private UsersService userService;
	@Autowired private UserMessageService userMessageService;

	private ArrayList<Room> roomsArray; 

	@SubscribeMapping("/{room}/chat.participants")
	public Map<String, Object> retrieveParticipantsSubscribe(@DestinationVariable String room) {//ONLY FOR TEST NEED FIX
		
		//	return participantRepository.getActiveSessions().values();
			
			System.out.println("@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@");//@LOG@
			System.out.println(Long.parseLong(room));//@LOG@
			Room room_o = roomService.getRoom(Long.parseLong(room));
			Set<LoginEvent> userList = new HashSet<>();
			userList.add(new LoginEvent(room_o.getAuthor().getUsername()));
			for(User user : room_o.getUsers())
			{
				userList.add(new LoginEvent(user.getUsername()));
			}
			ArrayList<ChatMessage> messagesHistory = ChatMessage.getAllfromUserMessages(userMessageService.getUserMessagesByRoom(room_o));
			HashMap<String, Object> map = new HashMap();
			map.put("participants", userList);
			map.put("messages", messagesHistory);
			/*if(userList != null)
			{
				System.out.println(userList.size());//@LOG@
				if(room_o != null)
				{
					userList.add(room_o.getAuthor());
				}
				System.out.println(userList.size());
			}*/
			
			return map;

		}
	@MessageMapping("/{room}/chat.participants")
	public Map<String, Object> retrieveParticipantsMessage(@DestinationVariable String room) {//ONLY FOR TEST NEED FIX
		return retrieveParticipantsSubscribe(room);
	}
	
	@SubscribeMapping("/chat/rooms/user.{username}")
	//@SendToUser(value = "/exchange/amq.direct/errors", broadcast = false)
	public Map<Long, String> getRoomsByAuthorSubscribe(Principal principal) {
		System.out.println("Okkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkk");//@LOG@
		System.out.println(principal.getName());//@LOG@
		return getRoomsByAuthor(principal.getName());
	}
	private Map<Long, String> getRoomsByAuthor(String name) {
		User currentUser = userService.getUser(name);
		ArrayList<Room> list = new ArrayList<>(currentUser.getRootRooms());
		list.addAll(currentUser.getRoomsFromUsers());
		return convertToNameList(list);
	}
	@MessageMapping("/chat/rooms/user.{username}")
	public Map<Long, String> getRoomsByAuthorMessage(Principal principal) {
		return getRoomsByAuthorSubscribe(principal);
	}
	
	@MessageMapping("/chat/rooms/add.{name}")
	//@SendToUser(value = "/exchange/amq.direct/errors", broadcast = false)
	public boolean addRoomByAuthor( @DestinationVariable("name") String name, Principal principal) {
		System.out.println("OkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkAdd");//@LOG@

		System.out.println(principal.getName());//@LOG@
		User user = userService.getUser(principal.getName());
		roomService.register(name, user);
		simpMessagingTemplate.convertAndSend("/topic/chat/rooms/user." + principal.getName(), getRoomsByAuthorMessage(principal));
		return true;
	}
	
	@MessageMapping("/chat/rooms.{room}/user.add.{login}")
	//@SendToUser(value = "/exchange/amq.direct/errors", broadcast = false)
	public boolean addUserToRoom( @DestinationVariable("login") String login, @DestinationVariable("room") String room, Principal principal) {
		System.out.println("OkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkAddUser");//@LOG@

		System.out.println(login);//@LOG@
		System.out.println(room);//@LOG@
		
		Room room_o = roomService.getRoom(Long.parseLong(room));
		if(room_o == null)
			return false;
		
		User user_o = userService.getUser(login);
		if(user_o == null)
			return false;
		
		User user = userService.getUser(principal.getName());
		
		if(user.getId() != room_o.getAuthor().getId())
			return false;
		
		System.out.println(getRoomsByAuthor(user_o.getLogin()).size() + "  " + Boolean.toString(roomService.addUserToRoom(user_o, room_o)));
		simpMessagingTemplate.convertAndSend("/topic/" + room + "/chat.participants", retrieveParticipantsMessage(room));
		String test = "/topic/chat/rooms/user." + user_o.getLogin();
		System.out.println("update " + test + " new size " + getRoomsByAuthor(user_o.getLogin()).size());
		simpMessagingTemplate.convertAndSend(test, getRoomsByAuthor(user_o.getLogin()));
		
		return true;
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