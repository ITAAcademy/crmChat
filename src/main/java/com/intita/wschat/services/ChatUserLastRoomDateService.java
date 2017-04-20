package com.intita.wschat.services;


import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.Month;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.collections4.IteratorUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.intita.wschat.models.ChatUser;
import com.intita.wschat.models.ChatUserLastRoomDate;
import com.intita.wschat.models.Room;
import com.intita.wschat.models.User;
import com.intita.wschat.repositories.ChatUserLastRoomDateRepository;

@Service
public class ChatUserLastRoomDateService {

	@Autowired
	private ChatUserLastRoomDateRepository chatUserLastRoomDateRepo;

	@Transactional
	public Page<ChatUserLastRoomDate> getUserLastRoomDatPages(int page, int pageSize){
		return chatUserLastRoomDateRepo.findAll(new PageRequest(page-1, pageSize)); // spring count pages from zero	}
	}

	@Transactional
	public List<ChatUserLastRoomDate> getLastRoomDates(){
		return chatUserLastRoomDateRepo.findAll();
	}

	@Transactional
	public List<ChatUserLastRoomDate> getUserLastRoomDates(ChatUser user){
		return chatUserLastRoomDateRepo.findByChatUser(user);
	}
	@Transactional
	public List<ChatUserLastRoomDate> getUserLastRoomDates(ChatUser user,Pageable pageable){
		if(pageable==null)
			return chatUserLastRoomDateRepo.findByChatUser(user);
		else
			return chatUserLastRoomDateRepo.findByChatUser(user,pageable);
	}

	@Transactional
	public List<ChatUserLastRoomDate> getRoomLastRoomDates(Room room){
		return chatUserLastRoomDateRepo.findByRoom(room);
	}


	@Transactional
	public List<ChatUserLastRoomDate> getUserLastRoomDatesInList(ChatUser user, ArrayList<Room> rooms){
		return chatUserLastRoomDateRepo.findByChatUserAndRoomIn(user, rooms);

	}

	@Transactional
	public ChatUserLastRoomDate getUserLastRoomDate(Room room, ChatUser chatUser) {
		ChatUserLastRoomDate obj = chatUserLastRoomDateRepo.findFirstByRoomAndChatUser(room, chatUser);
		if (obj == null)
		{
			obj = new  ChatUserLastRoomDate( new Date(), room);
			obj.setChatUser(chatUser);
			chatUserLastRoomDateRepo.save(obj);
		}
		return obj;
	}

	@Transactional
	public void updateUserLastRoomDateInfo(ChatUserLastRoomDate u){
		chatUserLastRoomDateRepo.save(u);
	}

	@Transactional
	public ChatUserLastRoomDate addUserLastRoomDateInfo(ChatUser user, Room room){
		ChatUserLastRoomDate date = chatUserLastRoomDateRepo.findFirstByRoomAndChatUser(room, user);
		if(date == null)
		{
			date = new ChatUserLastRoomDate(new Date(), room);//@OOO@
			date.setChatUser(user);
			updateUserLastRoomDateInfo(date);
		}
		return date;
	}

	@Transactional
	public boolean removeUserLastRoomDate(List<ChatUserLastRoomDate> list){
		chatUserLastRoomDateRepo.delete(list);
		return true;
	}
	@Transactional
	public boolean removeUserLastRoomDate(ChatUser user, Room room){
		ChatUserLastRoomDate last = chatUserLastRoomDateRepo.findFirstByRoomAndChatUser(room, user);
		if(last == null)
			return false;
		chatUserLastRoomDateRepo.delete(last.getId());
		return true;
	}
	@Transactional
	public ChatUserLastRoomDate findByRoomAndChatUserNotAndLast_logoutAfer(Room room, ChatUser user, Date after){
		return chatUserLastRoomDateRepo.findFirstByRoomAndChatUserNotAndLastLogoutAfter(room, user, after);
	}

	@Transactional
	public ChatUserLastRoomDate getLastNotUserActivity(Room room, ChatUser user){
		return chatUserLastRoomDateRepo.findFirstByRoomAndChatUserNotOrderByLastLogout(room, user);
	}


}
