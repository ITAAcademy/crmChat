package com.intita.wschat.services;

import java.util.ArrayList;

import javax.annotation.PostConstruct;
import javax.persistence.EntityNotFoundException;

import org.apache.commons.collections4.IteratorUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.intita.wschat.models.ChatTenant;
import com.intita.wschat.repositories.ChatTenantRepository;

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
		Page<ChatTenant> chatTenantPage = null;
		try{
			chatTenantPage = chatTenantRepo.findAll(new PageRequest(page-1, pageSize));
		}
		catch(EntityNotFoundException e){
			log.info(e.getMessage());
			return new PageImpl<ChatTenant>(new ArrayList<ChatTenant>());
		}
		return chatTenantPage; // spring рахує сторінки з нуля	}
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
		ChatTenant chatTenant =null;
	
		try{
		chatTenant = chatTenantRepo.findOne(id);
		}
		catch(EntityNotFoundException e){
			log.info("tenant Id not found:"+id);
			return null;
		}
		return chatTenant;
	}

	@Transactional
	public void updateChatTenantInfo(ChatTenant u){
		if (u==null){
			log.info("ChatTenant is null");
			return;
		}
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
