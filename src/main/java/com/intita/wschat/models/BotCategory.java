package com.intita.wschat.models;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;

import com.fasterxml.jackson.annotation.JsonIgnore;

@Entity
public class BotCategory implements java.io.Serializable {
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

	@ManyToMany(mappedBy = "botCategories", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
	Set<ChatTenant> chatUsers  = new HashSet<>();

	public Set<ChatTenant> getChatUsers() {
		return chatUsers;
	}
	public void setChatUsers(Set<ChatTenant> chatUsers) {
		this.chatUsers = chatUsers;
	}
	
	



}
