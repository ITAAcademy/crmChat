package com.intita.wschat.event;

import java.util.Date;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import com.intita.wschat.services.common.UsersOperationsService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import com.intita.wschat.domain.interfaces.IPresentOnForum;
import com.intita.wschat.domain.interfaces.LongpollPresence;
import com.intita.wschat.domain.interfaces.WsPresence;
import com.intita.wschat.models.ChatUser;
import com.intita.wschat.services.ChatUsersService;
import com.intita.wschat.web.ChatController;


/**
 * 
 * @author Nicolas
 */

@Component
public class ParticipantRepository {
	public ConcurrentHashMap<Long,IPresentOnForum> getActiveSessions() {
		return activeSessions;
	}
	public Set<Long> getActiveUsers(){
		return activeSessions.keySet();
	}
	@Autowired ChatUsersService chatUsersService;
	private final static Logger log = LoggerFactory.getLogger(ParticipantRepository.class);
	private ConcurrentHashMap<Long,IPresentOnForum> activeSessions = new ConcurrentHashMap<Long,IPresentOnForum>();
	@Autowired
	@Lazy
	UsersOperationsService usersOperationsService;
	//put null to distinguish WS from LP sessions. We need no last action time for Web Sockets
	public void addParticipantPresenceByConnections(Long chatId) {
		if(chatId==null) return;
		if (activeSessions.containsKey(chatId)){
			IPresentOnForum presence = activeSessions.get(chatId);
			presence.addConnectionsCount(1);
			activeSessions.put(chatId, presence);
			//System.out.println("presence increased to:"+(presence)+"for user with id:+"+chatId);
		}
		else{
	activeSessions.put(chatId,new WsPresence());
	//System.out.println("presence added with start value:"+1);
		}
		
	}
	public void addParticipantPresenceByLastConnectionTime(Long chatId){
		if (chatId==null)return;
		if (activeSessions.containsKey(chatId)){
			IPresentOnForum presence = activeSessions.get(chatId);
			presence.setLastPresenceTime(new Date());
			//log.info(String.format("Participant %s presence time updated ",chatId));
		}
		else{
			activeSessions.put(chatId, new LongpollPresence());
			usersOperationsService.tryAddTenantInListToTrainerLP(chatId);
			//log.info(String.format("Participant %s presence added first time ",chatId));
		}
			//System.out.println("user "+chatId+" enter chat");
	}

	public boolean isOnline(Long chatId) {
		if (chatId==null) return false;
		boolean containsKey = activeSessions.containsKey(chatId);
		boolean isPresent = containsKey ? activeSessions.get(chatId).isPresent() : false;
		//System.out.println("isOnline "+chatId+" ? "+ online);
		return isPresent;
	}
	/**
	 * Decrease presence index of user and if it <=0 remove it from online users
	 * @param chatId
	 * @return "true" if participant is removed, "false" if not removed or only presence index decreased
	 */
	public boolean invalidateParticipantPresence(Long chatId,boolean invalidateWS) {
		//log.info(String.format("Participant %s presence invalidating...",chatId));
		if (chatId == null) return false;
		if (activeSessions.containsKey(chatId)){
			IPresentOnForum userPresent = activeSessions.get(chatId);
			if (invalidateWS && userPresent.isConnectionBased())
			userPresent.decreaseConnectionsCount(1);
			if (!userPresent.isPresent())
			{
				ChatUser chatUser = chatUsersService.getChatUser(chatId);
				usersOperationsService.propagateRemovingTenantFromListToTrainer(chatUser);
				activeSessions.remove(chatId);
				//log.info(String.format("Participant %s presence removed",chatId));
				return true;
			}
			else
			{
			activeSessions.put(chatId, userPresent);
			//System.out.println("presence decreased to:"+(presenceIndex)+"for user with id:"+chatId);
			return false;
			}
		}
		return false;
	}
}
