package com.intita.wschat.services;

import java.util.ArrayList;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
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
	@Autowired private BotItemContainerRepository botItemContainerRepository;
	@Autowired private ChatLangService chatLangService;
	@Autowired private BotCategoryService botCategoryService;
	
	public BotDialogItem getByObjectId(LangId idObject){
		return botItemContainerRepository.findByIdAndLang(idObject.getId(), idObject.getLang());
	}
	public Long getNextId(){
		return getLastId()+1L;
	}
	public BotDialogItem getById(Long id){
		return botItemContainerRepository.findByIdAndLang(id, chatLangService.getCurrentLang());
	}
	public BotDialogItem getByIdAndLang(Long id, String lang){
		return botItemContainerRepository.findByIdAndLang(id, lang);
	}
	public BotDialogItem add(BotDialogItem itemContainer){
		return botItemContainerRepository.save(itemContainer);
	}
	public BotDialogItem update(BotDialogItem itemContainer){
		return botItemContainerRepository.save(itemContainer);
	}
	public ArrayList<Long> getAllIds(){
		return botItemContainerRepository.getAllIds();
	}
//	public ArrayList<Long> getAllIdsFromCategory(BotCategory category){
//		return botItemContainerRepository.getAllIdsFromCategory(category);
//	}
	public ArrayList<Long> getAllIdsFromCategory(Long categoryId){
		return botItemContainerRepository.getAllIdsFromCategory(categoryId);
	}
	
	public ArrayList<String> getFirstDescriptionsLike(String description,int limit){
		return botItemContainerRepository.getDescriptionsLike(description,new PageRequest(0,limit));
	}
	public ArrayList<String> getFirst5DescriptionsLike(String description){
		return getFirstDescriptionsLike(description,5);
	}
	public ArrayList<Long> getIdsWhereDescriptionsLike(String name){
		return botItemContainerRepository.getIdsWhereDescriptionsLike(name);
	}
	
	public ArrayList<String> getFirstDescriptionsLike(String description,Long categoryId,int limit){
		return botItemContainerRepository.getDescriptionsLike(description,categoryId,new PageRequest(0,limit));
	}
	public ArrayList<String> getFirst5DescriptionsLike(String description,Long categoryId){
		return getFirstDescriptionsLike(description,categoryId,5);
	}
	public ArrayList<Long> getIdsWhereDescriptionsLike(String description,Long categoryId){
		return botItemContainerRepository.getIdsWhereDescriptionsLike(description,categoryId);
	}
	public Long getLastId(){
		ArrayList<Long> ids = botItemContainerRepository.getLastIds(new PageRequest(0,1));
		if (ids.size()<=0) return -1L;
		return ids.get(0);
	}
	public ArrayList<BotDialogItem> getBotDialogItemsHavingDescription(String name,Long categoryId,int limit){
		return botItemContainerRepository.getBotDialogItemsHavingDescription(name,categoryId,new PageRequest(0,limit));
	}
	public ArrayList<BotDialogItem> getBotDialogItems(Long categoryId,int limit){
		return botItemContainerRepository.findByCategoryId(categoryId,new PageRequest(0,limit));
	}

	public String getJsonContainerBodySimple(String[] itemsText){
		String res = "{";
		for (int i = 0 ; i < itemsText.length;i++){
			if(i!=0)res += ",";
			res += String.format("item%d:{'data':'%s'}",i,itemsText[i]);
		}
		res+="}";
		return res;
	}

	public void generateTestSequnce(BotCategory botCategory){
		String[] container1 = {"Variant1,Variant2,Variant3,Variant4"};
		BotDialogItem testItemContainer1 = add(new BotDialogItem(getJsonContainerBodySimple(container1),botCategory,1L,"ua"));//begin
		BotDialogItem testItemContainer2 = add(new BotDialogItem(getJsonContainerBodySimple(container1),botCategory,2L,"ua"));
		BotDialogItem testItemContainer3 = add(new BotDialogItem(getJsonContainerBodySimple(container1),botCategory,3L,"ua"));
		BotDialogItem testItemContainer4 = add(new BotDialogItem(getJsonContainerBodySimple(container1),botCategory,4L,"ua"));//end
		update(testItemContainer1);
		update(testItemContainer2);
		update(testItemContainer3);
		update(testItemContainer4);
		botCategory.setMainElement(testItemContainer1);
		botCategoryService.update(botCategory);
		update(testItemContainer1);
	}
}
