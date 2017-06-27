package com.intita.wschat.event;

import java.security.Principal;

import com.intita.wschat.config.ChatPrincipal;
import com.intita.wschat.services.common.UsersOperationsService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.AutoConfigureOrder;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.Authentication;
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
	private UsersOperationsService usersOperationsService;

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
		Authentication auth = (Authentication)headers.getUser();
		if(auth == null)
			return;
		ChatPrincipal chatPrincipal = (ChatPrincipal)auth.getPrincipal();
		Long chatId = chatPrincipal.getChatUser().getId();
		if (chatId==null){
			log.warn("Cannot parse chatId");
		}

		LoginEvent loginEvent = new LoginEvent(chatId, "test");//,participantRepository.isOnline(chatId));
		messagingTemplate.convertAndSend(loginDestination, loginEvent);

		// We store the session as we need to be idempotent in the disconnect event processing
		//
		participantRepository.addParticipantPresenceByConnections(chatId);
		ChatUser user = chatPrincipal.getChatUser();
		if (chatTenantService.isTenant(user.getId())){
			//log.info(String.format("propagation of new tenant '%s' for trainers by ws and lp...", chatId));
			messagingTemplate.convertAndSend("/topic/chat.tenants.add",new LoginEvent(user));
			usersOperationsService.tryAddTenantInListToTrainerLP(user);
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
		Authentication auth = (Authentication)headers.getUser();
		if(auth != null)
		{

			ChatPrincipal chatPrincipal = (ChatPrincipal)auth.getPrincipal();
			Long chatId = chatPrincipal.getChatUser().getId();
			if (chatId==null)return;

			ChatUser user = chatPrincipal.getChatUser();
			participantRepository.invalidateParticipantPresence(chatId,true);
			if(!participantRepository.isOnline(chatId)){
				messagingTemplate.convertAndSend(logoutDestination, new LogoutEvent(chatId.toString()));
				//remove user from tenant list if tenant
				if (chatTenantService.isTenant(user.getId()))
					messagingTemplate.convertAndSend("/topic/chat.tenants.remove",new LoginEvent(user));
				usersOperationsService.propagateRemovingTenantFromListToTrainer(user);
				usersOperationsService.tryRemoveChatUserRequiredTrainer(user);
			}
		}
	}

	public void setLoginDestination(String loginDestination) {
		this.loginDestination = loginDestination;
	}

	public void setLogoutDestination(String logoutDestination) {
		this.logoutDestination = logoutDestination;
	}
}
