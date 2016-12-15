package com.intita.wschat.models;

import java.io.Serializable;
import java.sql.Time;
import java.util.ArrayList;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
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
@Entity(name = "consultations_calendar")
public class IntitaConsultation implements Serializable,Comparable<IntitaConsultation>  {

	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue
	@JsonView(Views.Public.class)
	private Long id;

	
	@ManyToOne( fetch = FetchType.LAZY)
	@NotNull
	@JsonManagedReference
	@JsonView(Views.Public.class)
	@NotFound(action=NotFoundAction.IGNORE)
	@JoinColumn(name="user_id")
	private User author;

	@ManyToOne( fetch = FetchType.LAZY)
	@NotNull
	@JsonManagedReference
	@JsonView(Views.Public.class)
	@NotFound(action=NotFoundAction.IGNORE)
	@JoinColumn(name="teacher_id")
	private User consultant;
	 

	@ManyToOne( fetch = FetchType.LAZY)
	@JsonManagedReference
	@JsonView(Views.Public.class)
	@NotFound(action=NotFoundAction.IGNORE)
	@JoinColumn(name="lecture_id") //999
	private Lectures lecture;
	
	public Lectures getLecture() {
		return lecture;
	}

	public void setLecture(Lectures lecture) {
		this.lecture = lecture;
	}

	@NotNull
	@Column(name="date_cons")
	private Date date;

	@Column(name="start_cons")
	private Time startTime;
	
	@Column(name="end_cons")
	private Time finishTime;

	@OneToOne(mappedBy = "intitaConsultation",fetch = FetchType.EAGER)
	private ChatConsultation chatConsultation;
	
	public IntitaConsultation() {
		
	}
	
	/*
	 * GET/SET
	 */
	public Long getId() {
		return id;
	}

	public static long getSerialversionuid() {
		return serialVersionUID;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public void setAuthor(User author) {
		this.author = author;
	}

	public void setConsultant(User consultant) {
		this.consultant = consultant;
	}

	public void setDate(Date date) {
		this.date = date;
	}

	public void setStartTime(Time startTime) {
		this.startTime = startTime;
	}

	public void setFinishTime(Time finishTime) {
		this.finishTime = finishTime;
	}

	public void setChatConsultation(ChatConsultation chatConsultation) {
		this.chatConsultation = chatConsultation;
	}

	public User getAuthor() {
		return author;
	}

	public User getConsultant() {
		return consultant;
	}

	public Date getDate() {
		return date;
	}

	public Time getStartTime() {
		return startTime;
	}

	public Time getFinishTime() {
		return finishTime;
	}
	
	public ChatConsultation getChatConsultation() {
		return chatConsultation;
	}
	
	public boolean isMy(User user)
	{
		if(author.equals(user) || consultant.equals(user))
			return true;
		return false;
	}
	
	/*
	 * (non-Javadoc)
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 * Reload
	 */
	@Override
	public int compareTo(IntitaConsultation arg0) {
		if (arg0==null)return -1;
		return this.getId().compareTo(arg0.getId());
	}


}
