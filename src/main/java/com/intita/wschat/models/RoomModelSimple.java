package com.intita.wschat.models;

import java.util.Date;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.intita.wschat.services.UserMessageService;
@Component
public class RoomModelSimple {
	@Autowired UserMessageService userMessageService;
	public Long getRoomAuthorId() {
		return roomAuthorId;
	}

	public void setRoomAuthorId(Long roomAuthorId) {
		this.roomAuthorId = roomAuthorId;
	}

	public Long getRoomId() {
		return roomId;
	}

	public void setRoomId(Long roomId) {
		this.roomId = roomId;
	}

	public String string;
	public Integer nums;
	public String date;
	public Long roomId;
	public Long roomAuthorId;
	public boolean active;
	public short type;
	public String lastMessage;
	public String lastMessageAuthor;
	public Date lastMessageDate;
	public int participantsCount;

	public short getType() {
		return type;
	}

	public void setType(short type) {
		this.type = type;
	}

	public boolean isActive() {
		return active;
	}

	public void setActive(boolean active) {
		this.active = active;
	}

	public String getDate() {
		return date;
	}

	public void setDate(String date) {
		this.date = date;
	}

	public RoomModelSimple() {
		string = "";
		nums = 0;
		date = new Date().toString();
	}

	public RoomModelSimple(Integer nums, String date,Room room,UserMessage lastMessage) {
		this.string = room.getName();
		this.nums = nums;
		this.date = date;
		this.roomId=room.getId();
		this.roomAuthorId=room.getAuthor().getId();
		this.active = room.isActive();
		this.type = room.getType();
		if (lastMessage!=null){
		this.lastMessage =  lastMessage.getBody();
		this.lastMessageAuthor = lastMessage.getAuthor().getNickName();
		this.lastMessageDate = lastMessage.getDate();
		}
		this.participantsCount = room.getParticipantsCount();
	}

	public String getLastMessage() {
		return lastMessage;
	}

	public void setLastMessage(String lastMessage) {
		this.lastMessage = lastMessage;
	}

	public int getParticipantsCount() {
		return participantsCount;
	}

	public void setParticipantsCount(int participantsCount) {
		this.participantsCount = participantsCount;
	}

	public String getLastMessageAuthor() {
		return lastMessageAuthor;
	}

	public void setLastMessageAuthor(String lastMessageAuthor) {
		this.lastMessageAuthor = lastMessageAuthor;
	}

	public Date getLastMessageDate() {
		return lastMessageDate;
	}

	public void setLastMessageDate(Date lastMessageDate) {
		this.lastMessageDate = lastMessageDate;
	}
}
