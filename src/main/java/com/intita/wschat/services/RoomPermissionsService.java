package com.intita.wschat.services;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.intita.wschat.models.ChatUser;
import com.intita.wschat.models.Room;
import com.intita.wschat.models.RoomPermissions;
import com.intita.wschat.repositories.RoomPermissionsRepository;

@Service
public class RoomPermissionsService {
	@Autowired RoomPermissionsRepository roomPermissionsRepository;

	@Transactional
	public void addPermissionsToUser(Room room, ChatUser chatUser, int permissions){
		RoomPermissions roomPermissions = new RoomPermissions(room,chatUser,permissions);
		roomPermissionsRepository.save(roomPermissions);
	}

	public Integer getPermissionsOfUser(Room room, ChatUser user)
	{
		List<RoomPermissions> userPermitionsList = roomPermissionsRepository.getAllByRoomAndUser(room, user);
		int permitions = 0;
		for (RoomPermissions permition : userPermitionsList){
			if (permition.isActual())
				permitions |= permition.getPermissions();
		}
		return permitions;
	}
	
	public boolean removePermissionsOfUser(Room room, ChatUser user)
	{
		List<RoomPermissions> userPermitionsList = roomPermissionsRepository.getAllByRoomAndUser(room, user);
		if(userPermitionsList.isEmpty())
			return true;
		
		roomPermissionsRepository.delete(userPermitionsList);
		return true;
	}
}
