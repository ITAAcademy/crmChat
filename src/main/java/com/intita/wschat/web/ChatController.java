package com.intita.wschat.web;

import java.security.Principal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpRequest;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageExceptionHandler;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.annotation.SendToUser;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.context.request.async.DeferredResult;
import org.springframework.web.context.request.async.WebAsyncManager;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.intita.wschat.config.CustomAuthenticationProvider;
import com.intita.wschat.domain.ChatMessage;
import com.intita.wschat.domain.SessionProfanity;
import com.intita.wschat.event.LoginEvent;
import com.intita.wschat.event.ParticipantRepository;
import com.intita.wschat.exception.TooMuchProfanityException;
import com.intita.wschat.models.ChatUser;
import com.intita.wschat.models.ChatUserLastRoomDate;
import com.intita.wschat.models.OperationStatus;
import com.intita.wschat.models.OperationStatus.OperationType;
import com.intita.wschat.models.Room;
import com.intita.wschat.models.User;
import com.intita.wschat.models.UserMessage;
import com.intita.wschat.services.ChatTenantService;
import com.intita.wschat.services.ChatUserLastRoomDateService;
import com.intita.wschat.services.ChatUsersService;
import com.intita.wschat.services.RoomsService;
import com.intita.wschat.services.UserMessageService;
import com.intita.wschat.services.UsersService;
import com.intita.wschat.util.ProfanityChecker;

/**
 * Controller that handles WebSocket chat messages
 * 
 * @author Nicolas
 */

@Controller
public class ChatController {

	@Autowired private ProfanityChecker profanityFilter;

	@Autowired private SessionProfanity profanity;

	@Autowired private ParticipantRepository participantRepository;

	@Autowired private SimpMessagingTemplate simpMessagingTemplate;

	@Autowired private CustomAuthenticationProvider authenticationProvider;

	@Autowired private RoomsService roomService;
	@Autowired private UsersService userService;
	@Autowired private UserMessageService userMessageService;
	@Autowired private ChatUsersService chatUsersService;
	@Autowired private ChatTenantService ChatTenantService;
	@Autowired private ChatUserLastRoomDateService chatUserLastRoomDateService;
	private final static ObjectMapper mapper = new ObjectMapper();

	private final static Map<String,Queue<UserMessage>> messagesBuffer = new ConcurrentHashMap<String, Queue<UserMessage>>();// key => roomId
	private final static Map<String,Queue<DeferredResult<String>>> responseBodyQueue =  new ConcurrentHashMap<String,Queue<DeferredResult<String>>>();// key => roomId

	//[TIMEOUTS]
	/*@Value("${timeouts.message}")
	private final Long timeOutMessage;
	 */
	/********************
	 * GET CHAT USERS LIST FOR TEST
	 *******************/
	@RequestMapping(value = "/chat/users", method = RequestMethod.POST)
	@ResponseBody
	public String getUsers(Principal principal) throws JsonProcessingException {

		Page<User> pageUsers = userService.getUsers(1, 15);
		Set<LoginEvent> userList = new HashSet<>();
		for(User user : pageUsers)
		{
			userList.add(new LoginEvent(user.getId(),user.getUsername(), user.getAvatar()));
		}
		return  new ObjectMapper().writeValueAsString(userList);
	}

	public UserMessage filterMessage( String roomStr,  ChatMessage message, Principal principal) {
		ChatUser chatUser = new ChatUser(Long.parseLong(principal.getName()));
		chatUser.setNickName(message.getUsername());
		Room room = new Room(Long.parseLong(roomStr));
		UserMessage messageToSave = new UserMessage(chatUser,room,message.getMessage());
		//message.setUsername(chatUser.getNickName());
		return messageToSave;

	}
	@MessageMapping("/{room}/chat.message")
	public ChatMessage filterMessageWS(@DestinationVariable("room") String roomStr, @Payload ChatMessage message, Principal principal) {
		//System.out.println("ZIGZAG ZIGZAG ZIGZAG ZIGZAG ZIGZAG ZIGZAG ZIGZAG ZIGZAG ZIGZAG");
		//checkProfanityAndSanitize(message);//@NEED WEBSOCKET@

		UserMessage messageToSave = filterMessage(roomStr, message, principal);
		userMessageService.addMessage(messageToSave);//DO FROM MESS BUFFER

		simpMessagingTemplate.convertAndSend("/topic/users/must/get.room.num/chat.message", roomStr);

		System.out.println("/////////////////ZIGZAG ZIGZAG ZIGZAG ZIGZAG ZIGZAG ZIGZAG ZIGZAG ZIGZAG ZIGZAG " + roomStr);		
		OperationStatus operationStatus = new OperationStatus(OperationType.SEND_MESSAGE_TO_ALL,true,"SENDING MESSAGE TO ALL USERS");
		String subscriptionStr = "/topic/users/" + principal.getName() + "/status";
		simpMessagingTemplate.convertAndSend(subscriptionStr, operationStatus);
		return message;
	}

