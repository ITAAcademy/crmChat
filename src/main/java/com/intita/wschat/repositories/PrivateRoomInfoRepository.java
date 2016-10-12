package com.intita.wschat.repositories;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import com.intita.wschat.models.ChatUser;
import com.intita.wschat.models.PrivateRoomInfo;
import com.intita.wschat.models.Room;
import com.intita.wschat.models.User;


@Qualifier("IntitaConf") 
public interface PrivateRoomInfoRepository extends CrudRepository<PrivateRoomInfo, Long> {
	Page<PrivateRoomInfo> findById(Long id, Pageable pageable);
	
	@Query("select info from chat_private_rooms info where (info.firtsUser = ?1 and info.secondUser = ?2) or (info.firtsUser = ?2 and info.secondUser = ?1)")
	PrivateRoomInfo getByUsers(ChatUser first, ChatUser last);
	PrivateRoomInfo findByRoom(Room room);
	
	@Query("select info from chat_private_rooms info where (info.firtsUser = ?1 or info.secondUser = ?1)")
	ArrayList<PrivateRoomInfo> getByUser(ChatUser user);
	@Query("select info.room from chat_private_rooms info where (info.firtsUser = ?1 or info.secondUser = ?1)")
	ArrayList<Room> getRoomsByUser(ChatUser user);
}