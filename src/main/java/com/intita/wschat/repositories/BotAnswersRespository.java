package com.intita.wschat.repositories;

import java.util.ArrayList;

import org.springframework.data.repository.CrudRepository;

import com.intita.wschat.models.BotDialogItem;
import com.intita.wschat.models.BotAnswer;

public interface BotAnswersRespository extends CrudRepository<BotAnswer, Long> {

	ArrayList<BotAnswer> findAll();
}
