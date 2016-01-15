package com.sergialmar.wschat.services;

import java.util.ArrayList;

import org.apache.commons.collections4.IteratorUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sergialmar.wschat.models.Room;
import com.sergialmar.wschat.models.User;
import com.sergialmar.wschat.models.UserMessage;
import com.sergialmar.wschat.repositories.UserMessageRepository;

@Service
public class UserMessageService {

	@Autowired
	private UserMessageRepository userMessageRepository;
	
	@Autowired 
	private UsersService userService;
	
	@Autowired
	private RoomsService roomsService;
	
	@Transactional
	public ArrayList<UserMessage> getUserMesagges(){
		return (ArrayList<UserMessage>) IteratorUtils.toList(userMessageRepository.findAll().iterator());
	}
	@Transactional
	public UserMessage getUserMessage(Long id){
		return userMessageRepository.findOne(id);
	}
	@Transactional
	public ArrayList<UserMessage> getUserMessagesByAuthor(String author) {
		
		return userMessageRepository.findByAuthor(userService.getUser(author));
	}
	@Transactional
	public ArrayList<UserMessage> getUserMessagesByRoom(Room room) {
		
		return userMessageRepository.findByRoom(room);
	}
	@Transactional
	public boolean addMessage(User user, Room room,String body) {
		if(user == null || room == null || body == null) return false;
		//have premition?
	UserMessage userMessage = new UserMessage(user,room,body);
	userMessageRepository.save(userMessage);
		return true;
	}
	@Transactional
	public boolean addMessage(UserMessage message) {
		if (message==null) return false;
		userMessageRepository.save(message);
		return true;
	}
	
	
}
