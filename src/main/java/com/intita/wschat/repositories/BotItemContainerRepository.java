package com.intita.wschat.repositories;

import java.util.ArrayList;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import com.intita.wschat.models.BotCategory;
import com.intita.wschat.models.BotDialogItem;
import com.intita.wschat.models.LangId;

public interface BotItemContainerRepository extends CrudRepository<BotDialogItem, Long> {
	@Query("select item from BotDialogItem item where item.id.id = ?1 and item.id.lang = ?2")
	BotDialogItem findByIdAndLang(Long id, String lang);
@Query("select item.id from BotDialogItem item")
ArrayList<Long> getAllIds();
//@Query("select item.id.id from BotDialogItem item where item.category like ?1")
//ArrayList<Long> getAllIdsFromCategory(BotCategory category);
@Query("select distinct item.idObject.id from BotDialogItem item where item.category.id like ?1")
ArrayList<Long> getAllIdsFromCategory(Long categoryId);

@Query("select item.description from BotDialogItem item where item.description like %?1%")
ArrayList<String> getDescriptionsLike(String description,Pageable pageable);
@Query("select distinct item.id.id from BotDialogItem item where item.description like %?1%")
ArrayList<Long> getIdsWhereDescriptionsLike(String description);

@Query("select item.description from BotDialogItem item where item.description like %?1% and item.category.id = ?2")
ArrayList<String> getDescriptionsLike(String description,Long categoryId,Pageable pageable);
@Query("select distinct item.id.id from BotDialogItem item where item.description like %?1% and item.category.id = ?2")
ArrayList<Long> getIdsWhereDescriptionsLike(String description,Long categoryId);

@Query("select distinct item.id.id from BotDialogItem item ORDER BY item.id.id DESC")
ArrayList<Long> getLastIds(Pageable pageable);

@Query("select item from BotDialogItem item where item.description like %?1% and item.category.id = ?2")
ArrayList<BotDialogItem> getBotDialogItemsHavingDescription(String description,Long categoryId,Pageable pageable);
}


