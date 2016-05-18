package com.intita.wschat.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.intita.wschat.models.BotCategory;
import com.intita.wschat.models.BotDialogItem;
import com.intita.wschat.models.LangId;
import com.intita.wschat.repositories.BotItemContainerRepository;
import com.intita.wschat.web.ChatController;

@Service
@Transactional
public class BotItemContainerService {
	 int ID_GENERATION_ATTEMPTION = 4;
	java.util.Random randomized = new java.util.Random();
	@Autowired
	BotItemContainerRepository botItemContainerRepository;
	
	public BotDialogItem getByObjectId(LangId idObject){
		return botItemContainerRepository.findByIdObject(idObject);
	}
	public Long getNextId(){
		for (int i = 0; i < ID_GENERATION_ATTEMPTION; i++){
		Long nextLong = randomized.nextLong();
		if (getById(nextLong)==null)
		return nextLong;
		}
		return null;
	}
	public BotDialogItem getById(Long id){
		return botItemContainerRepository.findByIdObject(new LangId(id,ChatController.getCurrentLang()));
	}
	public BotDialogItem add(BotDialogItem itemContainer){
		return botItemContainerRepository.save(itemContainer);
	}
	public BotDialogItem update(BotDialogItem itemContainer){
		return botItemContainerRepository.save(itemContainer);
	}
}
