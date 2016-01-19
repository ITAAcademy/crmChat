package com.intita.wschat.services;

import java.util.ArrayList;
import java.util.List;

import com.google.common.collect.Lists;
import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.collections4.IteratorUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.intita.wschat.models.ChatTenant;
import com.intita.wschat.models.ChatUserLastRoomDate;
import com.intita.wschat.models.User;
import com.intita.wschat.repositories.ChatTenantRepository;
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
		List<ChatUserLastRoomDate> res =  Lists.newArrayList(chatUserLastRoomDateRepo.findAll());
		return res;
	}
	@Transactional
	public ChatUserLastRoomDate getUserLastRoomDate(Long id){
		return chatUserLastRoomDateRepo.findById(id);
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
