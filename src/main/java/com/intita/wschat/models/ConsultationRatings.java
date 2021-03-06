package com.intita.wschat.models;

import java.io.Serializable;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

import com.fasterxml.jackson.annotation.JsonView;

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
	private Boolean active = true; 

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
	public Boolean isActive() {
		return active;
	}
	public void setActive(Boolean active) {
		this.active = active;
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
