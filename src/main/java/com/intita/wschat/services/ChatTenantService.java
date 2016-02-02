package com.intita.wschat.services;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.PostConstruct;

import org.apache.commons.collections4.IteratorUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.intita.wschat.models.ChatTenant;
import com.intita.wschat.models.User;
import com.intita.wschat.repositories.ChatTenantRepository;

@Service
public class ChatTenantService {

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
		return (ArrayList<ChatTenant>) IteratorUtils.toList(chatTenantRepo.findAll().iterator()); // spring рахує сторінки з нуля
	}
	@Transactional
	public ChatTenant getChatTenant(Long id){
		return chatTenantRepo.findOne(id);
	}

	@Transactional
	public void updateChatTenantInfo(ChatTenant u){
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
