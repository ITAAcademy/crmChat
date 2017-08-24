package com.intita.wschat.models;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.intita.wschat.dto.model.UserMessageDTO;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.NotFound;
import org.hibernate.annotations.NotFoundAction;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.fasterxml.jackson.annotation.JsonView;

import jsonview.Views;

@Entity(name = "chat_user_message")
public class UserMessage implements Serializable,Comparable<UserMessage>  {
	
	public UserMessage(){
		this.date= new Date();
	}
	public UserMessage(ChatUser author, Room room, UserMessageDTO userMessage){
	this.author = author;
	this.room = room;
	this.body = userMessage.getBody();
	this.date= new Date();
	this.setAttachedFiles(userMessage.getAttachedFiles());
	}
	public UserMessage(ChatUser author, Room room, String body){	
		this.author = author;
		this.room = room;
		this.body = body;
		this.date= new Date();
		}

		public static UserMessage forId(Long id){
		UserMessage message = new UserMessage();
		message.setId(id);
		return message;
		}
	
	@Id
	@GeneratedValue
	@JsonView(Views.Public.class)
	private Long id;
	
	//@NotBlank
	@ManyToOne( fetch = FetchType.LAZY )
	@NotNull
	@JsonManagedReference
	@JsonView(Views.Public.class)
	@NotFound(action=NotFoundAction.IGNORE)
	private ChatUser author;
	
	@ManyToOne(  fetch = FetchType.LAZY )
	@NotFound(action=NotFoundAction.IGNORE)
	private Room room;
	
	@Size(max=64000)
	@Column
	@JsonView(Views.Public.class)
	private String body;

	public boolean isActive() {
		return active;
	}

	public void setActive(boolean active) {
		this.active = active;
	}

	public String getAttachedFilesJson() {
		return attachedFilesJson;
	}

	public void setAttachedFilesJson(String attachedFilesJson) {
		this.attachedFilesJson = attachedFilesJson;
	}

	@JsonIgnore
	@Column(nullable = false)
	@ColumnDefault("1")
	private boolean active = true;

	@JsonView(Views.Public.class)
	@Lob
	private String attachedFilesJson;

	
	@Column
	@JsonView(Views.Public.class)
	private Date date;

	public Date getUpdateat() {
		return updateat;
	}

	public void setUpdateat(Date updateat) {
		this.updateat = updateat;
	}

	@Column(nullable = true)
	private Date updateat;

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
	@JsonProperty("attachedFilesJson")
	public ArrayList<String> getAttachedFiles() {
		ArrayList<String> result = null;
		ObjectMapper mapper = new ObjectMapper();
		if (attachedFilesJson==null || attachedFilesJson.length()<1) return new ArrayList<String>();
		try {
			result =  mapper.readValue(attachedFilesJson,ArrayList.class);
		} catch (IOException e) {
			e.printStackTrace();
			return new ArrayList<String>();
		}
		if (result==null) return new ArrayList<String>();
		return result;
	}
	@JsonProperty("attachedFilesJson")
	public void setAttachedFiles(ArrayList<String> attachedFiles) {
		if (attachedFiles==null) return ;
		ObjectMapper mapper = new ObjectMapper();
		try {
			this.attachedFilesJson = mapper.writeValueAsString(attachedFiles);
		} catch (JsonProcessingException e) {
			e.printStackTrace();
		}
	}
	
	
}
