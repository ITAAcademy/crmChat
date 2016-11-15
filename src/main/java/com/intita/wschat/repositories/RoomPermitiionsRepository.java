package com.intita.wschat.repositories;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import com.intita.wschat.models.ChatUser;
import com.intita.wschat.models.Room;
import com.intita.wschat.models.RoomPermitions;
import com.intita.wschat.models.User;


@Qualifier("IntitaConf") 
public interface RoomPermitiionsRepository extends CrudRepository<RoomPermitions, Long> {
	@Query("select u from RoomPermitions u where u.room = ?1 and u.user = ?2 and (((u.start_date <= NOW() AND u.end_date >= NOW()) OR u.end_date IS NULL))")
	ArrayList<RoomPermitions> getAllByRoomAndUser(Room room, User user);
}