package com.intita.wschat.services;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;

import org.apache.commons.collections4.IteratorUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.intita.wschat.models.ChatUser;
import com.intita.wschat.models.Room;
import com.intita.wschat.models.User;
import com.intita.wschat.models.UserMessage;
import com.intita.wschat.repositories.RoomRepository;
import com.intita.wschat.repositories.UserMessageRepository;

@Service
public class UserMessageService {

	@Autowired
	private UserMessageRepository userMessageRepository;
	
	@Autowired 
	private ChatUsersService chatUserService;
	
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
	/*@Transactional
	public ArrayList<UserMessage> getChatUserMessagesByAuthor(String author) {
		
		return userMessageRepository.findByAuthor(chatUserService.getChatUser(author));
	}*/
	@Transactional
	public ArrayList<UserMessage> getChatUserMessagesById(Long id) {
		
		return userMessageRepository.findByAuthor(chatUserService.getChatUser(id));
	}
	@Transactional
	public ArrayList<UserMessage> getUserMessagesByRoom(Room room) {
		return userMessageRepository.findByRoom(room);
	}
	@Transactional
	public UserMessage getLastUserMessageByRoom(Room room){
		return userMessageRepository.findFirstByRoomOrderByDateDesc(room);
	}
	public ArrayList<UserMessage> getFirst20UserMessagesByRoom(Room room) {
		return userMessageRepository.findFirst20ByRoomOrderByIdDesc(room);
	}
	
	public ArrayList<UserMessage> getUserMessagesByRoomId(Long roomId) {
		
		return userMessageRepository.findByRoom(new Room(roomId));
	}

	@Transactional
	public boolean addMessage(ChatUser user, Room room,String body) {
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
	@Transactional
	public boolean addMessages(Iterable<UserMessage> messages) {
		if (messages==null) return false;
		userMessageRepository.save(messages);
		return true;
	}
	
	@Transactional
	public ArrayList<UserMessage> getMessagesByDate(Date date) {
		return userMessageRepository.findAllByDateAfter(date);
	}
	
	@Transactional
	public ArrayList<UserMessage> getMessagesByRoomDate(Room room, Date date) {
		return userMessageRepository.findAllByRoomAndDateAfter(room, date);
	}
	@Transactional
	public ArrayList<UserMessage> get10MessagesByRoomDateAfter(Room room, Date date) {
		return userMessageRepository.findFirst10ByRoomAndDateAfter(room, date);
	}
	@Transactional
	public ArrayList<UserMessage> get10MessagesByRoomDateBefore(Room room, Date date) {
		return userMessageRepository.findFirst10ByRoomAndDateBeforeOrderByIdDesc(room, date);
	}

	@Transactional
	public ArrayList<UserMessage> getMessagesByRoomDateNotUser(Room room, Date date, ChatUser user) {
		return userMessageRepository.findAllByRoomAndDateAfterAndAuthorNot(room, date, user);
	}
	
	@Transactional
	public List<UserMessage> getMessagesByDateNotUser(Date date, ChatUser user) {
		return userMessageRepository.findAllByDateAfterAndAuthorNot( date, user);
	}
	
	@Transactional
	public Set<UserMessage> getMessagesByNotUser( ChatUser user) {
		return userMessageRepository.findAllByAuthorNot( user);
	}

	
}
