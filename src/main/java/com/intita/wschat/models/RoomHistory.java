package com.intita.wschat.models;

import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

@Table(
	    uniqueConstraints=
	        @UniqueConstraint(columnNames={"roomId", "chatUserId"})
	)
@Entity(name="room_history")
public class RoomHistory {
@Id
@GeneratedValue
Long id;
Long roomId;
Long chatUserId;
Date clearTime;
public Long getId() {
	return id;
}
public void setId(Long id) {
	this.id = id;
}
public Long getRoomId() {
	return roomId;
}
public void setRoomId(Long roomId) {
	this.roomId = roomId;
}
public Long getChatUserId() {
	return chatUserId;
}
public void setChatUserId(Long chatUserId) {
	this.chatUserId = chatUserId;
}
public Date getClearTime() {
	return clearTime;
}
public void setClearTime(Date clearTime) {
	this.clearTime = clearTime;
}
}
