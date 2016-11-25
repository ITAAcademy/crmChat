package com.intita.wschat.models;

import java.util.Date;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.intita.wschat.services.RoomsService;
import com.intita.wschat.services.UserMessageService;
import com.intita.wschat.web.ChatController;
@Component
public class RoomModelSimple {
	private final static Logger log = LoggerFactory.getLogger(RoomModelSimple.class);
	final static int MULTI_IMAGE_MAX_IMAGE_COUNT = 4;
	final static String NO_AVATAR_IMAGE_NAME = "noname.png";
	
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

	private String string;
	private Integer nums;
	private String date;
	private Long roomId;
	private Long roomAuthorId;
	private boolean active;
	private short type;
	private String lastMessage;
	private String lastMessageAuthor;
	private Long lastMessageAuthorId;
	private String lastMessageAuthorAvatar;
	private Date lastMessageDate;
	private int participantsCount;
	private String avatars[];
	Integer userPermissions;

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

	public Integer getUserPermissions() {
		return userPermissions;
	}

	public void setUserPermissions(int userPermissions) {
		this.userPermissions = userPermissions;
	}

	public static RoomModelSimple buildSimpleModelForRoom(ChatUser user, Integer nums, String date,Room room,UserMessage lastMessage) {
		RoomModelSimple simpleModel = new RoomModelSimple();
		simpleModel.string = room.getName();
		simpleModel.nums = nums;
		simpleModel.date = date;
		simpleModel.roomId=room.getId();
		simpleModel.roomAuthorId=room.getAuthor().getId();
		simpleModel.active = room.isActive();
		simpleModel.type = room.getType();
		if (lastMessage!=null){
			simpleModel.lastMessage =  lastMessage.getBody();
			simpleModel.lastMessageAuthor = lastMessage.getAuthor().getNickName();
			simpleModel.lastMessageAuthorId = lastMessage.getAuthor().getId();
			simpleModel.lastMessageDate = lastMessage.getDate();
			User lastMessageIntitaUser = lastMessage.getAuthor().getIntitaUser();
			if (lastMessageIntitaUser!=null)
				simpleModel.lastMessageAuthorAvatar = lastMessageIntitaUser.getAvatar();
			else
				simpleModel.lastMessageAuthorAvatar = NO_AVATAR_IMAGE_NAME;
		}
		simpleModel.participantsCount = room.getParticipantsCount();
		simpleModel.avatars = generateMultiImageLinks(room);
		return simpleModel;
		//this.userCapabilities = roomService.getPermissions(room, user);
	}
	public static String[]	generateMultiImageLinks(Room room){
		Set<ChatUser> roomUsers = room.getUsers();
		int usersCount = roomUsers.size()+1;
		if (usersCount<=0){
			log.info("error, usersCount is "+usersCount);
			return null;
		}
		int imagesCountInMultiImage = (usersCount <= MULTI_IMAGE_MAX_IMAGE_COUNT) ? usersCount : MULTI_IMAGE_MAX_IMAGE_COUNT;
		String multiImageLinksArray[] = new String[imagesCountInMultiImage];
		if(room.getAuthor().getIntitaUser() != null)
			multiImageLinksArray[0]=room.getAuthor().getIntitaUser().getAvatar();
		else
			multiImageLinksArray[0] = NO_AVATAR_IMAGE_NAME;
		
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

	public String getString() {
		return string;
	}

	public void setString(String string) {
		this.string = string;
	}

	public Integer getNums() {
		return nums;
	}

	public void setNums(Integer nums) {
		this.nums = nums;
	}

	public void setUserPermissions(Integer userPermissions) {
		this.userPermissions = userPermissions;
	}
}
