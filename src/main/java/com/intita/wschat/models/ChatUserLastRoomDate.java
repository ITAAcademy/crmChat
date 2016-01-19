package com.intita.wschat.models;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import java.util.Date;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToMany;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.validation.constraints.Size;

import org.hibernate.validator.constraints.NotBlank;

/**
 * 
 * @author Samoenko Yuriy
 */
@Entity(name="chat_user_last_room_date")
public class ChatUserLastRoomDate implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 89403535554426711L;
	
	@OneToOne
	private ChatUser chatUser;
	
	public void setChatUser(ChatUser chatUser) {
		this.chatUser = chatUser;
	}	
	
	public ChatUser getChatUser() {
		return chatUser;
	}

	  
	@Id	
	@NotBlank
	@GeneratedValue
	private Long id;

	

	public ChatUserLastRoomDate(){
		last_room = (long) 0;
		last_logout = new Date();
	}
	public ChatUserLastRoomDate(Long id, Long user_id){
		this.id=id;
	}
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	@NotBlank
	private Date last_logout;
	
	public Date getLastLogout() {
		return last_logout;
	}
	
	public void setLastLogout(Date date) {
		last_logout = date;
	}	
	
	@NotBlank
	private Long last_room;
	
	public Long getLastRoom() {
		return last_room;
	}
	
	public void setLastRoom(Long num) {
		last_room = num;
	}

}

