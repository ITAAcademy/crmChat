package com.intita.wschat.event;

import java.util.Date;

/**
 * 
 * @author Sergi Almar
 */
public class LoginEvent {

	private String username;
	private Long chatUserId;
	private Date time;
	

	public LoginEvent(Long chatUserId,String username) {
		this.chatUserId=chatUserId;
		this.username = username;
		time = new Date();
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public Date getTime() {
		return time;
	}

	public void setTime(Date time) {
		this.time = time;
	}

	public Long getChatUserId() {
		return chatUserId;
	}

	public void setChatUserId(Long chatUserId) {
		this.chatUserId = chatUserId;
	}
}
