package com.intita.wschat.repositories;

import java.util.ArrayList;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import com.intita.wschat.models.BotCategory;
import com.intita.wschat.models.BotDialogItem;
import com.intita.wschat.models.LangId;

public interface BotItemContainerRepository extends CrudRepository<BotDialogItem, Long> {
BotDialogItem findByIdObject(LangId idObject);
@Query("select item.id from BotDialogItem item")
ArrayList<Long> getAllIds();
//@Query("select item.id.id from BotDialogItem item where item.category like ?1")
//ArrayList<Long> getAllIdsFromCategory(BotCategory category);
@Query("select distinct item.idObject.id from BotDialogItem item where item.category.id like ?1")
ArrayList<Long> getAllIdsFromCategory(Long categoryId);
}


