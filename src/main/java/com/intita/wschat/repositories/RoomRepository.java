package com.intita.wschat.repositories;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import com.intita.wschat.models.ChatUser;
import com.intita.wschat.models.Room;
import com.intita.wschat.models.User;


@Qualifier("IntitaConf") 
public interface RoomRepository extends CrudRepository<Room, Long> {
	Room findByName(String name);
	//User findByEmail(String email);
	Page<Room> findById(Long id, Pageable pageable);
	Room findById(Long id);
	
	Page<Room> findAll(Pageable pageable);
	List<Room> findFirst10ByIdNotIn(List<Long> users);
	
	Room findByAuthorAndTypeAndUsersContaining(ChatUser author, short type, ChatUser privateUser);
	ArrayList<Room> findByAuthor(ChatUser author);
	
	ArrayList<Room> findFirst10ByNameLike(String like);
	
	@Query("select r from ChatRoom r where r.id in ?1")
	ArrayList<Room> findRoomsByIds(ArrayList<Long> ids);

	@Modifying(clearAutomatically = true)
	@Query("update ChatRoom r set r.name = ?2 where r.id = ?1")
	void setRoomName(Long roomId, String roomName);
}