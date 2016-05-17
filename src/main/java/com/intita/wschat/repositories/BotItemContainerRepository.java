package com.intita.wschat.repositories;

import org.springframework.data.repository.CrudRepository;

import com.intita.wschat.models.BotDialogItem;
import com.intita.wschat.models.LangId;

public interface BotItemContainerRepository extends CrudRepository<BotDialogItem, Long> {
BotDialogItem findByIdObject(LangId idObject);
}
