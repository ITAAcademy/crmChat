package com.intita.wschat.services;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.commons.collections4.IteratorUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.intita.wschat.models.ChatUser;
import com.intita.wschat.models.Room;
import com.intita.wschat.models.User;
import com.intita.wschat.models.UserMessage;
import com.intita.wschat.repositories.UserMessageRepository;

@Service
@Transactional
public class UserMessageService {

	@Autowired
	private UserMessageRepository userMessageRepository;
	
	@Autowired 
	private ChatUsersService chatUserService;
	
	@Autowired
	private RoomsService roomsService;
	
	
	public ArrayList<UserMessage> getUserMesagges(){
		return (ArrayList<UserMessage>) IteratorUtils.toList(userMessageRepository.findAll().iterator());
	}
	
	public UserMessage getUserMessage(Long id){
		return userMessageRepository.findOne(id);
	}
	/*@Transactional
	public ArrayList<UserMessage> getChatUserMessagesByAuthor(String author) {
		
		return userMessageRepository.findByAuthor(chatUserService.getChatUser(author));
	}*/
	
	public ArrayList<UserMessage> getChatUserMessagesById(Long id) {
		
		return userMessageRepository.findByAuthor(chatUserService.getChatUser(id));
	}
	
	public ArrayList<UserMessage> getUserMessagesByRoom(Room room) {
		return userMessageRepository.findByRoom(room);
	}
	public ArrayList<UserMessage> getFirst20UserMessagesByRoom(Room room) {
		return userMessageRepository.findFirst20ByRoomOrderByIdDesc(room);
	}
	
	public ArrayList<UserMessage> getUserMessagesByRoomId(Long roomId) {
		
		return userMessageRepository.findByRoom(new Room(roomId));
	}

	
	public boolean addMessage(ChatUser user, Room room,String body) {
		if(user == null || room == null || body == null) return false;
		//have premition?
	UserMessage userMessage = new UserMessage(user,room,body);
	userMessageRepository.save(userMessage);
		return true;
	}
	
	public boolean addMessage(UserMessage message) {
		if (message==null) return false;
		userMessageRepository.save(message);
		return true;
	}
	
	public boolean addMessages(Iterable<UserMessage> messages) {
		if (messages==null) return false;
		userMessageRepository.save(messages);
		return true;
	}
	
	
	public ArrayList<UserMessage> getMessagesByDate(Date date) {
		return userMessageRepository.findAllByDateAfter(date);
	}
	
	
	public ArrayList<UserMessage> getMessagesByRoomDate(Room room, Date date) {
		return userMessageRepository.findAllByRoomAndDateAfter(room, date);
	}
	
	public ArrayList<UserMessage> get10MessagesByRoomDateAfter(Room room, Date date) {
		return userMessageRepository.findFirst10ByRoomAndDateAfter(room, date);
	}
	
	public ArrayList<UserMessage> get10MessagesByRoomDateBefore(Room room, Date date) {
		return userMessageRepository.findFirst10ByRoomAndDateBefore(room, date);
	}

	
	public ArrayList<UserMessage> getMessagesByRoomDateNotUser(Room room, Date date, ChatUser user) {
		return userMessageRepository.findAllByRoomAndDateAfterAndAuthorNot(room, date, user);
	}
	
	
	public List<UserMessage> getMessagesByDateNotUser(Date date, ChatUser user) {
		return userMessageRepository.findAllByDateAfterAndAuthorNot( date, user);
	}
	
	
	public List<UserMessage> getMessagesByNotUser( ChatUser user) {
		return userMessageRepository.findAllByAuthorNot( user);
	}

	
}
