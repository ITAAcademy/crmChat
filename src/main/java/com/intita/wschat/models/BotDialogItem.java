package com.intita.wschat.models;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Transient;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.intita.wschat.services.BotItemContainerService;
import com.intita.wschat.web.ChatController;


@Entity
public class BotDialogItem {
	public BotCategory getCategory() {
		return category;
	}

	public void setCategory(BotCategory category) {
		this.category = category;
	}

	public BotDialogItem(){

	}
	
	public BotDialogItem(BotDialogItem item){
		this.body = item.body;
		this.category = item.category;
		this.idObject = item.idObject;
		this.testCase = item.testCase;
		this.description = item.description;
	}
	
	private final static Logger log = LoggerFactory.getLogger(ChatController.class);
	@Autowired
	@Transient
	BotItemContainerService botItemContainerService;
	
	@EmbeddedId
	private LangId idObject;
	
	private String body;
	@ManyToOne
	private BotCategory category;

	@Column(name="test_case", columnDefinition = "TEXT")
	String testCase = new String();
	
	@Column(columnDefinition = "TEXT")
	String description;
	
	@JsonIgnore
	@OneToMany(fetch = FetchType.LAZY, mappedBy = "item")
	List<BotAnswer> botAnswers = new ArrayList<BotAnswer>();
	
	public  BotDialogItem(String body,BotCategory category,Long id,String lang){
		this.body=body;
		this.category = category;
		this.idObject = new LangId(id,lang);
	}

	public void setTestCase(String str){
		this.testCase = str;
	}

	public String getBody() {
		return body;
	}
	public void setBody(String body) {
		this.body = body;
	}
	public LangId getIdObject() {
		return idObject;
	}
	public void setIdObject(LangId id) {
		this.idObject = id;
	}
	
	public String getTestCase() {
		/*ObjectMapper objectMapper = new ObjectMapper();
		HashMap<Integer, Long> conditionalMap = null;
		try {
			conditionalMap = objectMapper.readValue(testCase, new TypeReference<Map<Integer, Long>>(){} );
		} catch (IOException e) {
			// TODO Auto-generated catch block
			log.info("conditionalTransitions is empty");
			return null;

		}
		return conditionalMap;*/
		return testCase;
	}
	public void setConditionalTransitionsMap(HashMap<Integer, Long> map){
		ObjectMapper objectMapper = new ObjectMapper();
		try {
			setConditionalTransitions(objectMapper.writeValueAsString(map));
		} catch (JsonProcessingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	public void setConditionalTransitions(String conditionalTransitions) {
		this.testCase = conditionalTransitions;
	}
	public static BotDialogItem createFromCategories(ArrayList<BotCategory> categories){
		BotDialogItem container = new BotDialogItem();
		//container.setId((long) -1);
		String itemTemplate = "<div botlink=\"\" href=\" \" ispost=\"true\" classes=\"'btn btn-default'\" linkindex=\"%d\" text=\"'%s'\"> </div><br>";
		String body  = "";
		for (BotCategory category : categories){
			String categoryName = category.getName();
			Long mainContainerId = category.getMainElement().getIdObject().getId();
			body += String.format(itemTemplate,category.getId(),mainContainerId,categoryName );
		}
		log.info("body:"+body);
		container.setBody(body);
		return container;
	}

}
