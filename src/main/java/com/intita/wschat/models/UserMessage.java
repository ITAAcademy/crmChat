package com.intita.wschat.models;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import org.hibernate.validator.constraints.NotBlank;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.fasterxml.jackson.annotation.JsonView;

import jsonview.Views;

@Entity(name = "chat_user_message")
public class UserMessage implements Serializable,Comparable<UserMessage>  {
	
	public UserMessage(){
		this.date= new Date();
	}
	public UserMessage(ChatUser author, Room room, String body){	
	this.author = author;
	this.room = room;
	this.body = body;
	this.date= new Date();
	}
	
	@Id
	@GeneratedValue
	@JsonView(Views.Public.class)
	private Long id;
	
	//@NotBlank
	@ManyToOne
	@NotNull
	@JsonManagedReference
	@JsonView(Views.Public.class)
	private ChatUser author;
	
	@ManyToOne
	private Room room;
	
	@Size(max=64000)
	@Column
	@JsonView(Views.Public.class)
	private String body;
	
	@Column
	@JsonView(Views.Public.class)
	private Date date;

	public ChatUser getAuthor() {
		return author;
	}
	public void setAuthor(ChatUser author) {
		this.author = author;
	}
	public Room getRoom() {
		return room;
	}
	public void setRoom(Room room) {
		this.room = room;
	}
	public String getBody() {
		return body;
	}
	public void setBody(String body) {
		this.body = body;
	}
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	public Date getDate() {
		return date;
	}
	public void setDate(Date date) {
		this.date = date;
	}
	@Override
	public int compareTo(UserMessage o) {
		if (o==null)return -1;
		return this.getId().compareTo(o.getId());
	}
	
	
}
