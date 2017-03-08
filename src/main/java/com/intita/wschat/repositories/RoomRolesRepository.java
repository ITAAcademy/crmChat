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
import com.intita.wschat.models.RoomPermissions;
import com.intita.wschat.models.RoomRoleInfo;
import com.intita.wschat.models.User;


@Qualifier("IntitaConf") 
public interface RoomRolesRepository extends CrudRepository<RoomRoleInfo, Long> {
	RoomRoleInfo findOneByRoleId(int role);
}