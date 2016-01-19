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
	 @OneToOne(mappedBy = "chatUser")
	 ChatUserLastRoomDate chatUserLastRoomDate;
	 
	 public ChatUserLastRoomDate getChatUserLastRoomDate() {
		 return chatUserLastRoomDate;
	 }
	 
	 public void setChatUserLastRoomDate(ChatUserLastRoomDate value) {
		 chatUserLastRoomDate = value;
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



}
