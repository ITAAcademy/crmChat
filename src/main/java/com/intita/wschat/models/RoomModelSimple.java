package com.intita.wschat.models;

import java.util.Date;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.intita.wschat.services.UserMessageService;
import com.intita.wschat.web.ChatController;
@Component
public class RoomModelSimple {
	private final static Logger log = LoggerFactory.getLogger(RoomModelSimple.class);
	final int MULTI_IMAGE_MAX_IMAGE_COUNT = 4;
	final String NO_AVATAR_IMAGE_NAME = "noname.png";
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
	public String lastMessageAuthorAvatar;
	public Date lastMessageDate;
	public int participantsCount;
	public String avatars[];

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
		avatars = new String[4];
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
			User lastMessageIntitaUser = lastMessage.getAuthor().getIntitaUser();
			if (lastMessageIntitaUser!=null)
				this.lastMessageAuthorAvatar = lastMessageIntitaUser.getAvatar();
			else
				this.lastMessageAuthorAvatar = NO_AVATAR_IMAGE_NAME;
		}
		this.participantsCount = room.getParticipantsCount();
		this.avatars = generateMultiImageLinks(room);

	}
	public String[]	generateMultiImageLinks(Room room){
		Set<ChatUser> roomUsers = room.getUsers();
		int usersCount = roomUsers.size()+1;
		if (usersCount<=0){
			log.info("error, usersCount is "+usersCount);
			return null;
		}
		int imagesCountInMultiImage = (usersCount <= MULTI_IMAGE_MAX_IMAGE_COUNT) ? usersCount : MULTI_IMAGE_MAX_IMAGE_COUNT;
		String multiImageLinksArray[] = new String[imagesCountInMultiImage];
		multiImageLinksArray[0]=room.getAuthor().getIntitaUser().getAvatar();
		for (int i = 1; i < imagesCountInMultiImage;i++){
			ChatUser chatUser = room.getUsers().iterator().next();
			User intitaUser = chatUser.getIntitaUser();
			if (intitaUser != null)multiImageLinksArray[i]=intitaUser.getAvatar();
			else {
				multiImageLinksArray[i]=NO_AVATAR_IMAGE_NAME;
			}
		}
		return multiImageLinksArray;
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

	public String[] getAvatars() {
		return avatars;
	}

	public void setAvatars(String[] avatars) {
		this.avatars = avatars;
	}

	public String getLastMessageAuthorAvatar() {
		return lastMessageAuthorAvatar;
	}

	public void setLastMessageAuthorAvatar(String lastMessageAuthorAvatar) {
		this.lastMessageAuthorAvatar = lastMessageAuthorAvatar;
	}
}
