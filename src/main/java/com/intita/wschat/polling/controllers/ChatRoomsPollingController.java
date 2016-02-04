package com.intita.wschat.polling.controllers;

import java.security.Principal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.context.request.async.DeferredResult;

import com.intita.wschat.models.ChatUser;
import com.intita.wschat.models.Room;
import com.intita.wschat.models.User;
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

	private final Map<DeferredResult<List<String>>, Long> chatRequests =
			new ConcurrentHashMap<DeferredResult<List<String>>, Long>();


	@Autowired
	public ChatRoomsPollingController(RoomsService roomsService) {
		this.roomsService = roomsService;
	}

	@RequestMapping(value="/longpoll_topics",method=RequestMethod.GET)
	@ResponseBody
	public DeferredResult<List<String>> getRooms(Principal principal) {
		try {
			Thread.sleep(1000);//imitation of some calculation;
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		final DeferredResult<List<String>> deferredResult = new DeferredResult<List<String>>(null, Collections.emptyList());
		ChatUser chatUser = chatUsersService.getChatUser(principal);
		if (chatUser==null ) return deferredResult;
		this.chatRequests.put(deferredResult, chatUser.getId());

		deferredResult.onCompletion(new Runnable() {
			@Override
			public void run() {
				chatRequests.remove(deferredResult);
			}
		});
		
		
		
		List<Room> rooms = new ArrayList<Room>(chatUser.getRootRooms());
		
		List<String> roomsNames = Room.getRoomsNames(rooms);
		if (!rooms.isEmpty()) {
			deferredResult.setResult(roomsNames);
		}
		return deferredResult;
	}

	@RequestMapping(value="/longpoll_topics",method=RequestMethod.POST)
	@ResponseBody
	public void addRoom(@RequestParam String roomName,Principal principal) {
ChatUser author = chatUsersService.getChatUser(principal);
if (author==null) return;
		this.roomsService.register(roomName,author);

		// Update all chat requests as part of the POST request
		// See Redis branch for a more sophisticated, non-blocking approach

		for (Entry<DeferredResult<List<String>>, Long> entry : this.chatRequests.entrySet()) {
			List<Room> rooms = this.roomsService.getRoomByAuthor(author.getIntitaUser());
			List<String> roomsNames = Room.getRoomsNames(rooms);
			entry.getKey().setResult(roomsNames);
		}
	}
}
