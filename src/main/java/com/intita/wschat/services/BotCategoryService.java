package com.intita.wschat.services;

import java.util.ArrayList;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.intita.wschat.models.BotCategory;
import com.intita.wschat.repositories.BotCategoryRepository;

@Service
@Transactional
public class BotCategoryService {

	@Autowired
	BotCategoryRepository botCategoryRepository;
	
	public BotCategory add(BotCategory sequence){
		return botCategoryRepository.save(sequence);
	}
	public void update(BotCategory update){
		botCategoryRepository.save(update);
	}
	public Long getCount(){
		return botCategoryRepository.count();
	}
	public ArrayList<BotCategory> getAll(){
		return botCategoryRepository.findAll();
	}
	
	
}
