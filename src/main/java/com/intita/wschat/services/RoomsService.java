package com.intita.wschat.services;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.PostConstruct;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.messaging.MessagingException;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.intita.wschat.event.LoginEvent;
import com.intita.wschat.event.ParticipantRepository;
import com.intita.wschat.models.ChatUser;
import com.intita.wschat.models.ChatUserLastRoomDate;
import com.intita.wschat.models.Phrase;
import com.intita.wschat.models.PrivateRoomInfo;
import com.intita.wschat.models.Room;
import com.intita.wschat.models.Room.RoomType;
import com.intita.wschat.models.RoomModelSimple;
import com.intita.wschat.models.User;
import com.intita.wschat.models.UserMessage;
import com.intita.wschat.repositories.ChatPhrasesRepository;
import com.intita.wschat.repositories.PrivateRoomInfoRepository;
import com.intita.wschat.repositories.RoomRepository;
import com.intita.wschat.web.ChatController;

@Service
public class RoomsService {

	@Autowired private RoomRepository roomRepo;
	@Autowired private PrivateRoomInfoRepository privateRoomInfoRepo;
	@Autowired private ChatPhrasesRepository phrasesRepo;
	@Autowired private UsersService userService;
	@Autowired private ChatUsersService chatUserService;
	@Autowired private ChatUserLastRoomDateService chatLastRoomDateService;
	@Autowired private UserMessageService userMessageService;
	@Autowired private SimpMessagingTemplate simpMessagingTemplate;
	@Autowired private ParticipantRepository participantRepository;

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
	public PrivateRoomInfo getPrivateRoomInfo(ChatUser author, ChatUser privateUser) {
		return privateRoomInfoRepo.getByUsers(author, privateUser);
	}
	@Transactional
	public PrivateRoomInfo getPrivateRoomInfo(Room room) {
		return privateRoomInfoRepo.findByRoom(room);
	}

	@Transactional
	public Room getPrivateRoom(ChatUser author, ChatUser privateUser) {
		PrivateRoomInfo info = getPrivateRoomInfo(author, privateUser); 
		if(info == null)
			return null;
		return info.getRoom();
	}

	@Transactional
	public ArrayList<PrivateRoomInfo> getPrivateRoomsInfoByUser(ChatUser user) {
		return privateRoomInfoRepo.getByUser(user);
	}

	@Transactional
	public ArrayList<Room> getPrivateRooms(ChatUser user) {
		return privateRoomInfoRepo.getRoomsByUser(user);
	}

	@Transactional
	public ArrayList<ChatUser> getPrivateChatUsers(ChatUser user) {
		ArrayList<ChatUser> users = new ArrayList<>();
		ArrayList<PrivateRoomInfo> infoList = getPrivateRoomsInfoByUser(user);
		for (PrivateRoomInfo privateRoomInfo : infoList) {
			if(privateRoomInfo.getFirtsUser() == user)
				users.add(privateRoomInfo.getSecondUser());
			else
				users.add(privateRoomInfo.getFirtsUser());
		}
		return users;
	}
	@Transactional
	public ArrayList<LoginEvent> getPrivateLoginEvent(ChatUser user) {
		ArrayList<LoginEvent> users = new ArrayList<>();
		ArrayList<PrivateRoomInfo> infoList = getPrivateRoomsInfoByUser(user);
		for (PrivateRoomInfo privateRoomInfo : infoList) {
			ChatUser uTemp = null;
			if(privateRoomInfo.getFirtsUser() == user)
				uTemp = privateRoomInfo.getSecondUser();
			else
				uTemp = privateRoomInfo.getFirtsUser();
			users.add(new LoginEvent(uTemp, participantRepository.isOnline(uTemp.getId().toString())));
		}
		return users;
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
		//r.addUser(author);//@BAG@
		roomRepo.save(r);
		chatLastRoomDateService.addUserLastRoomDateInfo(author, r);
		return r;
	}
	@Transactional(readOnly = false)
	public Room register(String name, ChatUser author, short type) {
		if(type == RoomType.PRIVATE)
		{
			throw new IllegalArgumentException("use register private function for Room with type ==" + RoomType.PRIVATE);
		}
		Room r = new Room();
		r.setAuthor(author);
		r.setName(name);
		r.setType(type);
		r = roomRepo.save(r);
		chatLastRoomDateService.addUserLastRoomDateInfo(author, r);
		return r;
	}

	@Transactional(readOnly = false)
	public Room registerPrivate(ChatUser first, ChatUser second) {
		Room r = new Room();
		r.setAuthor(first);
		r.setName(first.getNickName() + "_" + second.getNickName());
		r.setType(RoomType.PRIVATE);
		r = roomRepo.save(r);
		chatLastRoomDateService.addUserLastRoomDateInfo(first, r);
		if (first.getId()!= second.getId())
			addUserToRoom(second, r);
		PrivateRoomInfo info = privateRoomInfoRepo.save(new PrivateRoomInfo(r, first, second));
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

	public boolean addUserToRoom(Long id, User user) {
		Room room = roomRepo.findOne(id);
		addUserToRoom(chatUserService.getChatUser(id), room);		
		return true;
	}

	public void setAuthor(ChatUser user, Room room)
	{
		room.setAuthor(user);
		chatLastRoomDateService.addUserLastRoomDateInfo(user, room);	
	}


	@Transactional(readOnly = false)
	public boolean update(Room room){
		roomRepo.save(room);
		Set<ChatUser> users = new HashSet<>(room.getUsers());
		users.add(room.getAuthor());
		for (ChatUser chatUser : users) {
			chatController.updateRoomByUser(chatUser, room);
		}
		/*Map<String, Object> sendedMap = new HashMap<>();
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
		chatController.addFieldToInfoMap("updateRoom", sendedMap);*/
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
	public boolean removeUserFromRoom(ChatUser user, Room room) {
		if(room == null)
			return false;
		if(user == null)
			return false;
		//have premition?

		room.removeUser(user);
		roomRepo.save(room);
		return true;
	}
	//@Transactional(readOnly = true)
	public ArrayList<Phrase> getPhrases(){
		return phrasesRepo.findAll(); 
	}

	//@Transactional(readOnly = true)
	public ArrayList<Phrase> getEvaluatedPhrases(ChatUser currentUser){

		String[] searchList = {"$date","$username"};
		String currentDate = new SimpleDateFormat("dd-MM-yyyy").format(new Date());
		User intitaUser = null;
		if (currentUser!=null) 
			intitaUser = currentUser.getIntitaUser();
		String currentUserName = "";

		if(intitaUser!=null) 
			currentUserName = intitaUser.getFullName();
		else 
			currentUserName = currentUser.getNickName();

		String[] replacementList = {currentDate,currentUserName};
		ArrayList<Phrase> phrases = getPhrases();
		for (Phrase phrase : phrases){
			String phraseText = phrase.getText();
			phraseText = StringUtils.replaceEach(phraseText, searchList, replacementList);
			phrase.setText(phraseText);
		}
		return phrases;	
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


