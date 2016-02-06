package com.intita.wschat.polling.controllers;

import java.security.Principal;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.context.request.async.DeferredResult;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.intita.wschat.models.ChatUser;
import com.intita.wschat.models.Room;
import com.intita.wschat.services.ChatUsersService;
import com.intita.wschat.services.RoomsService;
import com.intita.wschat.services.UsersService;

@Controller
public class ChatRoomsPollingController {
	@Autowired
	private  RoomsService roomsService;
	@Autowired
	private  ChatUsersService chatUsersService;
	@Autowired 
	private UsersService usersService;
	
	
	
	private volatile static Queue<ChatUser> subscribedtoRoomsUsersBuffer = new ConcurrentLinkedQueue<ChatUser>();// key => roomId
	private volatile static Map<Long,Queue<DeferredResult<String>>> responseRoomBodyQueue =  new ConcurrentHashMap<Long,Queue<DeferredResult<String>>>();// key => roomId

	@PostConstruct
	public void initController() throws Exception {
		subscribedtoRoomsUsersBuffer.add(chatUsersService.getChatUser(40L));
		subscribedtoRoomsUsersBuffer.add(chatUsersService.getChatUser(41L));
	}
	
	@Autowired
	public ChatRoomsPollingController(RoomsService roomsService) {
		this.roomsService = roomsService;
	}

	@RequestMapping(value="/longpoll_topics",method=RequestMethod.GET)
	@ResponseBody
	public DeferredResult<String> getRooms(Principal principal) {
		Long timeOut = 1000000L;
		 DeferredResult<String> deferredResult = new DeferredResult<String>(timeOut);
		ChatUser chatUser = chatUsersService.getChatUser(principal);
		if (chatUser==null ) return deferredResult;

		Queue<DeferredResult<String>> queue = responseRoomBodyQueue.get(chatUser.getId());
		if(queue == null)
		{
			queue = new ConcurrentLinkedQueue<DeferredResult<String>>();		
		}
		
		//List<Room> rooms = new ArrayList<Room>(chatUser.getRootRooms());
		
		/*List<String> roomsNames = Room.getRoomsNames(rooms);
		if (!rooms.isEmpty()) {
			deferredResult.setResult(roomsNames);
		}*/
		
		while(responseRoomBodyQueue.putIfAbsent(chatUser.getId(), queue) == null);		
		queue.add(deferredResult);
		
		return deferredResult;
	}
	
	@Scheduled(fixedRate=1000L)
	public void processRoomsQueues() throws JsonProcessingException {
		ObjectMapper mapper = new ObjectMapper();

		for(ChatUser chatUser : subscribedtoRoomsUsersBuffer)
		{
			if (chatUser==null){
				System.out.println("WARNING: NULL USER");
				continue;
			}
			Queue<DeferredResult<String>> responseList = responseRoomBodyQueue.get(chatUser.getId());
			if (responseList==null){
				//System.out.println("WARNING: RESPONSE LIST IS CLEAR");
				continue;
			}
			for(DeferredResult<String> response : responseList)
			{
				if(responseList != null)
				{
					
					String str = mapper.writeValueAsString(roomsService.getRoomsByChatUser(chatUser));
					response.setResult(str);
				}
			}
			responseList.clear();
		//userMessageService.addMessages(array);
		}
		//roomsBuffer.clear();;
		//this.responseBodyQueue.clear();


	}
	

	@RequestMapping(value="/longpoll_topics",method=RequestMethod.POST)
	@ResponseBody
	public void addRoom(@RequestParam String roomName,Principal principal) {
ChatUser author = chatUsersService.getChatUser(principal);
if (author==null) return;
		//this.roomsService.register(roomName,author);
Room roomToSave = roomsService.register(roomName,author);


		// Update all chat requests as part of the POST request
		// See Redis branch for a more sophisticated, non-blocking approach

	}
}
