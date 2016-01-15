package com.sergialmar.wschat.web;

import java.security.Principal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

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

import com.sergialmar.wschat.domain.ChatMessage;
import com.sergialmar.wschat.domain.SessionProfanity;
import com.sergialmar.wschat.event.LoginEvent;
import com.sergialmar.wschat.event.ParticipantRepository;
import com.sergialmar.wschat.exception.TooMuchProfanityException;
import com.sergialmar.wschat.models.Room;
import com.sergialmar.wschat.models.User;
import com.sergialmar.wschat.services.RoomsService;
import com.sergialmar.wschat.services.UsersService;
import com.sergialmar.wschat.util.ProfanityChecker;


@Controller
public class RoomController {

	@Autowired private ProfanityChecker profanityFilter;

	@Autowired private SessionProfanity profanity;

	@Autowired private ParticipantRepository participantRepository;

	@Autowired private SimpMessagingTemplate simpMessagingTemplate;

	@Autowired private RoomsService roomService;
	@Autowired private UsersService userService;

	private ArrayList<Room> roomsArray; 

	
	@SubscribeMapping("/chat/rooms")
	//@SendToUser(value = "/exchange/amq.direct/errors", broadcast = false)
	public Map<Long, String> getRoomsByAuthorSubscribe(Principal principal) {
		System.out.println("Okkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkk");//@LOG@
		System.out.println(principal.getName());//@LOG@
		User currentUser = userService.getUser(principal.getName());
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
	//	simpMessagingTemplate.convertAndSend("/chat/{username}/rooms", getRoomsByAuthorMessage(principal));
		return true;
	}
	
	@MessageMapping("/chat/rooms.{room}/user.add.{login}")
	//@SendToUser(value = "/exchange/amq.direct/errors", broadcast = false)
	public boolean addUserToRoom( @DestinationVariable("login") String login, @DestinationVariable("room") String room, Principal principal) {
		System.out.println("OkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkAddUser");//@LOG@

		System.out.println(login);//@LOG@
		System.out.println(room);//@LOG@
		
		Room room_o = roomService.getRoom(Long.parseLong(room));
		User user_o = userService.getUser(login);
		
		User user = userService.getUser(principal.getName());
		
		if(user.getId() != room_o.getAuthor().getId())
			return false;
		
		System.out.println(Boolean.toString(roomService.addUserToRoom(user_o, room_o)));
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