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

import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * 
 * @author Nicolas Haiduchok
 */

@Entity
public class BotAnswer {
	@Id
	@GeneratedValue
	private Long id;

	@NotNull
	private String name;
	
	@ManyToOne
	@NotNull
	private BotDialogItem item;
	
	@ManyToOne
	@NotNull
	private Room room;
	
	@Column(columnDefinition = "TEXT")
	@NotNull
	private String value;
	
	
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
