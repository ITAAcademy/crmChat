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
import javax.validation.constraints.Size;

import org.hibernate.metamodel.binding.CascadeType;
import org.hibernate.validator.constraints.NotBlank;
import org.springframework.beans.factory.annotation.Autowired;


@Entity(name="ChatRoom")
public class Room implements Serializable {
	@Id
	@GeneratedValue
	private Long id;
	
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
	private User author;
	
	@OneToMany(mappedBy = "room")
	private List<UserMessage> messages = new ArrayList<>();
	
//	@OneToMany
	//@ManyToMany(mappedBy = "roomsFromUsers")
	
	@ManyToMany(fetch = FetchType.EAGER)
	private Set<User> users = new HashSet<>();
	
	
	public Set<User> getUsers() {
		return users;
	}
	public Set<ChatUser> getChatUsers(){
		Set<ChatUser> chatUsers = new HashSet<ChatUser>();
		for (User u : users){
			chatUsers.add(u.getChatUser());
		}
		return chatUsers;
	}
	
	public boolean addUser(User user) {
		return users.add(user);
	}
	public boolean removeUser(User user) {
		return users.remove(user);
	}
	public Room()
	{
		
	}
	public User getAuthor() {
		return author;
	}


	public void setAuthor(User autor) {
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


	@Override
	public String toString() {
		return "ChatMessage ";
	}
}
