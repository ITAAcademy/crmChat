package com.intita.wschat.web;

import org.springframework.stereotype.Controller;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.intita.bot.BotSequence;
import com.intita.bot.groups.BotItemContainer;
import com.intita.bot.items.BotItem;

@Service
@Controller
public class BotController {
	public String getJsonContainerBodySimple(){
		return "{item1:'',item2:'',item3:''}";
	}
	
	@RequestMapping(value = "/bot/getcontainer", method = RequestMethod.GET)
	@ResponseBody
	public String getSequence(Long id) throws JsonProcessingException {
		ObjectMapper objectMapper = new ObjectMapper();
		BotSequence botSequence = new BotSequence();
		BotItemContainer testItemContainer = new BotItemContainer(getJsonContainerBodySimple());
		botSequence.addElement(testItemContainer);
		return objectMapper.writeValueAsString(testItemContainer);
	}
}
