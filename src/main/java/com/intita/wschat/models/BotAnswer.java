package com.intita.wschat.models;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Null;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonView;

import jsonview.Views;

/**
 * 
 * @author Nicolas Haiduchok
 */

@Entity
public class BotAnswer {
	@Id
	@GeneratedValue
	@JsonView(Views.Public.class)
	private Long id;

	@NotNull
	@JsonView(Views.Public.class)
	private String name;
	
	@ManyToOne
	@NotNull
	@JsonIgnore
	private BotDialogItem item;
	
	@ManyToOne
	@JsonIgnore
	@Null
	private Room room;
	
	@Column(columnDefinition = "TEXT")
	@NotNull
	@JsonView(Views.Public.class)
	private String value;
	
	
	public BotAnswer() {
		// TODO Auto-generated constructor stub
	}
	public BotAnswer(String name, BotDialogItem item, Room room, String value){
		this.name= name;
		this.item = item;
		this.room = room;
		this.value = value;
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
	public BotDialogItem getItem() {
		return item;
	}
	public void setItem(BotDialogItem item) {
		this.item = item;
	}
	public Room getRoom() {
		return room;
	}
	public void setRoom(Room room) {
		this.room = room;
	}
	public String getValue() {
		return value;
	}
	public void setValue(String value) {
		this.value = value;
	}
	
	
}
