package com.intita.wschat.services;

import java.util.ArrayList;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.intita.wschat.models.BotAnswer;
import com.intita.wschat.models.BotCategory;
import com.intita.wschat.repositories.BotAnswersRespository;
import com.intita.wschat.repositories.BotCategoryRepository;
import com.intita.wschat.repositories.BotItemContainerRepository;

@Service
@Transactional
public class BotAnswersService {

	@Autowired	BotAnswersRespository botAnswersRespository;
	
	@Autowired	BotCategoryService botCategoryService;
	@Autowired	BotItemContainerService botItemContainerService;
	@Autowired	RoomsService chatRoomService;
	
	
	public ArrayList<BotAnswer> getAll()
	{
		return botAnswersRespository.findAll();
	}
}
