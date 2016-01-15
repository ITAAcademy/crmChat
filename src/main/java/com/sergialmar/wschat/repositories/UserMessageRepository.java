package com.sergialmar.wschat.repositories;

import java.util.ArrayList;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.CrudRepository;

import com.sergialmar.wschat.models.Room;
import com.sergialmar.wschat.models.User;
import com.sergialmar.wschat.models.UserMessage;


public interface UserMessageRepository  extends CrudRepository<UserMessage, Long>{
	  Page<UserMessage> findById(Long id, Pageable pageable);
	  ArrayList<UserMessage> findAll(Pageable pageable);
	  ArrayList<UserMessage> findByAuthor(User author);
	  ArrayList<UserMessage> findByRoom(Room room);
}
