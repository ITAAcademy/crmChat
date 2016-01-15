package com.sergialmar.wschat.web;

import java.security.Principal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageExceptionHandler;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.annotation.SendToUser;
import org.springframework.messaging.simp.annotation.SubscribeMapping;
import org.springframework.stereotype.Controller;

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

/**
 * Controller that handles WebSocket chat messages
 * 
 * @author Sergi Almar
 */
@Controller
public class ChatController {

	@Autowired private ProfanityChecker profanityFilter;
	
	@Autowired private SessionProfanity profanity;
	
	@Autowired private ParticipantRepository participantRepository;
	
	@Autowired private SimpMessagingTemplate simpMessagingTemplate;
	@Autowired private RoomsService roomService;
	@Autowired private UsersService userService;
	
	@SubscribeMapping("/{room}/chat.participants")
	public Collection<LoginEvent> retrieveParticipantsSubscribe(@DestinationVariable String room) {//ONLY FOR TEST NEED FIX
		
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
		/*if(userList != null)
		{
			System.out.println(userList.size());//@LOG@
			if(room_o != null)
			{
				userList.add(room_o.getAuthor());
			}
			System.out.println(userList.size());
		}*/
		
		return userList;
	}
	@MessageMapping("/{room}/chat.participants")
	public Collection<LoginEvent> retrieveParticipantsMessage(@DestinationVariable String room) {//ONLY FOR TEST NEED FIX
		return retrieveParticipantsSubscribe(room);
	}

	
	@MessageMapping("/{room}/chat.message")
	public ChatMessage filterMessage(@DestinationVariable String room,@Payload ChatMessage message, Principal principal) {
		checkProfanityAndSanitize(message);
		
		message.setUsername(principal.getName());
		
		return message;
	}
	
	@MessageMapping("/{room}/chat.private.{username}")
	public void filterPrivateMessage(@DestinationVariable String room,@Payload ChatMessage message, @DestinationVariable("username") String username, Principal principal) {
		checkProfanityAndSanitize(message);
		
		message.setUsername(principal.getName());

		simpMessagingTemplate.convertAndSend("/user/" + username + "/exchange/amq.direct/"+room+"/chat.message", message);
	}
	
	private void checkProfanityAndSanitize(ChatMessage message) {
		long profanityLevel = profanityFilter.getMessageProfanity(message.getMessage());
		profanity.increment(profanityLevel);
		message.setMessage(profanityFilter.filter(message.getMessage()));
	}
	
	@MessageExceptionHandler
	@SendToUser(value = "/exchange/amq.direct/errors", broadcast = false)
	public String handleProfanity(TooMuchProfanityException e) {
		return e.getMessage();
	}
}