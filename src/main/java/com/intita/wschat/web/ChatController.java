package com.intita.wschat.web;

import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.security.Principal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
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
import com.intita.wschat.domain.ChatMessage;
import com.intita.wschat.domain.SessionProfanity;
import com.intita.wschat.event.LoginEvent;
import com.intita.wschat.event.ParticipantRepository;
import com.intita.wschat.exception.TooMuchProfanityException;
import com.intita.wschat.models.ChatUser;
import com.intita.wschat.models.Room;
import com.intita.wschat.models.User;
import com.intita.wschat.models.UserMessage;
import com.intita.wschat.services.ChatUsersService;
import com.intita.wschat.services.RoomsService;
import com.intita.wschat.services.UserMessageService;
import com.intita.wschat.services.UsersService;
import com.intita.wschat.util.ProfanityChecker;

import java.util.List;
import java.util.Map;

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
	@Autowired private UserMessageService userMessageService;
	@Autowired private ChatUsersService chatUsersService;


	@MessageMapping("/{room}/chat.message")
	public ChatMessage filterMessage(@DestinationVariable("room") String roomStr,@Payload ChatMessage message, Principal principal) {
		System.out.println("ZIGZAG ZIGZAG ZIGZAG ZIGZAG ZIGZAG ZIGZAG ZIGZAG ZIGZAG ZIGZAG");
		checkProfanityAndSanitize(message);
	
		
		Long chatUserId = 0L;
		chatUserId = Long.parseLong(principal.getName());
		ChatUser chatUser = chatUsersService.getChatUser(chatUserId);
		Room room = roomService.getRoom(Long.parseLong(roomStr));
		UserMessage messageToSave = new UserMessage(chatUser,room,message.getMessage());
		message.setUsername(chatUser.getNickName());
		userMessageService.addMessage(messageToSave);
		System.out.println("/////////////////ZIGZAG ZIGZAG ZIGZAG ZIGZAG ZIGZAG ZIGZAG ZIGZAG ZIGZAG ZIGZAG " + roomStr);		
		
		simpMessagingTemplate.convertAndSend("/topic/users/must/get.room.num/chat.message", roomStr);
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
	public String getEmailsLike(@RequestParam String login, @RequestParam Long room) throws JsonProcessingException {

		Set<User>  users_set = roomService.getRoom(room).getUsers();
		List<User> users = new  ArrayList<User>();
		users.addAll(users_set);

		users.add(roomService.getRoom(room).getAuthor());
		List<String> room_emails = new  ArrayList<String>();
		for(int i = 0; i <  users.size(); i++)
		{
			room_emails.add(users.get(i).getEmail());
		}

		List<String> emails = userService.getUsersEmailsFist5(login, room_emails);

		ObjectMapper mapper = new ObjectMapper();

					String jsonInString = mapper.writeValueAsString(emails);
					return jsonInString;
	}
	
	@RequestMapping(value="/get_users_nicknames_like", method = RequestMethod.GET)
	@ResponseBody
	public Set<LoginEvent>  getNickNamesLike(@RequestParam String nickName, @RequestParam Long room) throws JsonProcessingException {

		Set<ChatUser>  users_set = roomService.getRoom(room).getChatUsers();
		List<ChatUser> users = new  ArrayList<ChatUser>();
		users.addAll(users_set);

		users.add(roomService.getRoom(room).getAuthor().getChatUser());
		Set<LoginEvent> usersData = new HashSet<LoginEvent>();
		
		List<String> room_nicks = new  ArrayList<String>();
		for(int i = 0; i <  users.size(); i++)
		{
			room_nicks.add(users.get(i).getNickName());
		}

		List<String> nicks = chatUsersService.getUsersNickNameFist5(nickName, room_nicks);
		
		for (ChatUser singleChatUser: users){
			String nn = singleChatUser.getNickName();
			if (!nicks.contains(nn))continue;
			LoginEvent userData = new LoginEvent(singleChatUser.getId(),nn);
			usersData.add(userData);	
		}
return usersData;
		/*ObjectMapper mapper = new ObjectMapper();

					String jsonInString = mapper.writeValueAsString(nicks);
					return jsonInString;*/
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