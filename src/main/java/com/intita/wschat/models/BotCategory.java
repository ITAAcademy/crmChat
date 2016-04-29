package com.intita.wschat.models;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.OneToMany;

@Entity
public class BotCategory {
@OneToMany(fetch = FetchType.LAZY, mappedBy = "category")
List<BotItemContainer> elements;
@Id
@GeneratedValue
private Long id;

private String name;

public BotCategory(){
	elements = new ArrayList<BotItemContainer>(); 
}

public List<BotItemContainer> getElements() {
	return elements;
}

public void setElements(ArrayList<BotItemContainer> elements) {
	this.elements = elements;
}
public void addElement(BotItemContainer element){
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
}
