package com.wschat.repositories;

import java.util.ArrayList;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.CrudRepository;

import com.wschat.models.Room;
import com.wschat.models.User;
import com.wschat.models.UserMessage;


public interface UserMessageRepository  extends CrudRepository<UserMessage, Long>{
	  Page<UserMessage> findById(Long id, Pageable pageable);
	  ArrayList<UserMessage> findAll(Pageable pageable);
	  ArrayList<UserMessage> findByAuthor(User author);
	  ArrayList<UserMessage> findByRoom(Room room);
}
