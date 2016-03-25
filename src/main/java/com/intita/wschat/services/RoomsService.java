package com.intita.wschat.services;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.intita.wschat.models.ChatUser;
import com.intita.wschat.models.ChatUserLastRoomDate;
import com.intita.wschat.models.Room;
import com.intita.wschat.models.User;
import com.intita.wschat.models.UserMessage;
import com.intita.wschat.repositories.RoomRepository;

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
	public List<StringIntDate> getRoomsByChatUser(ChatUser currentUser) {
		System.out.println("<<<<<<<<<<<<<<<<<<<<<<  " + new Date());
		System.out.println("currentUser:"+currentUser.getId());
		//Map<Long, String>  rooms_map = convertToNameList(room_array);		
		List<StringIntDate> result = new ArrayList <StringIntDate> ();

		List<ChatUserLastRoomDate> rooms_lastd = chatLastRoomDateService.getUserLastRoomDates(currentUser);	

		Set<UserMessage> messages =  userMessageService.getMessagesByNotUser(currentUser);

		for (int i = 0; i < rooms_lastd.size() ; i++)
		{
			ChatUserLastRoomDate entry = rooms_lastd.get(i);
			Date date = entry.getLastLogout();
			int messages_cnt = 0;// =  userMessageService.getMessagesByRoomDateNotUser(entry, date, currentUser).size();
			for (UserMessage msg : messages)
			{
				Date m_data = msg.getDate();
				//	System.out.println( msg.getRoom().getId() + "	" + entry.getRoom().getId());
				if (m_data != null && msg.getRoom() != null)
					if (m_data.after(date) == true && msg.getRoom().getId() == 	entry.getRoom().getId())
					{
						messages_cnt += 1;
					}
			}
			if (entry.getLastRoom()==null || entry.getLastRoom().getType() == Room.RoomType.CONSULTATION) continue;
			StringIntDate sb = new StringIntDate(entry.getLastRoom().getName(), messages_cnt , date.toString(),entry.getLastRoom());
			result.add(sb);
		}
		System.out.println(">>>>>>>>>>>>>  " + new Date());
		return result;				
	}


	static public class StringIntDate {
		public Long getRoomAuthorId() {
			return roomAuthorId;
		}

		public void setRoomAuthorId(Long roomAuthorId) {
			this.roomAuthorId = roomAuthorId;
		}

		public Long getRoomId() {
			return roomId;
		}

		public void setRoomId(Long roomId) {
			this.roomId = roomId;
		}

		public String string;
		public Integer nums;
		public String date;
		public Long roomId;
		public Long roomAuthorId;
		public boolean active;

		public boolean isActive() {
			return active;
		}

		public void setActive(boolean active) {
			this.active = active;
		}

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

		public StringIntDate(String string, Integer nums, String date,Room room) {
			this.string = string;
			this.nums = nums;
			this.date = date;
			this.roomId=room.getId();
			this.roomAuthorId=room.getAuthor().getId();
			this.active = room.isActive();
		}
	}
}


