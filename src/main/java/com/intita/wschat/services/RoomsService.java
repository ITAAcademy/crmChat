package com.intita.wschat.services;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.authentication.encoding.ShaPasswordEncoder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.intita.wschat.models.ChatUser;
import com.intita.wschat.models.ChatUserLastRoomDate;
import com.intita.wschat.models.Room;
import com.intita.wschat.models.User;
import com.intita.wschat.models.UserMessage;
import com.intita.wschat.models.User.Permissions;
import com.intita.wschat.repositories.RoomRepository;
import com.intita.wschat.repositories.UserRepository;
import com.intita.wschat.web.RoomController;

import org.apache.commons.collections4.IteratorUtils;

@Service
public class RoomsService {
	
	@Autowired
	private RoomRepository roomRepo;
	@Autowired private UsersService userService;
	@Autowired private ChatUsersService chatUserService;
	@Autowired private ChatUserLastRoomDateService chatLastRoomDateService;
	@Autowired private UserMessageService userMessageService;
	
	@PostConstruct
	@Transactional
	public void createDefRoom() {
		
	}
	
	@Transactional
	   public Page<Room> getRooms(int page, int pageSize){
			return roomRepo.findAll(new PageRequest(page-1, pageSize)); // spring рахує сторінки з нуля
			
	   }
	
	@Transactional
	public List<Room> getRooms(){
		return (List<Room>) roomRepo.findAll(); // spring рахує сторінки з нуля
	}
	
	@Transactional
	public Room getRoom(Long id){
		return roomRepo.findOne(id);
	}
	
	@Transactional
	public Room getRoom(String name) {
		return roomRepo.findByName(name);
	}
	public ArrayList<Room> getRoomByAuthor(String author) {
		
		return roomRepo.findByAuthor(userService.getUser(author));
	}
	
	@Transactional(readOnly = false)
	public Room register(String name, ChatUser author) {
		Room r = new Room();
		r.setAuthor(author);
		r.setName(name);
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
	
	public Map<Long, StringIntDate> getRoomsByChatUser(ChatUser currentUser) {
		System.out.println("<<<<<<<<<<<<<<<<<<<<<<  " + new Date());

		//Map<Long, String>  rooms_map = convertToNameList(room_array);		
		Map<Long, StringIntDate> result = new HashMap <Long, StringIntDate> ();

		List<ChatUserLastRoomDate> rooms_lastd = chatLastRoomDateService.getUserLastRoomDates(currentUser);	
		
		List<UserMessage> messages =  userMessageService.getMessagesByNotUser(currentUser);

		for (int i = 0; i < rooms_lastd.size() ; i++)
		{
			ChatUserLastRoomDate entry = rooms_lastd.get(i);
			Date date = entry.getLastLogout();
			int messages_cnt = 0;// =  userMessageService.getMessagesByRoomDateNotUser(entry, date, currentUser).size();
			for (UserMessage msg : messages)
			{
				Date m_data = msg.getDate();
				System.out.println( msg.getRoom().getId() + "	" + entry.getRoom().getId());
				if (m_data != null)
					if (m_data.after(date) == true && msg.getRoom().getId() == entry.getRoom().getId())
					{
						messages_cnt += 1;
					}
			}

			StringIntDate sb = new StringIntDate(entry.getLastRoom().getName(), messages_cnt , date.toString());
			result.put(entry.getLastRoom().getId() ,sb);
		}
		System.out.println(">>>>>>>>>>>>>  " + new Date());
		return result;				
	}
	

	static public class StringIntDate {
		public String string;
		public Integer nums;
		public String date;

		public String getDate() {
			return date;
		}

		public void setDate(String date) {
			this.date = date;
		}

		public StringIntDate() {
			string = "";
			nums = 0;
			date = new Date().toString();
		}

		public StringIntDate(String string, Integer nums, String date) {
			this.string = string;
			this.nums = nums;
			this.date = date;
		}
	}
}


