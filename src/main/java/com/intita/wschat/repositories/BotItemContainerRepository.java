package com.intita.wschat.repositories;

import org.springframework.data.repository.CrudRepository;

import com.intita.wschat.models.BotDialogItem;

public interface BotItemContainerRepository extends CrudRepository<BotDialogItem, Long> {

}
