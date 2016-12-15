package com.intita.wschat.models;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;

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
	private ChatConsultation consultation;

	@ManyToOne(fetch=FetchType.LAZY)
	private ConsultationRatings rating;

	private int value; 

	public ChatConsultationResult() {

	}
	public ChatConsultationResult(Long id, Integer val, ChatConsultation cons) {
		value = val;
		rating = new ConsultationRatings();
		rating.setId(id);
		consultation = cons;
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

	public ChatConsultation getConsultation() {
		return consultation;
	}

	public void setConsultation(ChatConsultation consultation) {
		this.consultation = consultation;
	}

	public ConsultationRatings getRating() {
		return rating;
	}

	public void setRating(ConsultationRatings rating) {
		this.rating = rating;
	}

	public int getValue() {
		return value;
	}

	public void setValue(int value) {
		this.value = value;
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
