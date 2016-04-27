package com.intita.wschat.repositories;

import org.springframework.data.repository.CrudRepository;

import com.intita.wschat.models.BotSequence;
import com.intita.wschat.models.Lang;

public interface BotSequenceRepository extends CrudRepository<BotSequence, Long>{
}
