package com.intita.wschat.models;

import java.io.Serializable;
import java.sql.Date;
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
import javax.validation.constraints.Size;

import org.hibernate.metamodel.binding.CascadeType;
import org.hibernate.validator.constraints.NotBlank;
import org.springframework.beans.factory.annotation.Autowired;


@Entity(name="ChatRoom")
public class Room implements Serializable,Comparable<Room> {
	@Id
	@GeneratedValue
	private Long id;
	
	 @OneToMany(mappedBy = "room")
	List< ChatUserLastRoomDate> chatUserLastRoomDate;
	
	public List<ChatUserLastRoomDate> getChatUserLastRoomDate() {
		return chatUserLastRoomDate;
	}
	public void setChatUserLastRoomDate(List<ChatUserLastRoomDate> chatUserLastRoomDate) {
		this.chatUserLastRoomDate = chatUserLastRoomDate;
	}



	private boolean active = true;
	
	@NotBlank
	@Size(min = 1, max = 255)
	@Column(unique = false)
	private String name;
	
	public boolean isActive() {
		return active;
	}
	public void setActive(boolean active) {
		this.active = active;
	}
	
	

	@ManyToOne
	private ChatUser author;
	
	@OneToMany(mappedBy = "room")
	private List<UserMessage> messages = new ArrayList<>();
	
//	@OneToMany
	//@ManyToMany(mappedBy = "roomsFromUsers")
	
	@ManyToMany(fetch = FetchType.EAGER)
	private Set<ChatUser> users = new HashSet<>();
	
	
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


	@Override
	public String toString() {
		return "ChatMessage ";
	}
	@Override
	public int compareTo(Room o) {
		if (o==null)return -1;
		return this.getId().compareTo(o.getId());
	}
}
