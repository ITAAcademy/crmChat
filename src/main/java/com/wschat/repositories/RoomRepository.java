package com.wschat.repositories;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.CrudRepository;

import com.wschat.models.Room;
import com.wschat.models.User;


@Qualifier("IntitaConf") 
public interface RoomRepository extends CrudRepository<Room, Long> {
	Room findByName(String name);
  //User findByEmail(String email);
  Page<Room> findById(Long id, Pageable pageable);
  Page<Room> findAll(Pageable pageable);
  List<Room> findFirst10ByIdNotIn(List<Long> users);
  ArrayList<Room> findByAuthor(User author);
}