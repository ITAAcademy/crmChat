package com.intita.wschat.services;

import java.util.ArrayList;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
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
	public BotCategory getById(Long id)
	{
		return botCategoryRepository.findOne(id);
	}
	public ArrayList<Long> getAllIds(){
		return botCategoryRepository.getAllIds();
	}
	public ArrayList<String> getFirstNamesLike(String name,int limit){
		return botCategoryRepository.getNamesLike(name,new PageRequest(0,limit));
	}
	public ArrayList<String> getFirst5NamesLike(String name){
		return getFirstNamesLike(name,5);
	}
	
	
}
