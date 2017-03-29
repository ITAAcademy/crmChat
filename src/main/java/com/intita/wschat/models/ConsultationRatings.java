package com.intita.wschat.models;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
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
@Entity(name = "chat_consultation_ratings")
public class ConsultationRatings implements Serializable,Comparable<ConsultationRatings>, Cloneable  {

	protected Object clone() throws CloneNotSupportedException {
		return super.clone();
	}
	@Id
	@GeneratedValue
	@JsonView(Views.Public.class)
	private Long id;

	private String name;

	public ConsultationRatings() {
		// TODO Auto-generated constructor stub
	}
	public ConsultationRatings(Long id) {
		this.id = id;
	}
	public ConsultationRatings(ConsultationRatings source){
		this.id=source.getId();
		this.name=source.getName();	
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

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
	/*
	 * (non-Javadoc)
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 * Reload
	 */
	@Override
	public int compareTo(ConsultationRatings arg0) {
		if (arg0==null)return -1;
		return this.getId().compareTo(arg0.getId());
	}
}
