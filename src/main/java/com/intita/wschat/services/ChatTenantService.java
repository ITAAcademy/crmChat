package com.intita.wschat.services;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Random;

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

import com.intita.wschat.event.ParticipantRepository;
import com.intita.wschat.models.ChatTenant;
import com.intita.wschat.repositories.ChatTenantRepository;

@Service
public class ChatTenantService {
	@Autowired private ParticipantRepository participantRepository;

	private final static Logger log = LoggerFactory.getLogger(ChatTenantService.class);
	@Autowired
	private ChatTenantRepository chatTenantRepo;


	private List<Long> tenantsBusy = new ArrayList<Long>();	 

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
		return chatTenantPage; 
	}

	@Transactional
	public ArrayList<ChatTenant> getTenants(){
		Iterable<ChatTenant> chatTenantsIterable = null;
		try{
			chatTenantsIterable = chatTenantRepo.findAllByEndDateAfterOrEndDateIsNull(new Date());
		}
		catch(EntityNotFoundException e){
			return new ArrayList<ChatTenant>();
		}
		ArrayList<ChatTenant> tenantsList = (ArrayList<ChatTenant>) IteratorUtils.toList(chatTenantsIterable.iterator());
		return tenantsList;
	}	


	public ChatTenant getFreeTenant() {
		List<ChatTenant> tenants = getTenants();
		for (ChatTenant tenant : tenants)
		{
			Long id = tenant.getId();
			if (isTenantBusy(id) == false)
			{
				String chatUserId = tenant.getChatUser().getPrincipal().getName();
				if (participantRepository.isOnline("" + chatUserId)) //989
					return tenant;
			}
		}
		return null;
	}


	public void setTenantBusy(Long id) {
		if ( !isTenantBusy(id))
			tenantsBusy.add(id);
	}

	public void setTenantBusy(ChatTenant tenant) {
		if ( !isTenantBusy(tenant))
			tenantsBusy.add(tenant.getId());
	}

	public void setTenantFree(Long id) {
		for (int i = 0; i < tenantsBusy.size(); i++)
		{
			if (tenantsBusy.get(i) == id)
				tenantsBusy.remove(i);
		}
	}

	boolean isTenantBusy(Long id) {
		for (Long tenant_id : tenantsBusy)
		{
			if (tenant_id == id )
				return true;
		}
		return false;
	}

	boolean isTenantBusy(ChatTenant tenant) {
		return isTenantBusy(tenant.getId());
	}

	@Transactional
	public ChatTenant getRandomTenant(){
		ArrayList<ChatTenant> countTenant = getTenants();
		if(countTenant.isEmpty())
		{
			return null;
		}
		int k = new Random().nextInt(countTenant.size());

		ChatTenant t_user = countTenant.get(k);//choose method
		return t_user;
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
