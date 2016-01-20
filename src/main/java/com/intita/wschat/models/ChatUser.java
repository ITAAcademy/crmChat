package com.intita.wschat.models;


import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToMany;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.validation.constraints.Size;

/**
 * 
 * @author Zinchuk Roman
 */
@Entity(name="chat_user")
public class ChatUser implements Serializable {
	 @OneToMany(mappedBy = "chatUser")
	 List<ChatUserLastRoomDate> chatUserLastRoomDate;
	 
	
	public List<ChatUserLastRoomDate> getChatUserLastRoomDate() {
		return chatUserLastRoomDate;
	}
	public void setChatUserLastRoomDate(List<ChatUserLastRoomDate> chatUserLastRoomDate) {
		this.chatUserLastRoomDate = chatUserLastRoomDate;
	}
	public ChatUser(){
		
	}
	public ChatUser(String nickName, User intitaUser){
		this.nickName=nickName;
		this.intitaUser=intitaUser;
	}
public Long getId() {
	return id;
}
public void setId(Long id) {
	this.id = id;
}
public User getIntitaUser() {
	return intitaUser;
}
public void setIntitaUser(User intitaUser) {
	this.intitaUser = intitaUser;
}
public String getNickName() {
	return nickName;
}
public void setNickName(String nickName) {
	this.nickName = nickName;
}


@Id
@GeneratedValue
private Long id;

@OneToOne(fetch = FetchType.EAGER)
private User intitaUser;

@Size(min = 0, max = 50)
private String nickName;

@OneToOne(mappedBy = "chatUser",fetch = FetchType.EAGER)
private ChatTenant chatUser;


@OneToMany(mappedBy = "author", fetch = FetchType.EAGER)
private Set<Room> rooms = new HashSet<>();

@OneToMany(mappedBy = "author", fetch = FetchType.EAGER)
private List<UserMessage> messages = new ArrayList<>();

@ManyToMany(mappedBy = "users", fetch = FetchType.EAGER)
private Set<Room> roomsFromUsers = new HashSet<>();
public Set<Room> getRootRooms() {
	return rooms;
}

public Set<Room> getRoomsFromUsers() {
	return roomsFromUsers;
}


}
