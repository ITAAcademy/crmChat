package com.intita.wschat.models;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;

import com.intita.wschat.domain.ChatRoomType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.intita.wschat.services.RoomsService;
import com.intita.wschat.services.UserMessageService;
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
	private List<String> avatars;
	private Long[] privateUserIds;
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
		avatars = new ArrayList();
	}

	public Integer getUserPermissions() {
		return userPermissions;
	}

	public void setUserPermissions(int userPermissions) {
		this.userPermissions = userPermissions;
	}

	public static RoomModelSimple buildSimpleModelForRoom(RoomsService roomService, ChatUser user, Integer nums, String date,Room room,UserMessage lastMessage) {
		RoomModelSimple simpleModel = new RoomModelSimple();
		if(room.getType() == ChatRoomType.PRIVATE.getValue())
		{
			try {
				PrivateRoomInfo info = roomService.getPrivateRoomInfo(room);
				ChatUser first = info.getFirtsUser();
				ChatUser second = info.getSecondUser();
				if(!first.equals(user))
					simpleModel.string = first.getNickName();
				else
					simpleModel.string = second.getNickName();
				simpleModel.privateUserIds = new Long[2];
				simpleModel.privateUserIds[0] = first.getId();
				simpleModel.privateUserIds[1] = second.getId();
			} catch (NullPointerException e) {
				simpleModel.string = room.getName();
			}
		}
		else
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
		ArrayList<Long> excludeFromAvatars = new ArrayList<Long>();
		excludeFromAvatars.add(user.getId());
		simpleModel.avatars = generateMultiImageLinks(room,excludeFromAvatars);
		return simpleModel;
		//this.userCapabilities = roomService.getPermissions(room, user);
	}
	public static List<String>	generateMultiImageLinks(Room room,List<Long> ignoreChatUsers){
		Set<ChatUser> roomUsers = room.cloneChatUsers();
		roomUsers.add(room.getAuthor());
		int usersCount = roomUsers.size();

		ArrayList<String> multiImageLinksArray = new ArrayList<String>();
		int i = 0;
		for(ChatUser chatUser : roomUsers ){
			if(multiImageLinksArray.size()>=MULTI_IMAGE_MAX_IMAGE_COUNT)break;
			if( ignoreChatUsers.contains(chatUser.getId()) ){
				i++;
				continue;
			}
			User intitaUser = chatUser.getIntitaUser();
			if (intitaUser != null && intitaUser.getAvatar() != null)multiImageLinksArray.add(intitaUser.getAvatar());
			else {
				multiImageLinksArray.add(NO_AVATAR_IMAGE_NAME);
			}
			i++;
		}
		if (multiImageLinksArray.isEmpty()){
			if(room.getAuthor().getIntitaUser()==null || room.getAuthor().getIntitaUser().getAvatar()==null
					|| room.getAuthor().getIntitaUser().getAvatar().length() < 1)
				multiImageLinksArray.add(NO_AVATAR_IMAGE_NAME);
			else
				multiImageLinksArray.add(room.getAuthor().getIntitaUser().getAvatar());
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

	public List<String> getAvatars() {
		return avatars;
	}

	public void setAvatars(List<String> avatars) {
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

	public Long getLastMessageAuthorId() {
		return lastMessageAuthorId;
	}

	public void setLastMessageAuthorId(Long lastMessageAuthorId) {
		this.lastMessageAuthorId = lastMessageAuthorId;
	}

	public Long[] getPrivateUserIds() {
		return privateUserIds;
	}

	public void setPrivateUserIds(Long[] privateUserIds) {
		this.privateUserIds = privateUserIds;
	}
	
}
