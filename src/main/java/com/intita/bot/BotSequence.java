package com.intita.bot;

import java.util.ArrayList;

import com.intita.bot.groups.BotItemContainer;

public class BotSequence {
ArrayList<BotItemContainer> elements;
public BotSequence(){
	elements = new ArrayList<BotItemContainer>(); 
}

public ArrayList<BotItemContainer> getElements() {
	return elements;
}

public void setElements(ArrayList<BotItemContainer> elements) {
	this.elements = elements;
}
public void addElement(BotItemContainer element){
	this.elements.add(element);
}
}
