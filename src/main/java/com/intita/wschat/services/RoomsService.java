package com.intita.wschat.services;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.messaging.MessagingException;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.intita.wschat.models.ChatUser;
import com.intita.wschat.models.ChatUserLastRoomDate;
import com.intita.wschat.models.Phrase;
import com.intita.wschat.models.Room;
import com.intita.wschat.models.RoomModelSimple;
import com.intita.wschat.models.User;
import com.intita.wschat.models.UserMessage;
import com.intita.wschat.repositories.ChatPhrasesRepository;
import com.intita.wschat.repositories.RoomRepository;
import com.intita.wschat.web.ChatController;

@Service
public class RoomsService {

	@Autowired private RoomRepository roomRepo;
	@Autowired private ChatPhrasesRepository phrasesRepo;
	@Autowired private UsersService userService;
	@Autowired private ChatUsersService chatUserService;
	@Autowired private ChatUserLastRoomDateService chatLastRoomDateService;
	@Autowired private UserMessageService userMessageService;
	@Autowired private SimpMessagingTemplate simpMessagingTemplate;

	@Autowired private ChatController chatController;

	@PostConstruct
	@Transactional
	public void createDefRoom() {

	}

	@Transactional
	public Page<Room> getRooms(int page, int pageSize){
		return roomRepo.findAll(new PageRequest(page-1, pageSize)); 

	}

	@Transactional
	public List<Room> getRooms(){
		return (List<Room>) roomRepo.findAll(); 
	}

	@Transactional
	public Room getRoom(Long id){
		if(id < 0)
			return null;
		return roomRepo.findOne(id);
	}

	@Transactional
	public boolean addRooms(Iterable<Room> rooms) {
		if (rooms==null) return false;
		roomRepo.save(rooms);
		return true;
	}

	@Transactional
	public Room getRoom(String name) {
		return roomRepo.findByName(name);
	}
	@Transactional
	public Room getPrivateRoom(ChatUser author, ChatUser privateUser) {
		return roomRepo.findByAuthorAndTypeAndUsersContaining(author, (short) 1, privateUser);
	}
	@Transactional
	public ArrayList<Room> getRoomByAuthor(String author) {

		return roomRepo.findByAuthor(chatUserService.getChatUser(author));
	}
	@Transactional
	public ArrayList<Room> getRoomByAuthor(ChatUser user) {

		return roomRepo.findByAuthor(user);
	}

	@Transactional(readOnly = false)
	public Room register(String name, ChatUser author) {
		if (name==null || name.length()==0) return null;
		Room r = new Room();
		r.setAuthor(author);
		r.setName(name);
		r.setType((short) 0);
		chatLastRoomDateService.addUserLastRoomDateInfo(author, r);
		//r.addUser(author);//@BAG@
		roomRepo.save(r);
		return r;
	}
	@Transactional(readOnly = false)
	public Room register(String name, ChatUser author, short type) {
		Room r = new Room();
		r.setAuthor(author);
		r.setName(name);
		r.setType(type);
		chatLastRoomDateService.addUserLastRoomDateInfo(author, r);
		//r.addUser(author);//@BAG@
		roomRepo.save(r);
		return r;
	}
	@Transactional(readOnly = false)
	public boolean unRegister(String name, ChatUser author) {
		Room room = roomRepo.findByName(name);
		if(!author.getRootRooms().contains(room))
			return false;
		room.setActive(false);
		roomRepo.save(room);//@NEED_ASK@
		return true;
	}

	@Transactional(readOnly = false)
	public boolean addUserToRoom(Long id, User user) {
		Room room = roomRepo.findOne(id);
		addUserToRoom(chatUserService.getChatUser(id), room);		
		return true;
	}
	@Transactional(readOnly = false)
	public boolean update(Room room){
		roomRepo.save(room);

		Map<String, Object> sendedMap = new HashMap<>();
		sendedMap.put("updateRoom", new RoomModelSimple(0, new Date().toString(), room,userMessageService.getLastUserMessageByRoom(room)));

		String subscriptionStr = "/topic/users/info";
		ObjectMapper mapper =  new ObjectMapper();
		//mapper.disable(MapperFeature.DEFAULT_VIEW_INCLUSION);

		try {
			simpMessagingTemplate.convertAndSend(subscriptionStr, sendedMap);
		} catch (MessagingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		chatController.addFieldToInfoMap("updateRoom", sendedMap);
		return true;
	}

	@Transactional(readOnly = false)
	public boolean addUserByNameToRoom(Long id, String name) {
		Room room = roomRepo.findOne(id);
		return addUserToRoom(chatUserService.getChatUser(name), room);
	}

	@Transactional(readOnly = false)
	public boolean addUserToRoom(ChatUser user, Room room) {
		if(room == null)
			return false;
		if(user == null)
			return false;
		//have premition?

		room.addUser(user);
		roomRepo.save(room);
		chatLastRoomDateService.addUserLastRoomDateInfo(user, room);
		return true;
	}

	@Transactional(readOnly = false)
	public boolean removeUserFromRoom(User user, Room room) {
		if(room == null)
			return false;
		if(user == null)
			return false;
		//have premition?

		room.removeUser(user);
		roomRepo.save(room);
		return true;
	}
	@Transactional
	public ArrayList<Phrase> getPhrases(){
		return phrasesRepo.findAll(); 

	}


	@Transactional
	public List<RoomModelSimple> getRoomsModelByChatUser(ChatUser currentUser) {
		return getRoomsByChatUserAndList(currentUser, null);				
	}

	@Transactional
	public List<RoomModelSimple> getRoomsModelByChatUserAndRoomList(ChatUser currentUser) {
		return getRoomsByChatUserAndList(currentUser, null);				
	}

	@Transactional
	public List<RoomModelSimple> getRoomsByChatUserAndList(ChatUser currentUser, ArrayList<Room> sourseRooms) {
		System.out.println("<<<<<<<<<<<<<<<<<<<<<<  " + new Date());
		System.out.println("currentUser:"+currentUser.getId());
		//Map<Long, String>  rooms_map = convertToNameList(room_array);		
		List<RoomModelSimple> result = new ArrayList <RoomModelSimple> ();

		List<ChatUserLastRoomDate> rooms_lastd = null;
		if(sourseRooms == null)
			rooms_lastd = chatLastRoomDateService.getUserLastRoomDates(currentUser);
		else
			rooms_lastd = chatLastRoomDateService.getUserLastRoomDatesInList(currentUser, sourseRooms);

		//Set<UserMessage> messages =  userMessageService.getMessagesByNotUser(currentUser);
		for (int i = 0; i < rooms_lastd.size() ; i++)
		{
			ChatUserLastRoomDate entry = rooms_lastd.get(i);
			Date date = entry.getLastLogout();
			int messages_cnt =  userMessageService.getMessagesCountByRoomDateNotUser(entry.getRoom(), date, entry.getChatUser()).intValue();
			
			if (entry.getLastRoom()==null /*|| entry.getLastRoom().getType() == Room.RoomType.CONSULTATION*/) 
				continue;
			RoomModelSimple sb = new RoomModelSimple(messages_cnt , date.toString(),
					entry.getLastRoom(),userMessageService.getLastUserMessageByRoom(entry.getLastRoom()));
			result.add(sb);
		}
		System.out.println(">>>>>>>>>>>>>  " + new Date());
		return result;				
	}


}


