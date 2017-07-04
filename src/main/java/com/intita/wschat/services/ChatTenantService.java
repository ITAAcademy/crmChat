package com.intita.wschat.services;

import java.security.Principal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Random;
import java.util.Set;

import javax.annotation.PostConstruct;
import javax.persistence.EntityNotFoundException;

import com.intita.wschat.config.ChatPrincipal;
import org.apache.commons.collections4.IteratorUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.intita.wschat.event.ParticipantRepository;
import com.intita.wschat.models.ChatTenant;
import com.intita.wschat.models.ChatUser;
import com.intita.wschat.models.Room;
import com.intita.wschat.repositories.ChatTenantRepository;

@Service
public class ChatTenantService {
	@Autowired private ParticipantRepository participantRepository;
	@Autowired private ChatUsersService chatUsersService;
	@Autowired private ChatTenantRepository chatTenantRepo;
	@PostConstruct
	@Transactional
	public void createAdminUser() {
		System.out.println("admin user created");
		//register("user", "user", "user");
	}

	private final static Logger log = LoggerFactory.getLogger(ChatTenantService.class);

	private int lastAskedTenantCnt = 0;
	private List<Long> tenantsBusy =  new ArrayList<Long>();	 	 

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
		ArrayList<ChatTenant> chatTenantsIterable = null;
		try{
			chatTenantsIterable = chatTenantRepo.findAllByEndDateAfterOrEndDateIsNull(new Date());
		}
		catch(EntityNotFoundException e){
			return new ArrayList<ChatTenant>();
		}
		ArrayList<ChatTenant> tenantsList = (ArrayList<ChatTenant>) IteratorUtils.toList(chatTenantsIterable.iterator());
		return tenantsList;
	}	

	@Transactional
	public List<ChatTenant> getAllTenants(){
		return chatTenantRepo.findAll();
	}
	@Transactional
	public Set<ChatTenant> getUniqueTenants(){
		return chatTenantRepo.findDistinctByEndDateAfterOrEndDateIsNull(new Date());
	}
	
	@Transactional
	public List<ChatTenant> getAllTenantsWhereChatUserIdInList(ArrayList<Long> usersIds){
		return chatTenantRepo.findWhereChatUserIdInList(usersIds);
	}
	
	public ChatTenant getFreeTenantNotFromRoom(Room room) {
		Set<ChatUser> users = room.getUsers();
		List<Long> usersId = new ArrayList<Long>();
		
		for (ChatUser user : users)
			usersId.add(user.getId());
		
		
		List<ChatTenant> tenants = getTenants();
		
		List<ChatTenant> tenantsNotFromRoom = new ArrayList<ChatTenant>();
		
		for( ChatTenant tenant : tenants) {
			Long id = tenant.getChatUser().getId();
			boolean equal = false;
			for (Long userId : usersId)
				if (id == userId) {
					equal = true;
					break;
				}
			if (equal == false)
				tenantsNotFromRoom.add(tenant);
		}
		return getFreeTenant(tenantsNotFromRoom);		
	}
	
	public ChatTenant getFreeTenant() {
		List<ChatTenant> tenants = getTenants();
		return getFreeTenant(tenants);
	}


	public ChatTenant getFreeTenant(List<ChatTenant> tenants) {			
		

		int i_0; //  = lastAskedTenantCnt + 1;
		int i_1;  // = lastAskedTenantCnt;

		int tenantsLastIndex = tenants.size() - 1;

		boolean secondCircle = false;		

		if (lastAskedTenantCnt == tenantsLastIndex) {
			i_0 = 0;
			i_1 = tenantsLastIndex;
			secondCircle = true;
		}
		else {
			i_0  = lastAskedTenantCnt + 1;
			i_1  = lastAskedTenantCnt;
		}		

		int i = i_0;

		List<Long> isAskedBefore = new  ArrayList<>();

		boolean zaklepka = true;
		while (zaklepka) 				
		{
			ChatTenant tenant =  tenants.get(i);	

			Long chatUserId = tenant.getChatUser().getId();

			boolean askedBefore = false;			

			for (Long a_id : isAskedBefore) {
				if (a_id == chatUserId) {
					askedBefore = true;
					break;
				}
			}
			isAskedBefore.add(chatUserId);

			if (!askedBefore) {
				//Long id = tenant.getId(); //999
				if (isTenantBusy(chatUserId) == false)
				{
					if (participantRepository.isOnline(chatUserId)) //989
					{
						lastAskedTenantCnt = i;
						return tenant;
					}					
				}		
			}

			i++;
			if ( !secondCircle ) {
				if (i > tenantsLastIndex) {
					i = 0;
					secondCircle = true;
				}
			}
			else {
				if ( i > i_1 )
					return null;
			}
		}
		return null;
	}

	public void setTenantBusy(ChatUser chatUser) {
		if (chatUser != null && !isTenantBusy(chatUser.getId())) {
			tenantsBusy.add(chatUser.getId());
		}
	}
	
	public void setTenantBusy(Long chatUserid) {
		if ( !isTenantBusy(chatUserid)) {
			tenantsBusy.add(chatUserid);
		}
	}

	public boolean setTenantBusy(ChatTenant tenant) {
		if (tenant == null)
			return false;
		setTenantBusy(tenant.getChatUser().getId());
		return true;
	}

	public boolean setTenantFree(Long chatUserid) {
		boolean res = tenantsBusy.remove(chatUserid);
		return res;
	}

	public Long getChatTenantId(Long chatUserId) {
		Long nol = (long) 0;

		ChatUser chatUser = chatUsersService.getChatUser(chatUserId);
		if (chatUser == null)
			return nol;

		ChatTenant tenant = chatTenantRepo.findByChatUser(chatUser);

		if (tenant == null)
			return  nol;
		return tenant.getId();
	}

	public void setTenantFree(Authentication auth) {
		ChatPrincipal chatPrincipal = (ChatPrincipal)auth.getPrincipal();
		Long chatUserId = chatPrincipal.getChatUser().getId();
		setTenantFree(chatUserId);
	}

	public void setTenantBusy(Authentication auth) {
		ChatPrincipal chatPrincipal = (ChatPrincipal)auth.getPrincipal();
		Long chatUserId = chatPrincipal.getChatUser().getId();
		setTenantBusy(chatUserId);
	}

	public boolean isTenantBusy(Long chatUserid) {
		for (Long tenant_id : tenantsBusy)
		{
			if (tenant_id.longValue() == chatUserid.longValue() )
				return true;
		}
		return false;
	}
	public boolean isTenantAvailable(Long chatUserid){
		if (!participantRepository.isOnline(chatUserid))return false;
		if (isTenantBusy(chatUserid)) return false;
		return true;
	}
	
	public boolean isTenant(Long chatUserId) {
		ArrayList<ChatTenant> countTenant = getTenants();
		for (ChatTenant tenant : countTenant)
			if (tenant.getChatUser().getId().equals(chatUserId))
				return true;
		return false;
	}
	
	public boolean isTenant(ChatTenant chatTenant) {
		return isTenant(chatTenant.getChatUser().getId());
	}
	
	public boolean isTenantBusy(ChatTenant tenant) {
		return isTenantBusy(tenant.getChatUser().getId());
	}

	public List<Long> getTenantsBusy() {
		return tenantsBusy;
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
	public ChatTenant getChatTenant(ChatUser  chatUser){
		return chatTenantRepo.findByChatUser(chatUser);
	}

	@Transactional
	public Long getChatTenantId(ChatUser  chatUser){
		return chatTenantRepo.findIdByChatUser(chatUser);
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
