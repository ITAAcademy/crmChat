package com.intita.wschat.web;

import java.io.Console;
import java.io.IOException;
import java.security.Principal;
import java.sql.Time;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Semaphore;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import javax.annotation.PostConstruct;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.servlet.http.HttpSession;

import org.hibernate.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpRequest;
import org.springframework.messaging.MessageDeliveryException;
import org.springframework.messaging.MessagingException;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageExceptionHandler;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.annotation.SendToUser;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Controller;
import org.springframework.stereotype.Service;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.context.request.async.DeferredResult;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser.Feature;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.intita.wschat.config.CustomAuthenticationProvider;
import com.intita.wschat.domain.ChatMessage;
import com.intita.wschat.domain.SessionProfanity;
import com.intita.wschat.event.LoginEvent;
import com.intita.wschat.event.ParticipantRepository;
import com.intita.wschat.exception.ChatUserNotFoundException;
import com.intita.wschat.exception.ChatUserNotInRoomException;
import com.intita.wschat.exception.RoomNotFoundException;
import com.intita.wschat.exception.TooMuchProfanityException;
import com.intita.wschat.models.ChatConsultation;
import com.intita.wschat.models.ChatUser;
import com.intita.wschat.models.ChatUserLastRoomDate;
import com.intita.wschat.models.ConfigParam;
import com.intita.wschat.models.ConsultationRatings;
import com.intita.wschat.models.Course;
import com.intita.wschat.models.IntitaConsultation;
import com.intita.wschat.models.Lang;
import com.intita.wschat.models.Lectures;
import com.intita.wschat.models.OperationStatus;
import com.intita.wschat.models.OperationStatus.OperationType;
import com.intita.wschat.models.Room;
import com.intita.wschat.models.RoomModelSimple;
import com.intita.wschat.models.User;
import com.intita.wschat.models.UserMessage;
import com.intita.wschat.repositories.ChatLangRepository;
import com.intita.wschat.repositories.ChatUserRepository;
import com.intita.wschat.repositories.LecturesRepository;
import com.intita.wschat.services.ChatTenantService;
import com.intita.wschat.services.ChatUserLastRoomDateService;
import com.intita.wschat.services.ChatUsersService;
import com.intita.wschat.services.ConfigParamService;
import com.intita.wschat.services.ConsultationsService;
import com.intita.wschat.services.CourseService;
import com.intita.wschat.services.LecturesService;
import com.intita.wschat.services.RoomsService;
import com.intita.wschat.services.UserMessageService;
import com.intita.wschat.services.UsersService;
import com.intita.wschat.util.ProfanityChecker;
import com.intita.wschat.web.RoomController.SubscribedtoRoomsUsersBufferModal;

import jsonview.Views;

/**
 * Controller that handles WebSocket chat messages
 * 
 * @author Nicolas Haiduchok
 */
@Service
@Controller
public class ChatController {

	@Autowired
	ConfigParamService configParamService;
	private final static Logger log = LoggerFactory.getLogger(ChatController.class);

	@Autowired private ProfanityChecker profanityFilter;

	@Autowired private SessionProfanity profanity;

	@Autowired private ParticipantRepository participantRepository;

	@Autowired private SimpMessagingTemplate simpMessagingTemplate;
	@Autowired private ConsultationsService chatIntitaConsultationService;

	@Autowired private CustomAuthenticationProvider authenticationProvider;
	
	@Autowired private RoomsService chatRoomsService;

	@Autowired private RoomsService roomService;
	@Autowired private UsersService userService;
	@Autowired private UserMessageService userMessageService;
	@Autowired private ChatUsersService chatUsersService;
	@Autowired private ChatTenantService ChatTenantService;
	@Autowired private ChatUserLastRoomDateService chatUserLastRoomDateService;
	@Autowired private ChatLangRepository chatLangRepository;
	@Autowired private ConsultationsService chatConsultationsService;
	@Autowired private CourseService courseService;
	@Autowired private LecturesService lecturesService;

	private final Semaphore msgLocker =  new Semaphore(1);


	@PersistenceContext
	EntityManager entityManager;

	protected Session getCurrentHibernateSession()  {
		return entityManager.unwrap(Session.class);
	}

	private final static ObjectMapper mapper = new ObjectMapper();
	private Map<String,Map<String,Object>> langMap = new HashMap<>();

