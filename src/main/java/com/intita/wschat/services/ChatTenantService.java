package com.intita.wschat.services;

import java.util.ArrayList;

import javax.annotation.PostConstruct;
import javax.persistence.EntityNotFoundException;

import org.apache.commons.collections4.IteratorUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.intita.wschat.models.ChatTenant;
import com.intita.wschat.repositories.ChatTenantRepository;
import com.intita.wschat.web.FileController;

@Service
public class ChatTenantService {
	private final static Logger log = LoggerFactory.getLogger(ChatTenantService.class);
	@Autowired
	private ChatTenantRepository chatTenantRepo;

	@PostConstruct
	@Transactional
	public void createAdminUser() {
		System.out.println("admin user created");
		//register("user", "user", "user");

	}

	@Transactional
	public Page<ChatTenant> getTenantsFromPages(int page, int pageSize){
		return chatTenantRepo.findAll(new PageRequest(page-1, pageSize)); // spring рахує сторінки з нуля	}
	}
	
	@Transactional
	public ArrayList<ChatTenant> getTenants(){
		Iterable<ChatTenant> chatTenantsIterable = null;
		try{
			chatTenantsIterable = chatTenantRepo.findAll();
		}
		catch(EntityNotFoundException e){
			return new ArrayList<ChatTenant>();
		}
		ArrayList<ChatTenant> tenantsList = (ArrayList<ChatTenant>) IteratorUtils.toList(chatTenantsIterable.iterator()); // spring рахує сторінки з нуля
		return tenantsList;
	}
	@Transactional
	public ChatTenant getChatTenant(Long id){
		return chatTenantRepo.findOne(id);
	}

	@Transactional
	public void updateChatTenantInfo(ChatTenant u){
		if (u!=null)
		chatTenantRepo.save(u);
	}

	@Transactional
	public void removeTenant(Long id){
		chatTenantRepo.delete(id);
	}
	@Transactional
	public long getCount(){
		return chatTenantRepo.count();
	}
}
