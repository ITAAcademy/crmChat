package com.intita.wschat.event;

import java.util.Date;

import com.intita.wschat.models.ChatUser;
import com.intita.wschat.models.User;

/**
 * 
 * @author Nicolas
 */
public class LoginEvent {

	private String username;
	private Long chatUserId;
	private Long intitaUserId;
	private Date time;
	private boolean typing;
	private String avatar;
	private int role;
	private boolean online;

	public Long getIntitaUserId() {
		return intitaUserId;
	}

	public void setIntitaUserId(Long intitaUserId) {
		this.intitaUserId = intitaUserId;
	}
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

	public boolean isOnline() {
		return online;
	}

	public void setOnline(boolean online) {
		this.online = online;
	}

	public LoginEvent(Long chatUserId,String username,boolean isOnline) {
		this.chatUserId=chatUserId;
		this.username = username;
		time = new Date();
		typing = false;
		online = isOnline;
	}
	public LoginEvent(Long intitaUserId, Long chatUserId,String username, String avatar, boolean isOnline) {
		this.intitaUserId = intitaUserId;
		this.chatUserId=chatUserId;
		this.username = username;
		time = new Date();
		typing = false;
		online = isOnline;
		this.avatar = avatar;
	}
	public LoginEvent(ChatUser u, boolean isOnline){
		if (u!=null){
			this.chatUserId=u.getId();
			User intitaUser = u.getIntitaUser();
			this.username = u.getNickName();
			if (intitaUser!=null){
				this.avatar = intitaUser.getAvatar();
				this.intitaUserId = intitaUser.getId();
			}
		}
		time = new Date();
		typing = false;
		online = isOnline;
		
	}

	public LoginEvent(Long chatUserId,String username, String avatar,boolean isOnline) {
		this.chatUserId=chatUserId;
		this.username = username;
		this.avatar = avatar;
		time = new Date();
		typing = false;
		online = isOnline;
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