	private volatile Map<String,Queue<UserMessage>> messagesBuffer =  Collections.synchronizedMap(new ConcurrentHashMap<String, Queue<UserMessage>>());// key => roomId
	private final Map<String,Queue<DeferredResult<String>>> responseBodyQueue =  new ConcurrentHashMap<String,Queue<DeferredResult<String>>>();// key => roomId

	private final ConcurrentHashMap<String, ArrayList<Object>> infoMap = new ConcurrentHashMap<>();
	//private ConcurrentLinkedMap<DeferredResult<String>> globalInfoResult = new ConcurrentLinkedQueue<>();
	ConcurrentHashMap<DeferredResult<String>,String> globalInfoResult = new ConcurrentHashMap<DeferredResult<String>,String>();
	public void addFieldToInfoMap(String key, Object value)
	{
		ArrayList<Object> listElm = infoMap.get(key);
		if(listElm == null)
		{
			listElm = new ArrayList<>();
			infoMap.put(key, listElm);
		}
		listElm.add(value);
	}
	public Map<String, Queue<UserMessage>> getMessagesBuffer() {
		return messagesBuffer;
	}


	public static class CurrentStatusUserRoomStruct{
		private Room room;
		private ChatUser user;

		public Room getRoom() {
			return room;
		}

		public ChatUser getUser() {
			return user;
		}

		public CurrentStatusUserRoomStruct(ChatUser user, Room room) {
			this.room = room;
			this.user = user;
		}
	}


	public static CurrentStatusUserRoomStruct isMyRoom(Long roomId, Principal principal, UsersService user_service, ChatUsersService chat_user_service, RoomsService chat_room_service)
	{
		long startTime = System.currentTimeMillis();

		Room o_room = chat_room_service.getRoom(roomId);
		if(o_room == null)
			return null;
		ChatUser o_user = chat_user_service.getChatUser(principal);
		if(o_user == null)
			return null;

		Set<Room> all = o_user.getRoomsFromUsers();
		all.addAll(o_user.getRootRooms());

		if(!all.contains(o_room))
			return null;

		long timeSpend = System.currentTimeMillis() - startTime;
		log.info("isMyRoom time:" + timeSpend );
		return new CurrentStatusUserRoomStruct(o_user, o_room);
	}


	//[TIMEOUTS]
	/*@Value("${timeouts.message}")
	private final Long timeOutMessage;
	 */

	@SuppressWarnings("unchecked")
	@PostConstruct
	public void onCreate()
	{
		updateLangMap();
	}

