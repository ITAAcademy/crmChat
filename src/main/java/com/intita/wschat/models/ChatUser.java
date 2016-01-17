package com.intita.wschat.models;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.OneToOne;
import javax.validation.constraints.Size;

/**
 * 
 * @author Zinchuk Roman
 */
@Entity(name="chat_user")
public class ChatUser {

	public ChatUser(){
		
	}
	public ChatUser(Long id, String nickName, User intitaUser){
		this.id=id;
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
