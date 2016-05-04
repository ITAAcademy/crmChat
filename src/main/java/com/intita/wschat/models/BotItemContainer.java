package com.intita.wschat.models;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.Transient;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.intita.wschat.services.BotItemContainerService;
import com.intita.wschat.util.HtmlUtility;
import com.intita.wschat.web.ChatController;


@Entity
 public class BotItemContainer {
	public BotItemContainer(){
		
	}
	private final static Logger log = LoggerFactory.getLogger(ChatController.class);
	@Autowired
	@Transient
	BotItemContainerService botItemContainerService;
	 @Id
	 @GeneratedValue
	 private Long id;
	 
	private String body;
	@ManyToOne
	BotCategory category;
	
	public  BotItemContainer(String body,BotCategory category){
		this.body=body;
		this.category = category;
	}

public BotItemContainer getNextNode(int chosedItem){
	HashMap<Integer, Long> conditionalMap = getConditionalTransitionsMap();
	if(conditionalMap==null) return null;
	if (conditionalMap.containsKey(chosedItem))return botItemContainerService.getById(conditionalMap.get(chosedItem));
	return null;
}
public void setTransitions(String str){
	this.conditionalTransitions = str;
}

String conditionalTransitions="";

public String getBody() {
	return body;
}
public void setBody(String body) {
	this.body = body;
}
public Long getId() {
	return id;
}
public void setId(Long id) {
	this.id = id;
}
public void addBranch(int key, BotItemContainer destinationContainer){
	HashMap<Integer, Long> map = getConditionalTransitionsMap();
	if (map==null) map = new HashMap<Integer, Long>();
	map.put(key, destinationContainer.getId());
	setConditionalTransitionsMap(map);
}
public HashMap<Integer, Long> getConditionalTransitionsMap() {
	ObjectMapper objectMapper = new ObjectMapper();
	HashMap<Integer, Long> conditionalMap = null;
	try {
		conditionalMap = objectMapper.readValue(conditionalTransitions, new TypeReference<Map<Integer, Long>>(){} );
	} catch (IOException e) {
		// TODO Auto-generated catch block
		log.info("conditionalTransitions is empty");
		return null;
		
	}
	return conditionalMap;
}
public void setConditionalTransitionsMap(HashMap<Integer, Long> map){
	ObjectMapper objectMapper = new ObjectMapper();
	try {
		conditionalTransitions = objectMapper.writeValueAsString(map);
	} catch (JsonProcessingException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}
}
public void setConditionalTransitions(String conditionalTransitions) {
	this.conditionalTransitions = conditionalTransitions;
}
public static BotItemContainer createFromCategories(ArrayList<BotCategory> categories){
	BotItemContainer container = new BotItemContainer();
	String itemTemplate = "<div botlink=' ' href='%s' ispost='true' classes='btn btn-default' linkindex='%d' text='%s'> </div><br>";
	String body  = "";
	for (BotCategory category : categories){
		String categoryName = category.getName();
		Long mainContainerId = category.getMainElement().getId();
		body += String.format(itemTemplate,category.getId(),mainContainerId,categoryName );
		}
	log.info("body:"+body);
	container.setBody(body);
	return container;
}
}
