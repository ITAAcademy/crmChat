package com.sergialmar.wschat.web;

import java.security.Principal;
import java.util.Collection;

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
import com.sergialmar.wschat.models.UserMessage;
import com.sergialmar.wschat.services.RoomsService;
import com.sergialmar.wschat.services.UserMessageService;
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
	
	@Autowired private UserMessageService userMessageService;
	
	@Autowired private UsersService usersService;
	
	@Autowired private RoomsService roomsService;
	
	@SubscribeMapping("/{room}/chat.participants")
	public Collection<LoginEvent> retrieveParticipants(@DestinationVariable String room) {	
		return participantRepository.getActiveSessions().values();
	}
	

	
	@MessageMapping("/{room}/chat.message")
	public ChatMessage filterMessage(@DestinationVariable("room") String roomStr,@Payload ChatMessage message, Principal principal) {
		System.out.println("ZIGZAG ZIGZAG ZIGZAG ZIGZAG ZIGZAG ZIGZAG ZIGZAG ZIGZAG ZIGZAG");
		checkProfanityAndSanitize(message);
		message.setUsername(principal.getName());
		User author = usersService.getUser(principal.getName());
		Room room = roomsService.getRoom(Long.parseLong(roomStr));
		UserMessage messageToSave = new UserMessage(author,room,message.getMessage());
		userMessageService.addMessage(messageToSave);
		System.out.println("/////////////////ZIGZAG ZIGZAG ZIGZAG ZIGZAG ZIGZAG ZIGZAG ZIGZAG ZIGZAG ZIGZAG");
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