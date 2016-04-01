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
@Entity(name="ChatRoom")
public class Room implements Serializable,Comparable<Room> {
	@Id
	@GeneratedValue
	@JsonView(Views.Public.class)
	private Long id;

	@OneToMany(mappedBy = "room",fetch = FetchType.LAZY)
	List< ChatUserLastRoomDate> chatUserLastRoomDate;

	@JsonView(Views.Public.class)
	private boolean active = true;

	@NotBlank
	@Size(min = 1, max = 255)
	@Column(unique = false)
	@JsonView(Views.Public.class)
	private String name;

	@NotNull
	@JsonView(Views.Public.class)
	private short type;

	@ManyToOne
	private ChatUser author;

	@OneToMany(mappedBy = "room", fetch = FetchType.LAZY)
	private List<UserMessage> messages = new ArrayList<>();

	@ManyToMany(fetch = FetchType.LAZY)
	private Set<ChatUser> users = new HashSet<>();


	
	public static class RoomType
	{
		public static final short DEFAULT = 0;
		public static final short PRIVATE = 1;
		public static final short CONSULTATION = 2;
	}
	
	
	/*
	 * GET/SET
	 */
	public Set<ChatUser> getUsers() {
		return users;
	}
	public Set<ChatUser> getChatUsers(){
		Set<ChatUser> chatUsers = new HashSet<ChatUser>();
		for (ChatUser u : users){
			chatUsers.add(u);
		}
		return chatUsers;
	}

	public boolean addUser(ChatUser user) {
		return users.add(user);
	}
	public boolean removeUser(User user) {
		return users.remove(user);
	}
	public Room()
	{

	}
	public Room(long id)
	{
		setId(id);
	}
	public ChatUser getAuthor() {
		return author;
	}


	public void setAuthor(ChatUser autor) {
		this.author = autor;
	}

	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	public String getName() {
		return name;
	}


	public void setName(String name) {
		this.name = name;
	}
	public static List<String> getRoomsNames(Iterable<Room> rooms){
		List<String> result = new ArrayList<String>();
		for (Room room : rooms){
			result.add(room.getName());
		}
		return result;
	}

	public boolean isActive() {
		return active;
	}
	public void setActive(boolean active) {
		this.active = active;
	}
	@JsonIgnore
	public List<ChatUserLastRoomDate> getChatUserLastRoomDate() {
		return chatUserLastRoomDate;
	}
	@JsonIgnore
	public void setChatUserLastRoomDate(List<ChatUserLastRoomDate> chatUserLastRoomDate) {
		this.chatUserLastRoomDate = chatUserLastRoomDate;
	}	
	@Override
	public String toString() {
		return "ChatMessage ";
	}
	public short getType() {
		return type;
	}
	public void setType(short type) {
		this.type = type;
	}
	
	@Override
	public int compareTo(Room o) {
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
		Room other = (Room) obj;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		return true;
	}
}
