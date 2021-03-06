package com.intita.wschat.models;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.OneToOne;
import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonView;

import jsonview.Views;
/**
 * 
 * @author Nicolas Haiduchok
 */
@Entity(name = "chat_consultations")
public class ChatConsultation implements Serializable,Comparable<ChatConsultation>  {

	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue
	@JsonView(Views.Public.class)
	private Long id;

	@OneToOne(fetch=FetchType.EAGER)
	@NotNull
	private IntitaConsultation intitaConsultation;

	private Date startDate = null;

	private Date finishDate = null;

	@OneToOne
	@NotNull
	private Room room;

	public ChatConsultation(IntitaConsultation cons, Room room) {
		this.intitaConsultation = cons;
		this.room = room;
	}
	public ChatConsultation()
	{
		
	}

	/*
	 * GET/SET
	 */

	public IntitaConsultation getIntitaConsultation() {
		return intitaConsultation;
	}

	public void setIntitaConsultation(IntitaConsultation intita) {
		this.intitaConsultation = intita;
	}

	public Date getStartDate() {
		return startDate;
	}

	public void setStartDate(Date startDate) {
		this.startDate = startDate;
	}

	public Date getFinishDate() {
		return finishDate;
	}

	public void setFinishDate(Date finishDate) {
		this.finishDate = finishDate;
	}

	public Room getRoom() {
		return room;
	}

	public void setRoom(Room room) {
		this.room = room;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}
	/*
	 * (non-Javadoc)
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 * Reload
	 */
	@Override
	public int compareTo(ChatConsultation arg0) {
		if (arg0==null)return -1;
		return this.getId().compareTo(arg0.getId());
	}


}
