package com.intita.wschat.repositories;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.codehaus.groovy.control.Phases;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.Phased;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.CrudRepository;

import com.intita.wschat.models.ChatUser;
import com.intita.wschat.models.Lang;
import com.intita.wschat.models.Phrase;
import com.intita.wschat.models.Room;
import com.intita.wschat.models.User;

@Qualifier("IntitaConf") 
public interface ChatPhrasesRepository extends CrudRepository<Phrase, Long> {
	ArrayList<Phrase> findAll();
}
