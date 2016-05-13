package com.intita.wschat.models;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;

import com.fasterxml.jackson.annotation.JsonIgnore;

@Entity
public class BotCategory {
	@Id
	@GeneratedValue
	private Long id;

	private String name;

	@OneToOne
	@JsonIgnore
	private BotDialogItem mainElement;
	
	@JsonIgnore
	@OneToMany(fetch = FetchType.LAZY, mappedBy = "category")
	List<BotDialogItem> elements = new ArrayList<BotDialogItem>();

	public BotCategory(){

	}
	public BotCategory(String name){
		this.name = name;	
	}

	public List<BotDialogItem> getElements() {
		return elements;
	}

	public void setElements(ArrayList<BotDialogItem> elements) {
		this.elements = elements;
	}
	public void addElement(BotDialogItem element){
		this.elements.add(element);
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
	public BotDialogItem getMainElement() {
		return mainElement;
	}
	public void setMainElement(BotDialogItem mainElement) {
		this.mainElement = mainElement;
	}
}
