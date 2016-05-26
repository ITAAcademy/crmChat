package com.intita.wschat.repositories;

import java.util.ArrayList;
import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import com.intita.wschat.models.BotCategory;

public interface BotCategoryRepository extends CrudRepository<BotCategory, Long>{
	public ArrayList<BotCategory> findAll();
	@Query("select c.id from BotCategory c")
	ArrayList<Long> getAllIds();
	@Query("select c.name from BotCategory c where c.name like %?1%")
	ArrayList<String> getNamesLike(String name,Pageable pageable);
}
