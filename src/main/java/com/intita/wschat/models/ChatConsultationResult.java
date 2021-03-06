package com.intita.wschat.models;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonView;

import jsonview.Views;
/**
 * 
 * @author Nicolas Haiduchok
 */
@Entity(name = "chat_consultations_results")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class ChatConsultationResult implements Serializable,Comparable<ChatConsultationResult>  {

	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue
	@JsonView(Views.Public.class)
	private Long id;

	@ManyToOne(fetch=FetchType.LAZY)
	private Room room;
	
	@ManyToOne(fetch=FetchType.LAZY)
	private ChatUser chatUser;

	@OneToMany(mappedBy="result", fetch=FetchType.EAGER)
	private List<ChatConsultationResultValue> values;
	

	Date date;

	public ChatConsultationResult() {
		date = new Date();
	}

	public ChatConsultationResult(Room room, ChatUser user, List<ChatConsultationResultValue> values) {
		date = new Date();
		this.room = room;
		this.chatUser = user;
		this.values = values;
	}
	
	/*
	 * GET/SET
	 */


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




	public ChatUser getChatUser() {
		return chatUser;
	}

	public void setChatUser(ChatUser chatUser) {
		this.chatUser = chatUser;
	}

	public List<ChatConsultationResultValue> getValues() {
		return values;
	}

	public void setValues(List<ChatConsultationResultValue> values) {
		this.values = values;
	}

	public Date getDate() {
		return date;
	}

	public void setDate(Date date) {
		this.date = date;
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 * Reload
	 */
	@Override
	public int compareTo(ChatConsultationResult arg0) {
		if (arg0==null)return -1;
		return this.getId().compareTo(arg0.getId());
	}
}
