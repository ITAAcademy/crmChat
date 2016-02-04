package com.intita.wschat.polling.controllers;

import java.security.Principal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

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
import com.intita.wschat.domain.ChatMessage;
import com.intita.wschat.models.ChatUser;
import com.intita.wschat.models.Room;
import com.intita.wschat.models.User;
import com.intita.wschat.models.UserMessage;
import com.intita.wschat.services.ChatUsersService;
import com.intita.wschat.services.RoomsService;
import com.intita.wschat.services.UsersService;

@Controller
public class ChatRoomsPollingController {
	@Autowired
	private  RoomsService roomsService;
	@Autowired
	private  ChatUsersService chatUsersService;
	@Autowired UsersService usersService;

	private volatile static Map<Long,Queue<Room>> roomsBuffer = new ConcurrentHashMap<Long, Queue<Room>>();// key => roomId
	private volatile static Map<Long,Queue<DeferredResult<String>>> responseBodyQueue =  new ConcurrentHashMap<Long,Queue<DeferredResult<String>>>();// key => roomId

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

		Queue<DeferredResult<String>> queue = responseBodyQueue.get(chatUser.getId());
		if(queue == null)
		{
			queue = new ConcurrentLinkedQueue<DeferredResult<String>>();
		}
		
		//List<Room> rooms = new ArrayList<Room>(chatUser.getRootRooms());
		
		/*List<String> roomsNames = Room.getRoomsNames(rooms);
		if (!rooms.isEmpty()) {
			deferredResult.setResult(roomsNames);
		}*/
		
		while(responseBodyQueue.putIfAbsent(chatUser.getId(), queue) == null);		
		queue.add(deferredResult);
		
		return deferredResult;
	}
	
	@Scheduled(fixedRate=1000L)
	public void processQueues() throws JsonProcessingException {
		ObjectMapper mapper = new ObjectMapper();

		for(Long userId : roomsBuffer.keySet())
		{
			Queue<Room> array = roomsBuffer.get(userId);
			Queue<DeferredResult<String>> responseList = responseBodyQueue.get(userId);
			for(DeferredResult<String> response : responseList)
			{
				if(responseList != null)
				{
					String str = mapper.writeValueAsString(Room.getRoomsNames(chatUsersService.getChatUser(userId).getRootRooms()));
					response.setResult(str);
				}
			}
			responseList.clear();
		//userMessageService.addMessages(array);
			roomsService.addRooms(array);
		}
		roomsBuffer.clear();;
		//this.responseBodyQueue.clear();


	}
	

	@RequestMapping(value="/longpoll_topics",method=RequestMethod.POST)
	@ResponseBody
	public void addRoom(@RequestParam String roomName,Principal principal) {
ChatUser author = chatUsersService.getChatUser(principal);
if (author==null) return;
		//this.roomsService.register(roomName,author);
Room roomToSave = new Room();
roomToSave.setAuthor(author);
roomToSave.setName(roomName);
Queue<Room> list = roomsBuffer.get(author.getId());
if(list == null)
{
	list = new ConcurrentLinkedQueue<>();
	roomsBuffer.put(author.getId(), list);
}
list.add(roomToSave);

		// Update all chat requests as part of the POST request
		// See Redis branch for a more sophisticated, non-blocking approach

	}
}
