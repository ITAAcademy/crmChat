package com.intita.wschat.repositories;

import java.util.ArrayList;

import org.springframework.data.repository.CrudRepository;

import com.intita.wschat.models.BotCategory;

public interface BotCategoryRepository extends CrudRepository<BotCategory, Long>{
	public ArrayList<BotCategory> findAll();
}
