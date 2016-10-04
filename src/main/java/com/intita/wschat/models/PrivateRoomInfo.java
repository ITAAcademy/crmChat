package com.intita.wschat.models;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import org.hibernate.validator.constraints.NotBlank;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.fasterxml.jackson.annotation.JsonView;

import jsonview.Views;

/**
 * 
 * @author Nicolas Haiduchok
 */
@Entity(name="chat_private_rooms")
public class PrivateRoomInfo implements Serializable,Comparable<PrivateRoomInfo> {
	/**
	 * 
	 */
	private static final long serialVersionUID = -4745865227177681310L;

	public PrivateRoomInfo()
	{
		
	}
	public PrivateRoomInfo(Room room, ChatUser first, ChatUser second) {
		this.room = room;
		this.firtsUser = first;
		this.secondUser = second;
	}
	@Id
	@GeneratedValue
	@JsonView(Views.Public.class)
	private Long id;

	@NotNull
	@OneToOne(fetch = FetchType.LAZY)
	Room room;

	@NotNull
	@ManyToOne( fetch = FetchType.LAZY)
	private ChatUser firtsUser;

	@NotNull
	@ManyToOne( fetch = FetchType.LAZY)
	private ChatUser secondUser;
	
	
	
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	public Room getRoom() {
		return room;
	}
	public void setRoom(Room room) {
		this.room = room;
	}
	public ChatUser getFirtsUser() {
		return firtsUser;
	}
	public void setFirtsUser(ChatUser firtsUser) {
		this.firtsUser = firtsUser;
	}
	public ChatUser getSecondUser() {
		return secondUser;
	}
	public void setSecondUser(ChatUser secondUser) {
		this.secondUser = secondUser;
	}
	@Override
	public int compareTo(PrivateRoomInfo o) {
		if (o==null)return -1;
		return this.getId().compareTo(o.getId());
	}
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		return result;
	}
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		PrivateRoomInfo other = (PrivateRoomInfo) obj;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		return true;
	}
}
