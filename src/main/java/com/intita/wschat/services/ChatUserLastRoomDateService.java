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
		return chatUserLastRoomDateRepo.findAll(new PageRequest(page-1, pageSize)); // spring рахує сторінки з нуля	}
	}

	@Transactional
	public List<ChatUserLastRoomDate> getUserLastRoomDates(){
		List<ChatUserLastRoomDate> res =  chatUserLastRoomDateRepo.findAll();
		return res;
	}
	@Transactional
	public ChatUserLastRoomDate getUserLastRoomDate(Room room, ChatUser chatUser) {
		ChatUserLastRoomDate obj = chatUserLastRoomDateRepo.findByRoomAndChatUser(room, chatUser);
		if (obj == null)
		{
			LocalDate firstDay_2000 = LocalDate.of(2000, Month.JANUARY, 1);
			
			//Date date = DateTime.parse("2007-03-12T00:00:00.000+01:00");
			obj = new  ChatUserLastRoomDate(Date.from(firstDay_2000.atStartOfDay(ZoneId.systemDefault()).toInstant()) ,room );
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
	public void removeUserLastRoomDate(Long id){
		chatUserLastRoomDateRepo.delete(id);
	}


}
