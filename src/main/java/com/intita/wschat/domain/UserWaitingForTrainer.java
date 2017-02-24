package com.intita.wschat.domain;

import com.intita.wschat.event.LoginEvent;

public class UserWaitingForTrainer {
//private LoginEvent user;
private Long chatUserId;
private String lastMessage;
private String avatar;
private Long roomId;
private String name;
public Long getChatUserId() {
	return chatUserId;
}
public void setChatUserId(Long chatUserId) {
	this.chatUserId = chatUserId;
}
public String getLastMessage() {
	return lastMessage;
}
public void setLastMessage(String lastMessage) {
	this.lastMessage = lastMessage;
}
public UserWaitingForTrainer(){
	
}
public UserWaitingForTrainer(Long roomId,String lastMessage,LoginEvent loginEvent){
	this.roomId = roomId;
	this.chatUserId = loginEvent.getChatUserId();
	this.lastMessage = lastMessage;
	this.avatar = loginEvent.getAvatar();
	this.name = loginEvent.getUsername();
	if (this.name==null || this.name.length() < 1)
		this.name = loginEvent.getNickName();
		
}
public String getAvatar() {
	return avatar;
}
public void setAvatar(String avatar) {
	this.avatar = avatar;
}
public Long getRoomId() {
	return roomId;
}
public void setRoomId(Long roomId) {
	this.roomId = roomId;
}
public String getName() {
	return name;
}
public void setName(String name) {
	this.name = name;
}



}
