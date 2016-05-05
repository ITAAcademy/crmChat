package com.intita.wschat.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.intita.wschat.models.BotCategory;
import com.intita.wschat.models.BotDialogItem;
import com.intita.wschat.repositories.BotItemContainerRepository;

@Service
@Transactional
public class BotItemContainerService {

	@Autowired
	BotItemContainerRepository botItemContainerRepository;
	
	public BotDialogItem getById(Long id){
		return botItemContainerRepository.findOne(id);
	}
	public BotDialogItem add(BotDialogItem itemContainer){
		return botItemContainerRepository.save(itemContainer);
	}
	public BotDialogItem update(BotDialogItem itemContainer){
		return botItemContainerRepository.save(itemContainer);
	}
}
