package com.intita.bot.groups;

import java.util.ArrayList;
import java.util.HashMap;

import com.intita.bot.items.BotItem;

 public class BotItemContainer {
	Long id;
	ArrayList<BotItem> childrens;
	String body;

	public  BotItemContainer(String body){
		this.body=body;
	}

public BotItemContainer getNextNode(int chosedItem){
	if (conditionalTransitions.containsKey(chosedItem))return conditionalTransitions.get(chosedItem);
	return null;
}
public void addBranch(int value, BotItemContainer destinationContainer){
	conditionalTransitions.put(value, destinationContainer);
}
HashMap<Integer,BotItemContainer> conditionalTransitions;

BotItemContainer previousNode;

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
public ArrayList<BotItem> getChildrens() {
	return childrens;
}
public void setChildrens(ArrayList<BotItem> childrens) {
	this.childrens = childrens;
}
public HashMap<Integer, BotItemContainer> getConditionalTransitions() {
	return conditionalTransitions;
}
public void setConditionalTransitions(HashMap<Integer, BotItemContainer> conditionalTransitions) {
	this.conditionalTransitions = conditionalTransitions;
}
public BotItemContainer getPreviousNode() {
	return previousNode;
}
public void setPreviousNode(BotItemContainer previousNode) {
	this.previousNode = previousNode;
}
}
