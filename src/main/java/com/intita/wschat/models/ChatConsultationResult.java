package com.intita.wschat.models;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import org.hibernate.annotations.NotFound;
import org.hibernate.annotations.NotFoundAction;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.fasterxml.jackson.annotation.JsonView;
import com.intita.wschat.domain.ChatMessage;

import jsonview.Views;
/**
 * 
 * @author Nicolas Haiduchok
 */
@Entity(name = "chat_consultations_results")
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

	@OneToMany(mappedBy="result", fetch=FetchType.LAZY)
	private List<ChatConsultationResultValues> values;
	

	Date date;

	public ChatConsultationResult() {
		date = new Date();
	}

	public ChatConsultationResult(Room room, ChatUser user, List<ChatConsultationResultValues> values) {
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

	public List<ChatConsultationResultValues> getValues() {
		return values;
	}

	public void setValues(List<ChatConsultationResultValues> values) {
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
