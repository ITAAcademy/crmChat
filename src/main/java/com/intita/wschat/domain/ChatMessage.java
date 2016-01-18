package com.intita.wschat.domain;

import java.util.ArrayList;

import com.intita.wschat.models.UserMessage;

/**
 * 
 * @author Sergi Almar
 */
public class ChatMessage {

	private String username;
	private String message;
	private Long chatUserId;
	public ChatMessage(){
		
	}
	public ChatMessage(UserMessage usrMsg){
		this.username = usrMsg.getAuthor().getNickName();
		this.message = usrMsg.getBody();
	}
	
	static public ArrayList<ChatMessage> getAllfromUserMessages (ArrayList<UserMessage> userMessages){
		ArrayList<ChatMessage> result = new ArrayList<ChatMessage>();
		for(UserMessage singleUserMessage : userMessages){
			result.add(new ChatMessage(singleUserMessage));
		}
		return result;
	}
	
	public String getUsername() {
		return username;
	}
	public void setUsername(String username) {
		this.username = username;
	}
	public String getMessage() {
		return message;
	}
	public void setMessage(String message) {
		this.message = message;
	}
	@Override
	public String toString() {
		return "ChatMessage ";
	}
	public Long getChatUserId() {
		return chatUserId;
	}
	public void setChatUserId(Long chatUserId) {
		this.chatUserId = chatUserId;
	}
	
}
