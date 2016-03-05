package org.springframework.boot.actuate.trace;

import java.security.Principal;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.AutoConfigurationPackage;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.ChannelInterceptorAdapter;
import org.springframework.messaging.support.GenericMessage;
import org.springframework.messaging.support.NativeMessageHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import com.intita.wschat.models.ChatUser;
import com.intita.wschat.models.Room;
import com.intita.wschat.services.ChatUsersService;
import com.intita.wschat.services.RoomsService;

/**
 * {@link ChannelInterceptor} that logs messages to a {@link TraceRepository}.
 *
 * @author Roma
 */

@Configuration
@Component
@AutoConfigurationPackage
public class WebSocketTraceChannelInterceptor extends ChannelInterceptorAdapter {

	@Autowired ChatUsersService chatUsersService;
	@Autowired RoomsService roomsService;
	private class RegexResult {
		public RegexResult(){

		}
		public RegexResult(boolean finded,boolean correct){
			this.finded=finded;
			this.correct=correct;
		}
		public boolean isFinded() {
			return finded;
		}
		public void setFinded(boolean finded) {
			this.finded = finded;
		}
		public boolean isCorrect() {
			return (correct && finded) || !finded;
		}
		public void setCorrect(boolean correct) {
			this.correct = correct;
		}
		private boolean finded;
		private boolean correct;
	}

	private TraceRepository traceRepository;
	public void setTraceRepository(TraceRepository traceRepository) {
		this.traceRepository = traceRepository;
	}

	final String[] mappingStrings = new String[]
			{//chat mappings
					"/(*)/chat.message", // 0 - room
					"/(*)/chat.private.{username}", //0 - room, 1 - username
					"/chat.go.to.dialog/(*)", // 0 - room
					"/chat.go.to.dialog.list/(*)" // 0 - room
					// 0 - room				
			};

	public WebSocketTraceChannelInterceptor(TraceRepository traceRepository) {
		this.traceRepository = traceRepository;
	}
	public WebSocketTraceChannelInterceptor() {
		this.traceRepository = new TraceRepository() {
			
			@Override
			public List<Trace> findAll() {
				// TODO Auto-generated method stub
				return null;
			}
			
			@Override
			public void add(Map<String, Object> traceInfo) {
				// TODO Auto-generated method stub
				
			}
		};
	}
	
	@Override
	public Message<?> preSend(Message<?> message, MessageChannel channel) {
		StompHeaderAccessor headerAccessor= StompHeaderAccessor.wrap(message);
		if (StompCommand.SUBSCRIBE.equals(headerAccessor.getCommand()) && headerAccessor.getUser() !=null ) {
			Principal userPrincipal = headerAccessor.getUser();
			//System.out.println("!!!!!!DDDDDDDDDDDDd");
			if(!validateSubscription(userPrincipal, headerAccessor.getDestination()))
			{
				//throw new IllegalArgumentException("No permission for this topic");
				return null;
			}
		}
		return message;
	}

	private RegexResult isChatParticipantsMappingCorrect(Principal principal,String topicDestination){
		RegexResult result = new RegexResult();
		String patternStr = ".*/(.*)/chat.participants";
		Pattern pattern = Pattern.compile(patternStr);
		Matcher m = pattern.matcher(topicDestination);
		if (m.find()){
			result.finded=true;
			String roomStr = m.group(1);
			
			if (roomStr==null) 
				return result;
			
			Long userId  = Long.parseLong(principal.getName());
			ChatUser chatUser = chatUsersService.getChatUser(userId);
			
			if (chatUser==null) 
				return result;
			
			Room room = roomsService.getRoom(Long.parseLong(roomStr));
			
			if (room==null) 
				return result;
			
			if (room.getChatUsers().contains(chatUser) || room.getAuthor().getId()==userId) result.correct=true;   
		}
		return result;
	}

	private boolean validateSubscription(Principal principal, String topicDestination)
	{

		System.out.println(principal.getName());
		System.out.println(topicDestination);
		if (!isChatParticipantsMappingCorrect(principal,topicDestination).isCorrect())
			return false;

		return true;
	}
	@Override
	public void afterSendCompletion(Message<?> message, MessageChannel channel, boolean sent, Exception ex) {
		Map<String, Object> trace = new LinkedHashMap<String, Object>();

		StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(message);

		// Don't trace non-STOMP messages (like heartbeats)
		if(headerAccessor.getCommand() == null) {
			return;
		}

		String payload = new String((byte[]) message.getPayload());

		trace.put("stompCommand", headerAccessor.getCommand().name());
		trace.put("nativeHeaders", getNativeHeaders(headerAccessor));

		if(!payload.isEmpty()) {
			trace.put("payload", payload);
		}

		traceRepository.add(trace);
	}

	@SuppressWarnings("unchecked")
	private Map<String, Object> getNativeHeaders(StompHeaderAccessor headerAccessor) {
		Map<String, List<String>> nativeHeaders = 
				(Map<String, List<String>>) headerAccessor.getHeader(NativeMessageHeaderAccessor.NATIVE_HEADERS);

		if(nativeHeaders == null) {
			return Collections.EMPTY_MAP;
		}

		Map<String, Object> traceHeaders = new LinkedHashMap<String, Object>();

		for(String header : nativeHeaders.keySet()) {
			List<String> headerValue = (List<String>) nativeHeaders.get(header);
			Object value = headerValue;

			if(headerValue.size() == 1) {
				value = headerValue.get(0);
			} else if(headerValue.isEmpty()) {
				value = "";
			}

			traceHeaders.put(header, value);
		}

		return traceHeaders;
	}
}
