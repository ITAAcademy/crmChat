package com.sergialmar.wschat.web;

import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

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

import com.fasterxml.jackson.annotation.JsonView;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
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
import java.util.List;

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

	/*@RequestMapping(value="/getusersemails", method = RequestMethod.POST)
	@ResponseBody
	public String getEmails(@RequestParam String login) throws JsonProcessingException {
		List<String> emails = userService.getUsersEmailsFist5(login);
		ObjectMapper mapper = new ObjectMapper();
		String jsonInString = mapper.writeValueAsString(emails);
		return jsonInString;
	}*/

	@RequestMapping(value="/get_users_emails_like", method = RequestMethod.GET)
	@ResponseBody
	public String getEmailsLike(@RequestParam String login) throws JsonProcessingException {
		List emails = userService.getUsersEmailsFist5(login);
		ObjectMapper mapper = new ObjectMapper();
		String jsonInString = mapper.writeValueAsString(emails);
		return jsonInString;
	}



	/*@RequestMapping(value = "/getusersemails", method = RequestMethod.POST)
	 //@ResponseStatus("200")
	@ResponseBody
	@JsonView(View.Summary.class)
	public  String getUserEmailsFromDB(@RequestBody String emailsa){
		ArrayList<User> users = userService.getUsers();
		ArrayList<String> emails = new ArrayList<String>();
		for(int i = 0; i < users.size(); i++)
			emails.add(users.get(i).getEmail());
		emailsa =  emails.toString();
		return emailsa;
	}*/

	@MessageExceptionHandler
	@SendToUser(value = "/exchange/amq.direct/errors", broadcast = false)
	public String handleProfanity(TooMuchProfanityException e) {
		return e.getMessage();
	}
}