	@RequestMapping(value = "/{room}/chat/message", method = RequestMethod.POST)
	@ResponseBody
	public void filterMessageLP(@PathVariable("room") String roomStr,@RequestBody ChatMessage message, Principal principal) {
		//System.out.println("ZIGZAG ZIGZAG ZIGZAG ZIGZAG ZIGZAG ZIGZAG ZIGZAG ZIGZAG ZIGZAG");
		//checkProfanityAndSanitize(message);//@NEED WEBSOCKET@

		UserMessage messageToSave = filterMessage(roomStr, message, principal);
		//DeferredResult<ArrayList<UserMessage>> result = new DeferredResult<>();
		//userMessageService.addMessage(messageToSave);
		Queue<UserMessage> list = messagesBuffer.get(roomStr);
		if(list == null)
		{
			list = new ConcurrentLinkedQueue<>();
			messagesBuffer.put(roomStr, list);
		}
		list.add(messageToSave);

		//send message to WS users
		simpMessagingTemplate.convertAndSend(("/" + roomStr + "/chat.message"), message);
		simpMessagingTemplate.convertAndSend("/topic/users/must/get.room.num/chat.message", roomStr);
	}

	@RequestMapping(value = "/{room}/chat/message/update", method = RequestMethod.POST)
	@ResponseBody
	public DeferredResult<String> updateMessageLP(@PathVariable("room") String room) throws JsonProcessingException {

		Long timeOut = 100000L;
		DeferredResult<String> result = new DeferredResult<String>(timeOut);
		Queue<DeferredResult<String>> queue = responseBodyQueue.get(room);
		if(queue == null)
		{
			queue = new ConcurrentLinkedQueue<DeferredResult<String>>();
		}
		responseBodyQueue.put(room, queue);		
		queue.add(result);
		return result;
	}

	@Scheduled(fixedRate=600L)
	public void processMessage(){

		for(String roomId : messagesBuffer.keySet())
		{
			Queue<UserMessage> array = messagesBuffer.get(roomId);
			Queue<DeferredResult<String>> responseList = responseBodyQueue.get(roomId);
			if(responseList != null)
			{
				for(DeferredResult<String> response : responseList)
				{
					if(responseList != null)
					{
						String str = null;
						try {
							str = mapper.writeValueAsString(ChatMessage.getAllfromUserMessages(array));
						} catch (JsonProcessingException e) {
							e.printStackTrace();
						}
						response.setResult(str);
					}
				}
			}
			userMessageService.addMessages(array);
			messagesBuffer.remove(roomId);
		}
	}


	@MessageMapping("/{room}/chat.private.{username}")
	public void filterPrivateMessage(@DestinationVariable String room,@Payload ChatMessage message, @DestinationVariable("username") String username, Principal principal) {
		checkProfanityAndSanitize(message);
		Long chatUserId = 0L;
		chatUserId = Long.parseLong(principal.getName());
		message.setUsername(principal.getName());
		OperationStatus operationStatus = new OperationStatus(OperationType.SEND_MESSAGE_TO_USER,true,"SENDING MESSAGE TO USER");
		String subscriptionStr = "/topic/users/"+chatUserId+"/status";
		simpMessagingTemplate.convertAndSend(subscriptionStr, operationStatus);

		simpMessagingTemplate.convertAndSend("/user/" + username + "/exchange/amq.direct/"+room+"/chat.message", message);
	}


	@MessageMapping("/chat.go.to.dialog/{roomId}")
	public void userGoToDialogListener(@DestinationVariable("roomId") String roomid, Principal principal) {
		//	checkProfanityAndSanitize(message);

		Long user_id = Long.parseLong(principal.getName(), 10);
		Long room_id = Long.parseLong(roomid, 10);
		ChatUser user = chatUsersService.getChatUser(user_id);
		//public ChatUserLastRoomDate(Long id, Date last_logout, Long last_room){

		// getUserLastRoomDate(Room room, ChatUser chatUser) 

		Room room = roomService.getRoom(room_id);
		ChatUserLastRoomDate last = chatUserLastRoomDateService.getUserLastRoomDate(room , user);
		last.setLastLogout(new Date());
		chatUserLastRoomDateService.updateUserLastRoomDateInfo(last);
	}


	@MessageMapping("/chat.go.to.dialog.list/{roomId}")
	public void userGoToDialogListListener(@DestinationVariable("roomId") String roomid, Principal principal) {
		//	checkProfanityAndSanitize(message);

		Long user_id = Long.parseLong(principal.getName(), 10);
		Long room_id = Long.parseLong(roomid, 10);
		ChatUser user = chatUsersService.getChatUser(user_id);
		Room room = roomService.getRoom(room_id);
		if (room == null)
			return;
		//public ChatUserLastRoomDate(Long id, Date last_logout, Long last_room){
		ChatUserLastRoomDate last = chatUserLastRoomDateService.getUserLastRoomDate( room, user);
		if (last == null)
		{
			last = new ChatUserLastRoomDate(user_id, new Date(),room );
			last.setChatUser(user);
		}
		else
		{
			last.setLastLogout(new Date());
		}		
		chatUserLastRoomDateService.updateUserLastRoomDateInfo(last);
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

		Set<ChatUser>  users_set = roomService.getRoom(room).getUsers();
		List<ChatUser> users = new  ArrayList<ChatUser>();
		users.addAll(users_set);

		users.add(roomService.getRoom(room).getAuthor());
		List<String> room_emails = new  ArrayList<String>();
		for(int i = 0; i <  users.size(); i++)
		{
			room_emails.add(users.get(i).getNickName());
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

		users.add(roomService.getRoom(room).getAuthor());
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

	@RequestMapping(value="/", method = RequestMethod.GET)
	public String  getIndex(HttpRequest request) {
		System.out.println("TEST!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
		authenticationProvider.autorization(authenticationProvider);
		return "/index.html";
	}

	@MessageExceptionHandler
	@SendToUser(value = "/exchange/amq.direct/errors", broadcast = false)
	public String handleProfanity(TooMuchProfanityException e) {
		return e.getMessage();
	}
}