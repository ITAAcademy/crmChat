package com.intita.wschat.repositories;

import java.util.List;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import com.intita.wschat.models.ChatUser;
import com.intita.wschat.models.Lang;
import com.intita.wschat.models.Room;
import com.intita.wschat.models.User;

@Qualifier("IntitaConf") 
public interface ChatLangRepository extends CrudRepository<Lang, Long> {
    @Modifying
    @Query("update chat_lang langRecord set langRecord.map = ?1 where langRecord.lang = ?2")
    public void updateMap(String map,String lang);
}
