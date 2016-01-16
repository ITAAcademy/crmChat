package com.sergialmar.wschat.domain;

import java.util.ArrayList;

import com.sergialmar.wschat.models.UserMessage;

/**
 * 
 * @author Sergi Almar
 */
public class ChatMessage {

	private String username;
	private String message;
	public ChatMessage(){
		
	}
	public ChatMessage(UserMessage usrMsg){
		this.username = usrMsg.getAuthor().getLogin();
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
}
