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
	private boolean typing;
	private String avatar;
	private int role;

	public String getAvatar() {
		return avatar;
	}

	public void setAvatar(String avatar) {
		this.avatar = avatar;
	}

	public int getRole() {
		return role;
	}

	public void setRole(int role) {
		this.role = role;
	}

	public LoginEvent(Long chatUserId,String username) {
		this.chatUserId=chatUserId;
		this.username = username;
		time = new Date();
		typing = false;
	}
	
	public LoginEvent(Long chatUserId,String username, String avatar) {
		this.chatUserId=chatUserId;
		this.username = username;
		this.avatar = avatar;
		time = new Date();
		typing = false;
	}

	public String getUsername() {
		return username;
	}

	public boolean isTyping() {
		return typing;
	}

	public void setTyping(boolean typing) {
		this.typing = typing;
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
