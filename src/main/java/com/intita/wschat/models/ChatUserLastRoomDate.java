package com.intita.wschat.models;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Entity;
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
	
	@ManyToOne
	private Room room;
	
	private Date last_logout;
	
	@ManyToOne
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
		this.last_logout = last_logout;
		this.room = last_room;
	}
	
	public ChatUserLastRoomDate(Date last_logout, Room last_room){
		this.last_logout = last_logout;
		this.room = last_room;
	}
	
	public Room getRoom() {
		return room;
	}

	public void setRoom(Room room) {
		this.room = room;
	}

	public Date getLast_logout() {
		return last_logout;
	}

	public void setLast_logout(Date last_logout) {
		this.last_logout = last_logout;
	}

	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	//@NotBlank
		
	public Date getLastLogout() {
		return last_logout;
	}
	
	public void setLastLogout(Date date) {
		last_logout = date;
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

