package com.intita.wschat.models;


import java.io.Serializable;
import java.security.Principal;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToMany;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Transient;
import javax.validation.constraints.Size;

import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.annotation.JsonView;
import com.intita.wschat.event.ParticipantRepository;

import jsonview.Views;


/**
 * 
 * @author Zinchuk Roman
 */
@Entity(name="chat_user")
public class ChatUser implements Serializable,Comparable<ChatUser> {

	@Autowired
	@Transient
    private SessionFactory factory;
	
	@OneToMany(mappedBy = "chatUser", fetch = FetchType.EAGER)
	List<ChatUserLastRoomDate> chatUserLastRoomDate;

	@Id
	@GeneratedValue
	@Column(columnDefinition="BIGINT")
	private Long id;

	@OneToOne(fetch = FetchType.EAGER)
	private User intitaUser;

	@JsonView(Views.Public.class)
	@Size(min = 0, max = 50)
	private String nickName;

	@OneToOne(mappedBy = "chatUser",fetch = FetchType.EAGER)
	private ChatTenant chatUser;


	@OneToMany(mappedBy = "author", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
	private Set<Room> rooms = new HashSet<>();

	@OneToMany(mappedBy = "author", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
	private List<UserMessage> messages = new ArrayList<>();

	@ManyToMany(mappedBy = "users", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
	private Set<Room> roomsFromUsers = new HashSet<>();
	
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
		if (!(obj instanceof ChatUser))
			return false;
		ChatUser other = (ChatUser) obj;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (id.longValue() != other.getId().longValue())
			return false;
		return true;
	}

	public ChatUser(){

	}
	public static ChatUser forId(Long id){
		ChatUser chatUser = new ChatUser();
		chatUser.id = id;
		return chatUser;
	}
	public ChatUser(String nickName, User intitaUser){
		this.nickName=nickName;
		this.intitaUser=intitaUser;
	}
	public ChatUser(User intitaUser){
		this.nickName=intitaUser.getLogin();
		this.intitaUser=intitaUser;
	}
	public ChatUser(Long id){
		setId(id);
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
	
	public Principal getPrincipal()
	{
		return new Principal() {
			
			@Override
			public String getName() {
				return getId().toString();
			}
		};
	}
	
	@Override
	public int compareTo(ChatUser o) {
		if (o==null)return -1;
		return this.getId().compareTo(o.getId());
	}
	



}
