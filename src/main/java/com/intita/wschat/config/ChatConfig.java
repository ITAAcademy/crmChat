package com.intita.wschat.config;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.springframework.boot.actuate.endpoint.MessageMappingEndpoint;
import org.springframework.boot.actuate.endpoint.WebSocketEndpoint;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.*;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.context.request.RequestContextListener;
import org.springframework.web.socket.config.WebSocketMessageBrokerStats;

import com.intita.wschat.domain.SessionProfanity;
import com.intita.wschat.event.ParticipantRepository;
import com.intita.wschat.event.PresenceEventListener;
import com.intita.wschat.util.ProfanityChecker;
/**
 * 
 * @author Nicolas Haiduchok
 */
@Configuration
@EnableAspectJAutoProxy//for TEST purposes to check functions run time
public class ChatConfig {

	public static class Destinations {
		private Destinations() {
		}

		public static final String LOGIN = "/topic/chat.login";
		public static final String LOGOUT = "/topic/chat.logout";
	}

	private static final int MAX_PROFANITY_LEVEL = 5;

	/*
	 * @Bean
	 * 
	 * @Description("Application event multicaster to process events asynchonously"
	 * ) public ApplicationEventMulticaster applicationEventMulticaster() {
	 * SimpleApplicationEventMulticaster multicaster = new
	 * SimpleApplicationEventMulticaster();
	 * multicaster.setTaskExecutor(Executors.newFixedThreadPool(10)); return
	 * multicaster; }
	 */
	@Bean
	@Description("Tracks user presence (join / leave) and broacasts it to all connected users")
	public PresenceEventListener presenceEventListener(SimpMessagingTemplate messagingTemplate) {
		PresenceEventListener presence = new PresenceEventListener(messagingTemplate, participantRepository());
		presence.setLoginDestination(Destinations.LOGIN);
		presence.setLogoutDestination(Destinations.LOGOUT);
		return presence;
	}

	@Bean
	@Description("Keeps connected users")
	public ParticipantRepository participantRepository() {
		return new ParticipantRepository();
	}

	@Bean
	@Scope(value = "websocket", proxyMode = ScopedProxyMode.TARGET_CLASS)
	@Description("Keeps track of the level of profanity of a websocket session")
	public SessionProfanity sessionProfanity() {
		return new SessionProfanity(MAX_PROFANITY_LEVEL);
	}

	@Bean
	@Description("Utility class to check the number of profanities and filter them")
	public ProfanityChecker profanityFilter() {
		Set<String> profanities = new HashSet<>(Arrays.asList("damn", "crap", "ass"));
		ProfanityChecker checker = new ProfanityChecker();
		checker.setProfanities(profanities);
		return checker;
	}

	@Bean
	@Description("Spring Actuator endpoint to expose WebSocket stats")
	public WebSocketEndpoint websocketEndpoint(WebSocketMessageBrokerStats stats) {
		return new WebSocketEndpoint(stats);
	}

	@Bean
	@Description("Spring Actuator endpoint to expose WebSocket message mappings")
	public MessageMappingEndpoint messageMappingEndpoint() {
		return new MessageMappingEndpoint();
	}
	
	@Bean
    @ConditionalOnMissingBean(RequestContextListener.class)
    public RequestContextListener requestContextListener() {
        return new RequestContextListener();
    }


}