	private void updateLangMap()
	{
		Iterable<Lang> it = chatLangRepository.findAll();
		for(Lang lg:it)
		{
			HashMap<String, Object> result = null;
			JsonFactory factory = new JsonFactory(); 
			ObjectMapper mapper = new ObjectMapper(factory); 
			mapper.configure(Feature.AUTO_CLOSE_SOURCE, true);

			TypeReference<HashMap<String,Object>> typeRef = new TypeReference<HashMap<String,Object>>() {};

			try {
				result = mapper.readValue(lg.getMap(), typeRef);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				System.err.println("Lang " + lg.getLang() + " is wrong!!!");
				e.printStackTrace();
			}
			langMap.put(lg.getLang(), result);
			log.info("Current lang pack" + langMap.toString());

		}
		return;
	}
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
			ChatUser chat_user = chatUsersService.getChatUserFromIntitaUser(user, true); 
			userList.add(new LoginEvent(user.getId(),user.getUsername(), user.getAvatar(),participantRepository.isOnline(""+chat_user.getId())));
		}
		return  new ObjectMapper().writeValueAsString(userList);
	}

	@RequestMapping(value = "/chat/lectures/getfivelike/", method = RequestMethod.POST)
	@ResponseBody
	public ArrayList<Lectures> getLecturesLike(@RequestBody String title) throws JsonProcessingException {
		List<Lectures> lecturesList = new ArrayList<Lectures>();

		int lang = getCurrentLangInt();

		if (lang == lecturesService.EN)
			lecturesList = lecturesService.getFirstFiveLecturesByTitleEnLike(title);
		else
			if (lang == lecturesService.RU)
				lecturesList = lecturesService.getFirstFiveLecturesByTitleRuLike(title);
		if (lang == lecturesService.UA)
			lecturesList = lecturesService.getFirstFiveLecturesByTitleUaLike(title);	

		return  new ArrayList<Lectures>(lecturesList);
	}

	@RequestMapping(value="/chat/lectures/get_five_titles_like/", method = RequestMethod.GET)
	@ResponseBody
	public String getLecturesTitlesLike(@RequestParam String title) throws JsonProcessingException {


		List<String> lecturesList = new ArrayList<>();		
		int lang = getCurrentLangInt();

		if (lang == lecturesService.EN)
			lecturesList = lecturesService.getFirstFiveLecturesTitlesByTitleEnLike(title);
		else
			if (lang == lecturesService.RU)
				lecturesList = lecturesService.getFirstFiveLecturesTitlesByTitleRuLike(title);
		if (lang == lecturesService.UA)
			lecturesList = lecturesService.getFirstFiveLecturesTitlesByTitleUaLike(title);	

		ObjectMapper mapper = new ObjectMapper();
		String jsonInString = mapper.writeValueAsString(lecturesList);
		return jsonInString;
	}

	@RequestMapping(value = "/{room}/chat/loadOtherMessage", method = RequestMethod.POST)
	@ResponseBody
	public ArrayList<ChatMessage> loadOtherMessage(@PathVariable("room") Long room, @RequestBody ChatMessage message, Principal principal)  {
		System.out.println("OK!!!!!!!!!!!!!!!!!!!!!!" + message.getDate());
		CurrentStatusUserRoomStruct struct = ChatController.isMyRoom(room, principal, userService, chatUsersService, roomService);//Control room from LP
		if( struct == null)
			throw new ChatUserNotInRoomException("");
		ArrayList<ChatMessage> messagesAfter = ChatMessage.getAllfromUserMessages(userMessageService.get10MessagesByRoomDateBefore(struct.getRoom(), message.getDate()));

		if(messagesAfter.size() == 0)
			return null;

		return messagesAfter;
		/*	CurrentStatusUserRoomStruct struct = ChatController.isMyRoom(room, principal, chatUserServise, roomService);//Control room from LP
		if( struct == null)
			return "{}";
		return mapper.writeValueAsString(retrieveParticipantsSubscribeAndMessagesObj(struct.getRoom()));*/
	}

	public UserMessage filterMessage( Long roomStr,  ChatMessage message, Principal principal) {
		CurrentStatusUserRoomStruct struct = ChatController.isMyRoom(roomStr, principal, userService, chatUsersService, roomService);//Control room from LP
		if(struct == null || !struct.room.isActive() || message.getMessage().isEmpty())//cant add msg
			return null;

		UserMessage messageToSave = new UserMessage(struct.user, struct.room, message);
		//if (messageToSave.getRoom().getName()==null || messageToSave.getBody() == null) return null;
		//message.setUsername(chatUser.getNickName());
		return messageToSave;
	}

	public UserMessage filterMessageWithoutFakeObj( ChatUser chatUser,  ChatMessage message, Room room) {
		if(!room.isActive() || message.getMessage().isEmpty())//cant add msg
			return null;

		UserMessage messageToSave = new UserMessage(chatUser,room,message);
		return messageToSave;
	}

	public synchronized void addMessageToBuffer(Long room, UserMessage message)
	{
		synchronized (messagesBuffer)
		{
			Queue<UserMessage> list = messagesBuffer.get(room.toString());
			if(list == null)
			{
				list = new ConcurrentLinkedQueue<>();
				messagesBuffer.put(room.toString(), list);
			}
			list.add(message);
			log.info("ADD: " + list.size());
		}


		//send message to WS users
		simpMessagingTemplate.convertAndSend("/topic/users/must/get.room.num/chat.message", room);
		addFieldToInfoMap("newMessage", room);
	}


	@MessageMapping("/{room}/chat.message")
	public ChatMessage filterMessageWS(@DestinationVariable("room") Long room, @Payload ChatMessage message, Principal principal) {
		//System.out.println("ZIGZAG ZIGZAG ZIGZAG ZIGZAG ZIGZAG ZIGZAG ZIGZAG ZIGZAG ZIGZAG");
		//checkProfanityAndSanitize(message);//@NEED WEBSOCKET@
		CurrentStatusUserRoomStruct struct = ChatController.isMyRoom(room, principal, userService, chatUsersService, roomService);//Control room from LP
		if(struct == null)
			return null;

		ChatUser r = new ChatUser(Long.parseLong(principal.getName()));//chatUsersService.isMyRoom(roomStr, principal.getName());
		if(r == null)
			return null;

		Room o_room = struct.getRoom();
		UserMessage messageToSave = filterMessageWithoutFakeObj(r, message, o_room);//filterMessage(roomStr, message, principal);
		if (messageToSave!=null)
		{
			addMessageToBuffer(room, messageToSave);

			OperationStatus operationStatus = new OperationStatus(OperationType.SEND_MESSAGE_TO_ALL,true,"SENDING MESSAGE TO ALL USERS");
			String subscriptionStr = "/topic/users/" + principal.getName() + "/status";
			simpMessagingTemplate.convertAndSend(subscriptionStr, operationStatus);
			return message;
		}
		return null;
	}

	@RequestMapping(value = "/{room}/chat/message", method = RequestMethod.POST)
	@ResponseBody
	public void filterMessageLP(@PathVariable("room") Long room,@RequestBody ChatMessage message, Principal principal) {
		//checkProfanityAndSanitize(message);//@NEED WEBSOCKET@
		UserMessage messageToSave = filterMessage(room, message, principal);
		if (messageToSave!=null)
		{
			addMessageToBuffer(room, messageToSave);
			simpMessagingTemplate.convertAndSend(("/topic/" + room.toString() + "/chat.message"), message);
		}
	}

	public void filterMessageBot( Long room,ChatMessage message, UserMessage to_save) {
		//checkProfanityAndSanitize(message);//@NEED WEBSOCKET@
		if (to_save != null)
		{
			addMessageToBuffer(room, to_save);
			simpMessagingTemplate.convertAndSend(("/topic/" + room.toString() + "/chat.message"), message);
		}
	}



	@RequestMapping(value = "/{room}/chat/message/update", method = RequestMethod.POST)
	@ResponseBody
	public DeferredResult<String> updateMessageLP(@PathVariable("room") Long room) throws JsonProcessingException {
		Long timeOut = 60000L;
		DeferredResult<String> result = new DeferredResult<String>(timeOut, "{}");
		Queue<DeferredResult<String>> queue = responseBodyQueue.get(room.toString());
		if(queue == null)
		{
			queue = new ConcurrentLinkedQueue<DeferredResult<String>>();
			responseBodyQueue.put(room.toString(), queue);
		}
		//System.out.println("updateMessageLP responseBodyQueue:"+queue.size());
		queue.add(result);
		return result;
	}

	@Scheduled(fixedDelay=600L)
	public void processMessage(){
		for(String roomId : messagesBuffer.keySet())
		{
			Queue<UserMessage> array = messagesBuffer.get(roomId);
			Queue<DeferredResult<String>> responseList = responseBodyQueue.get(roomId);
			if(responseList != null)
			{
				String str = "";
				try {
					str = mapper.writeValueAsString(ChatMessage.getAllfromUserMessages(array));
				} catch (JsonProcessingException e) {
					e.printStackTrace();
				}	
				for(DeferredResult<String> response : responseList)
				{
					if(!response.isSetOrExpired())
						response.setResult(str);
					//responseList.remove(response);
				}
				responseList.clear();
				//System.out.println("processMessage responseBodyQueue:"+responseList.size());
			}

			boolean ok = userMessageService.addMessages(array);
			messagesBuffer.remove(roomId);
		}
	}

	@RequestMapping(value = "/chat/global/lp/info", method = RequestMethod.POST)
	@ResponseBody
	public DeferredResult<String> updateGlobalInfoLP(Principal principal) throws JsonProcessingException {

		Long timeOut = 10000L;
		participantRepository.add(principal.getName());
		DeferredResult<String> result = new DeferredResult<String>(timeOut, "{}");
		globalInfoResult.put(result,principal.getName());

		LoginEvent loginEvent = new LoginEvent(Long.parseLong(principal.getName()), "test",participantRepository.isOnline(principal.getName()));
		simpMessagingTemplate.convertAndSend("/topic/addFieldToInfoMap", loginEvent);

		System.out.println("globalInfoResult.add:"+principal.getName());
		return result;
	}

	@Scheduled(fixedDelay=5000L)
	public void processGlobalInfo(){
		String result;
		try {
			result = mapper.writeValueAsString(infoMap);
		} catch (JsonProcessingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			result = "{}";
		}
		infoMap.clear();
		for(DeferredResult<String> nextUser : globalInfoResult.keySet())
		{

			//	System.out.println("globalInfoResult.remove:"+globalInfoResult.get(nextUser));
			participantRepository.removeParticipant((globalInfoResult.get(nextUser)));

			LoginEvent loginEvent = new LoginEvent(Long.parseLong(globalInfoResult.get(nextUser)), globalInfoResult.get(nextUser),participantRepository.isOnline(globalInfoResult.get(nextUser)));
			simpMessagingTemplate.convertAndSend("/topic/chat.logout", loginEvent);

			if(!nextUser.isSetOrExpired())
				nextUser.setResult(result);		
		}
		globalInfoResult.clear();

	}
	//NOT TEST!!!
	@MessageMapping("/{room}/chat.private.{username}")
	public void filterPrivateMessage(@DestinationVariable Long room,@Payload ChatMessage message, @DestinationVariable("username") String username, Principal principal) {
		checkProfanityAndSanitize(message);
		Long chatUserId = 0L;
		chatUserId = Long.parseLong(principal.getName());
		message.setUsername(principal.getName());
		OperationStatus operationStatus = new OperationStatus(OperationType.SEND_MESSAGE_TO_USER,true,"SENDING MESSAGE TO USER");
		String subscriptionStr = "/topic/users/"+chatUserId+"/status";
		simpMessagingTemplate.convertAndSend(subscriptionStr, operationStatus);

		simpMessagingTemplate.convertAndSend("/user/" + username + "/exchange/amq.direct/"+room+"/chat.message", message);
	}


	/*
	 * Go into room
	 */

	@MessageMapping("/chat.go.to.dialog/{roomId}")
	public void userGoToDialogListener(@DestinationVariable("roomId") Long roomId, Principal principal) {
		//	checkProfanityAndSanitize(message);
		CurrentStatusUserRoomStruct struct =  isMyRoom(roomId, principal, userService, chatUsersService, roomService);
		if(struct == null)
			throw new ChatUserNotInRoomException("CurrentStatusUserRoomStruct struct is null");

		ChatUser user = struct.getUser();
		Room room = struct.getRoom();

		if (room==null) 
			throw new RoomNotFoundException("room is null");

		ChatUserLastRoomDate last = chatUserLastRoomDateService.getUserLastRoomDate(room , user);
		last.setLastLogout(new Date());
		chatUserLastRoomDateService.updateUserLastRoomDateInfo(last);
	}
	@RequestMapping(value = "/chat.go.to.dialog/{roomId}", method = RequestMethod.POST)
	@ResponseBody
	public void userGoToDialogListenerLP(@PathVariable("roomId") Long roomid, Principal principal) {
		userGoToDialogListener(roomid, principal);
	}

	/*
	 * Out from room
	 */
	@MessageMapping("/chat.go.to.dialog.list/{roomId}")
	public void userGoToDialogListListener(@DestinationVariable("roomId") Long roomId, Principal principal, @RequestBody Map<String,Object> params) {
		//	checkProfanityAndSanitize(message);


		CurrentStatusUserRoomStruct struct =  isMyRoom(roomId, principal, userService, chatUsersService, roomService);
		if(struct == null)
			return;

		ChatUser user = struct.getUser();
		Room room = struct.getRoom();

		if (room == null)
			return;
		//public ChatUserLastRoomDate(Long id, Date last_logout, Long last_room){
		ChatUserLastRoomDate last = chatUserLastRoomDateService.getUserLastRoomDate( room, user);
		if (last == null)
		{
			last = new ChatUserLastRoomDate(user.getId(), new Date(),room );
			last.setChatUser(user);
		}
		else
		{
			last.setLastLogout(new Date());
		}		
		chatUserLastRoomDateService.updateUserLastRoomDateInfo(last);

		updateRoomsByUser(user, (HashMap<String, Object>) params.get("roomForUpdate"));

	}

	public void updateRoomsByUser(ChatUser user, HashMap<String, Object> roomsId)
	{
		ArrayList<Room> roomForUpdate = new ArrayList<>();

		//HashMap<String, Object> roomsId = (HashMap<String, Object>) params.get("roomForUpdate");
		Set<String> set = roomsId.keySet();

		if(roomsId.size() > 0)
		{
			for (String string : set) {
				try {
					roomForUpdate.add(new Room(Long.parseLong(string)));
				} catch (Exception e) {
					System.out.println(e.getMessage());
				}	
			}
		}
		sendMessageForUpdateRoomsByUser(user, roomForUpdate);
	}
	public void sendMessageForUpdateRoomsByUser(ChatUser user, ArrayList<Room> roomForUpdate)
	{
		RoomController.addFieldToSubscribedtoRoomsUsersBuffer(new SubscribedtoRoomsUsersBufferModal(user, roomForUpdate));
		simpMessagingTemplate.convertAndSend("/topic/chat/rooms/user." + user.getId(), new RoomController.UpdateRoomsPacketModal(roomService.getRoomsByChatUserAndList(user, roomForUpdate), false));
	}
	public void updateRoomByUser(ChatUser user, Room room)
	{
		ArrayList<Room> roomForUpdate = new ArrayList<>();
		roomForUpdate.add(room);
		sendMessageForUpdateRoomsByUser(user, roomForUpdate);
	}

	@RequestMapping(value = "/chat/update/dialog_list", method = RequestMethod.POST)
	@ResponseBody
	public void updateRoomsByList(Principal principal, @RequestBody Map<String,Object> params) {
		ChatUser user = chatUsersService.getChatUser(principal);
		if(user != null)
		{
			updateRoomsByUser(user, (HashMap<String, Object>) params.get("roomForUpdate"));
		}
	}

	@RequestMapping(value = "/chat.go.to.dialog.list/{roomId}", method = RequestMethod.POST)
	@ResponseBody
	public void userGoToDialogListListenerLP(@PathVariable("roomId") Long roomId, Principal principal, @RequestBody Map<String,Object> params) {

		userGoToDialogListListener(roomId, principal, params);
	}

	/* 
	 *Work only on WS
	 */
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


	@RequestMapping(value="/get_commands_like", method = RequestMethod.GET)
	@ResponseBody
	public String getCommandsLike(@RequestParam String command) throws JsonProcessingException{
		List<String> commands = new  ArrayList<String>();
		commands.add(new String("createDialogWithBot"));
		commands.add(new String("createConsultation"));

		List<String> result = new  ArrayList<String>();
		for (int i = 0; i < commands.size(); i++)
		{
			if (commands.get(i).matches(new String(".*" + command + ".*")))
				result.add(commands.get(i));
		}

		ObjectMapper mapper = new ObjectMapper();

		String jsonInString = mapper.writeValueAsString(result);
		return jsonInString;
	}

	@RequestMapping(value="/get_users_emails_like", method = RequestMethod.GET)
	@ResponseBody
	public String getEmailsLike(@RequestParam String login, @RequestParam Long room, boolean eliminate_users_of_current_room) throws JsonProcessingException {
		List<String> emails = null;

		if(eliminate_users_of_current_room)
		{
			List<ChatUser> users = new  ArrayList<ChatUser>();
			Set<ChatUser>  users_set = null;
			users_set = roomService.getRoom(room).getUsers();
			users.addAll(users_set);
			users.add(roomService.getRoom(room).getAuthor());

			List<Long> room_emails = new  ArrayList<>();
			for(int i = 0; i <  users.size(); i++)
			{
				User i_user = users.get(i).getIntitaUser();
				if(i_user != null)
					room_emails.add(i_user.getId());
			}
			emails = userService.getUsersEmailsFist5(login, room_emails);
		}
		else
			emails = userService.getUsersEmailsFist5(login);

		ObjectMapper mapper = new ObjectMapper();

		String jsonInString = mapper.writeValueAsString(emails);
		return jsonInString;
	}

	@RequestMapping(value="/get_all_users_emails_like", method = RequestMethod.GET)
	@ResponseBody
	public String getAllEmailsLike(@RequestParam String email) throws JsonProcessingException {
		List<String> emails = userService.getUsersEmailsFist5(email);
		ObjectMapper mapper = new ObjectMapper();
		String jsonInString = mapper.writeValueAsString(emails);
		return jsonInString;
	}

	@RequestMapping(value="/get_courses_like", method = RequestMethod.GET)
	@ResponseBody
	public String getCoursesLike(@RequestParam String prefix, @RequestParam String lang) throws JsonProcessingException {
		ArrayList<String> coursesNames = courseService.getAllCoursesNamesWithTitlePrefix(prefix,lang);
		ObjectMapper mapper = new ObjectMapper();

		String jsonInString = mapper.writeValueAsString(coursesNames);
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
			LoginEvent userData = new LoginEvent(singleChatUser.getId(),nn,participantRepository.isOnline(""+singleChatUser.getId()));
			usersData.add(userData);	
		}
		return usersData;
		/*ObjectMapper mapper = new ObjectMapper();

					String jsonInString = mapper.writeValueAsString(nicks);
					return jsonInString;*/
	}
	@RequestMapping(value="/get_id_by_username",method = RequestMethod.GET)
	@ResponseBody
	public Long getIdByUsername(@RequestParam String intitaUsername){
		User user = userService.getUser(intitaUsername);
		if (user == null) throw new ChatUserNotFoundException("");
		return user.getId();
	}
	@RequestMapping(value="/get_course_alias_by_title",method = RequestMethod.GET)
	@ResponseBody
	public String getCourseAliasByTitle(@RequestParam String title,@RequestParam String lang){
		Course course = courseService.getByTitle(title,lang);
		String jsonInString = null;
		try {
			jsonInString = mapper.writeValueAsString(course.getAlias());
		} catch (JsonProcessingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return jsonInString;
	}

	@RequestMapping(value="/get_users_nicknames_like_without_room", method = RequestMethod.GET)
	@ResponseBody
	public Set<LoginEvent>  getNickNamesLike2(@RequestParam String nickName) throws JsonProcessingException {


		List<ChatUser> nicks = chatUsersService.getChatUsersLike(nickName);

		Set<LoginEvent> usersData = new HashSet<LoginEvent>();
		for (ChatUser nick: nicks){
			usersData.add(new LoginEvent(nick.getId(), nick.getNickName(),participantRepository.isOnline(""+nick.getId())));
		}
		return usersData;
		/*ObjectMapper mapper = new ObjectMapper();

					String jsonInString = mapper.writeValueAsString(nicks);
					return jsonInString;*/
	}

	public static class ChatLangEnum{ 
		public static final String UA = "ua";
		public static final String EN = "en";
		public static final String RU = "ru";
		public static final ArrayList<String> LANGS = new ArrayList<String>(
			    Arrays.asList(UA, EN, RU));
		
	}
	public static String getCurrentLang()
	{
		ServletRequestAttributes attr = (ServletRequestAttributes) RequestContextHolder.currentRequestAttributes();
		HttpSession session = attr.getRequest().getSession(false);
		String lg;
		if(session != null)
			lg = (String) session.getAttribute("chatLg");
		else
			lg = "ua";
		if(lg == null)
			return "ua";
		return lg;
	}

	public int getCurrentLangInt()
	{
		String lang = getCurrentLang();
		if (lang.equals(("ua")))
			return 0;
		if (lang.equals(("ru")))
			return 1;
		return 2;
	}

	public Map<String, Object> getLocolization()
	{
		return langMap.get(getCurrentLang());
	}

	private static java.sql.Date getSqlDate(String date_str)
	{	  
		java.util.Date apptDay = null;
		DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
		java.sql.Date sqlDate = null;

		try
		{
			apptDay = (java.util.Date) df.parse(date_str);
		}
		catch(ParseException e)
		{
			System.out.println("Please set a valid date! Format is yyyy-mm-dd");
		}
		sqlDate = new java.sql.Date(apptDay.getTime());
		return sqlDate;
	}

	@RequestMapping(value = "/chat/rooms/create/consultation/", method = RequestMethod.POST)
	@ResponseBody
	public void createConsultation(Principal principal, @RequestBody Map<Object, String > param/*, @RequestBody String date_str,
			@RequestBody String time_begin, @RequestBody String time_end*/
			) throws ParseException
	{
		String time_begin = param.get("begin");
		String time_end = param.get("end");
		String date_str = param.get("date");
		String lection_title = param.get("lection");
		String teacher_email = param.get("email");

		IntitaConsultation consultation = new IntitaConsultation();	
		Date date = getSqlDate(date_str);

		DateFormat formatter = new SimpleDateFormat("HH:mm:ss");

		Time start_time = new Time(formatter.parse(time_begin).getTime());		
		Time endTime = new Time(formatter.parse(time_end).getTime());

		Lectures lecture = null;

		int lang = getCurrentLangInt();

		if (lang == lecturesService.EN)
			lecture = lecturesService.getLectureByTitleEN(lection_title);
		else
			if (lang == lecturesService.RU)
				lecture = lecturesService.getLectureByTitleRU(lection_title);
		if (lang == lecturesService.UA)
			lecture = lecturesService.getLectureByTitleUA(lection_title);

		if (date == null)
		{			
			System.out.println("Date null!!!!!!");
			return;
		}

		if (start_time == null)
		{
			System.out.println("start_time null!!!!!!");
			return;
		}

		if (endTime == null)
		{
			System.out.println("endTime null!!!!!!");
			return;
		}

		if (teacher_email == null)
		{
			System.out.println("teacher_email null!!!!!!");
			return;
		}

		if (lecture == null)
		{
			System.out.println("lecture null!!!!!!");
			return;
		}

		consultation.setDate(date);
		consultation.setStartTime(start_time);
		consultation.setFinishTime(endTime);
		consultation.setAuthor(userService.getUser(principal));

		User consultant = userService.getUser(teacher_email);

		ChatUser chatUserTest = consultant.getChatUser();
		if (chatUserTest == null)
		{
			chatUserTest = chatUsersService.getChatUserFromIntitaEmail(consultant.getEmail(), false);
			consultant.setChatUser(chatUserTest);
		}

		consultation.setConsultant(consultant);
		consultation.setLecture(lecture);

		IntitaConsultation consultation_registered = chatIntitaConsultationService.registerConsultaion(consultation);

		ChatConsultation chatConsultation = chatConsultationsService.getByIntitaConsultation(consultation_registered);

		//Room room_consultation = chatConsultation.getRoom();		

		Long chatUserId = Long.parseLong(principal.getName());	
		
		List<RoomModelSimple> list = chatRoomsService.getRoomsModelByChatUser(chatUserTest);
		
		simpMessagingTemplate.convertAndSend("/topic/chat/rooms/user." + chatUserId,
				new RoomController.UpdateRoomsPacketModal (list,false));
		
		//this said ti author that he nust update room`s list
		ChatUser author = chatUsersService.getChatUser(principal);
		RoomController.addFieldToSubscribedtoRoomsUsersBuffer(new SubscribedtoRoomsUsersBufferModal(author));
		//777
	}

	public void addLocolization(Model model)
	{
		String lang = getCurrentLang();
		model.addAttribute("lgPack", langMap.get(lang));
		List<ConfigParam> config =  configParamService.getParams();
		HashMap<String,String> configMap = ConfigParam.listAsMap(config);
		configMap.put("currentLang", lang);
		model.addAttribute("config", configMap);
		model.addAttribute("phrasesPack", roomService.getPhrases());
	}

	@RequestMapping(value="/", method = RequestMethod.GET)
	public String  getIndex(HttpRequest request, Model model) {
		authenticationProvider.autorization(authenticationProvider);
		updateLangMap();
		addLocolization(model);
		return "index";
	}

	@RequestMapping(value="/consultationTemplate.html", method = RequestMethod.GET)
	public String  getConsultationTemplate(HttpRequest request, Model model) {
		Set<ConsultationRatings> retings = chatConsultationsService.getAllSupportedRetings();
		Map<String, Object> ratingLang = (Map<String, Object>) getLocolization().get("ratings");
		for (ConsultationRatings consultationRatings : retings) {
			ConsultationRatings consultationRatingsCopy = new ConsultationRatings(consultationRatings);
			String translate_rating_name = (String)ratingLang.get(consultationRatingsCopy.getName());
			consultationRatingsCopy.setName(translate_rating_name);
			//retings.add(consultationRatingsCopy);
		}
		model.addAttribute("ratingsPack", retings);
		addLocolization(model);
		return getTeachersTemplate(request, "consultationTemplate", model);
	}	

	@RequestMapping(value="/{page}.html", method = RequestMethod.GET)
	public String  getTeachersTemplate(HttpRequest request, @PathVariable("page") String page, Model model) {
		//HashMap<String,Object> result =   new ObjectMapper().readValue(JSON_SOURCE, HashMap.class);
		addLocolization(model);

		return page;
	}

	@MessageExceptionHandler
	@SendToUser(value = "/exchange/amq.direct/errors", broadcast = false)
	public String handleProfanity(TooMuchProfanityException e) {
		return e.getMessage();
	}

	@RequestMapping(value="/get_room_messages", method = RequestMethod.GET)
	@ResponseBody
	public String  getRoomMessages(@RequestParam Long roomId, Principal principal) throws JsonProcessingException {
		mapper.disable(MapperFeature.DEFAULT_VIEW_INCLUSION);
		boolean isAdmin = userService.isAdmin(principal.getName());
		if(!isAdmin)
			return null;
		return mapper.writerWithView(Views.Public.class).writeValueAsString(userMessageService.getUserMessagesByRoomId(roomId));
	}
	@MessageExceptionHandler(MessageDeliveryException.class)
	public String handleMessageDeliveryException(MessageDeliveryException e) {
		//log.error("MessageDeliveryException handler executed");
		return "MessageDeliveryException handler executed";
	}
	@MessageExceptionHandler(NumberFormatException.class)
	public String handleNumberFormatException(Exception ex) {
		//logger.error("NumberFormatException handler executed");
		return "NumberFormatException handler executed";
	}
	@MessageExceptionHandler(Exception.class)
	public String handleMessageException(Exception ex) {
		//log.error("NumberFormatException handler executed");
		return "NumberFormatException handler executed";
	}


}