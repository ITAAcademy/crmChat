package com.intita.wschat.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.intita.wschat.models.BotItemContainer;
import com.intita.wschat.repositories.BotItemContainerRepository;

@Service
@Transactional
public class BotItemContainerService {

	@Autowired
	BotItemContainerRepository botItemContainerRepository;
	
	public BotItemContainer getById(Long id){
		return botItemContainerRepository.findOne(id);
	}
}
