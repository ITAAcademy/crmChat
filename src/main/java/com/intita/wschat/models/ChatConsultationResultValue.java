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

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.fasterxml.jackson.annotation.JsonView;
import com.intita.wschat.domain.ChatMessage;

import jsonview.Views;
/**
 * 
 * @author Nicolas Haiduchok
 */
@Entity(name = "chat_consultations_results_values")
public class ChatConsultationResultValue implements Serializable,Comparable<ChatConsultationResultValue>  {
	private static final long serialVersionUID = -5795522170195997862L;

	@Id
	@GeneratedValue
	@JsonView(Views.Public.class)
	private Long id;

	
	@NotNull
	@ManyToOne
	@JsonIgnore
	private ChatConsultationResult result;
	
	@ManyToOne(fetch=FetchType.LAZY)
	private ConsultationRatings rating;

	private int value = 0; 

	public ChatConsultationResultValue() {

	}
	public ChatConsultationResultValue(ConsultationRatings rating, int value) {
		this.rating = rating;
		this.value = value;
	}
	/*
	 * GET/SET
	 */

	@Override
	public int compareTo(ChatConsultationResultValue arg0) {
		// TODO Auto-generated method stub
		return 0;
	}



	public Long getId() {
		return id;
	}



	public void setId(Long id) {
		this.id = id;
	}



	public ChatConsultationResult getResult() {
		return result;
	}



	public void setResult(ChatConsultationResult result) {
		this.result = result;
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
		ChatConsultationResultValue other = (ChatConsultationResultValue) obj;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		return true;
	}


}
