package com.intita.wschat.models;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToOne;

import com.fasterxml.jackson.annotation.JsonManagedReference;

/**
 * 
 * @author Samoenko Yuriy
 */
@Entity(name="chat_user_last_room_date")
public class ChatUserLastRoomDate implements Serializable,Comparable<ChatUserLastRoomDate> {

	/**
	 * 
	 */
	private static final long serialVersionUID = 89403535554426711L;
	
	@Id	
	@GeneratedValue
	private Long id;
	
	@ManyToOne( fetch = FetchType.LAZY)
	private Room room;
	
	@Column(name="last_logout")
	private Date lastLogout;
	
	@ManyToOne( fetch = FetchType.LAZY)
	private ChatUser chatUser;
	
	
	public void setChatUser(ChatUser chatUser) {
		this.chatUser = chatUser;
	}	
	
	public ChatUser getChatUser() {
		return chatUser;
	}

	public ChatUserLastRoomDate()
	{
		
	}
	  
	public ChatUserLastRoomDate(Long id, Date last_logout, Room last_room){
		this.id=id;
		this.lastLogout = last_logout;
		this.room = last_room;
	}
	
	public ChatUserLastRoomDate(Date last_logout, Room last_room){
		this.lastLogout = last_logout;
		this.room = last_room;
	}
	
	public Room getRoom() {
		return room;
	}

	public void setRoom(Room room) {
		this.room = room;
	}

	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	//@NotBlank
		
	public Date getLastLogout() {
		return lastLogout;
	}
	
	public void setLastLogout(Date date) {
		lastLogout = date;
	}	
	
	
	public Room getLastRoom() {
		return room;
	}
	
	public void setLastRoom(Room room) {
		this.room = room;
	}

	@Override
	public int compareTo(ChatUserLastRoomDate o) {
		if (o==null)return -1;
		return this.getId().compareTo(o.getId());
	}

}

