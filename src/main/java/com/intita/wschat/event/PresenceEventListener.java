package com.intita.wschat.event;

import java.security.Principal;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.messaging.SessionConnectEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

import com.intita.wschat.models.ChatUser;
import com.intita.wschat.models.User;
import com.intita.wschat.services.ChatTenantService;
import com.intita.wschat.services.ChatUsersService;
import com.intita.wschat.web.ChatController;

/**
 * Listener to track user presence. 
 * Sends notifications to the login destination when a connected event is received
 * and notifications to the logout destination when a disconnect event is received
 * 
 * @author Nicolas
 */
public class PresenceEventListener {
	
	@Autowired
	private ParticipantRepository participantRepository;
	@Autowired 
	private ChatUsersService chatUsersService;
	@Autowired
	private ChatTenantService chatTenantService;
	@Autowired
	private ChatController chatController;
	
	private SimpMessagingTemplate messagingTemplate;
	
	private String loginDestination;
	
	private String logoutDestination;
	
	public PresenceEventListener(SimpMessagingTemplate messagingTemplate, ParticipantRepository participantRepository) {
		this.messagingTemplate = messagingTemplate;
	}
		
	private final static Logger log = LoggerFactory.getLogger(PresenceEventListener.class);
/*	void afterConnectionClosed(WebSocketSession session,
            CloseStatus closeStatus)
     throws Exception{
		ChatUser user =chatUsersService.getChatUser(User.getCurrentUserId());
		String chatId = user.getId().toString();
		participantRepository.removeParticipant(chatId);
		if(!participantRepository.isOnline(chatId)){
		messagingTemplate.convertAndSend(logoutDestination, new LogoutEvent(chatId));
		//remove user from tenant list if tenant
		if (chatTenantService.isTenant(user.getId()))
		messagingTemplate.convertAndSend("/topic/chat.tenants.remove",new LoginEvent(user,user.getIntitaUser(),false));
	}
	}*/
	@EventListener
	private void handleSessionConnected(SessionConnectEvent event) {
		SimpMessageHeaderAccessor headers = SimpMessageHeaderAccessor.wrap(event.getMessage());
		String chatIdStr = headers.getUser().getName();
		Long chatId = Long.parseLong(chatIdStr);
		if (chatId==null){
			log.warn("Cannot parse chatId");
		}

		LoginEvent loginEvent = new LoginEvent(chatId, "test");//,participantRepository.isOnline(chatId));
		messagingTemplate.convertAndSend(loginDestination, loginEvent);
		
		// We store the session as we need to be idempotent in the disconnect event processing
		Principal principal = headers.getUser();
		participantRepository.addParticipantPresenceByConnections(chatId);
		ChatUser user = chatUsersService.getChatUser(principal);
		if (chatTenantService.isTenant(user.getId())){
			//log.info(String.format("propagation of new tenant '%s' for trainers by ws and lp...", chatId));
			messagingTemplate.convertAndSend("/topic/chat.tenants.add",new LoginEvent(user, true));
			chatController.tryAddTenantInListToTrainerLP(user);
		}
	}
	
	@EventListener
	private void handleSessionDisconnect(SessionDisconnectEvent event) {
		
		/*Optional.ofNullable(participantRepository.getParticipant(event.getSessionId()))
				.ifPresent(login -> {
					messagingTemplate.convertAndSend(logoutDestination, new LogoutEvent(login.getUsername()));
					participantRepository.removeParticipant(event.getSessionId());
				});*/
		SimpMessageHeaderAccessor headers = SimpMessageHeaderAccessor.wrap(event.getMessage());
		Principal principal = headers.getUser();
		String chatIdStr = principal.getName();
		Long chatId = Long.parseLong(chatIdStr);
		ChatUser user = chatUsersService.getChatUser(principal);
		participantRepository.invalidateParticipantPresence(chatId,true);
		if(!participantRepository.isOnline(chatId)){
		messagingTemplate.convertAndSend(logoutDestination, new LogoutEvent(chatIdStr));
		//remove user from tenant list if tenant
		if (chatTenantService.isTenant(user.getId()))
		messagingTemplate.convertAndSend("/topic/chat.tenants.remove",new LoginEvent(user, false));
		chatController.propagateRemovingTenantFromListToTrainer(user);
		chatController.tryRemoveChatUserRequiredTrainer(user);
		}
		
				
	}

	public void setLoginDestination(String loginDestination) {
		this.loginDestination = loginDestination;
	}

	public void setLogoutDestination(String logoutDestination) {
		this.logoutDestination = logoutDestination;
	}
}
