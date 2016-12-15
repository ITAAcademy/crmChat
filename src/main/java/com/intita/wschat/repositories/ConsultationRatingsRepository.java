package com.intita.wschat.repositories;

import java.util.List;
import java.util.Set;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.CrudRepository;

import com.intita.wschat.models.ChatUser;
import com.intita.wschat.models.ConsultationRatings;
import com.intita.wschat.models.Lang;
import com.intita.wschat.models.Room;
import com.intita.wschat.models.User;

@Qualifier("IntitaConf") 
public interface ConsultationRatingsRepository extends CrudRepository<ConsultationRatings, Long> {
	Set<ConsultationRatings> findAll();
}
