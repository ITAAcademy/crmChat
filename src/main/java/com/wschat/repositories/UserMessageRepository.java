package com.wschat.repositories;

import java.util.ArrayList;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.CrudRepository;

import com.wschat.models.Room;
import com.wschat.models.User;
import com.wschat.models.UserMessage;

@Qualifier("IntitaConf") 
public interface UserMessageRepository  extends JpaRepository<UserMessage, Long>{
	  Page<UserMessage> findById(Long id, Pageable pageable);
	  Page<UserMessage> findAll(Pageable pageable);
	  ArrayList<UserMessage> findByAuthor(User author);
	  ArrayList<UserMessage> findByRoom(Room room);
}
