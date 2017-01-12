package com.intita.wschat.services;

import java.util.ArrayList;
import java.util.Set;

import javax.annotation.PostConstruct;
import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.intita.wschat.models.ChatUser;
import com.intita.wschat.models.OfflineGroup;
import com.intita.wschat.models.OfflineSubGroup;
import com.intita.wschat.models.Room;
import com.intita.wschat.repositories.OfflineGroupRespository;
import com.intita.wschat.repositories.OfflineStudentRespository;
import com.intita.wschat.repositories.OfflineSubGroupRespository;
import com.intita.wschat.web.RoomController;

@Service
public class OfflineStudentsGroupService {

	@Autowired
	OfflineGroupRespository offlineGroupRespository;
	@Autowired
	OfflineSubGroupRespository offlineSubGroupRespository;
	@Autowired
	OfflineStudentRespository offlineStudentRespository;
	@Autowired
	ChatUsersService chatUsersService;
	@Autowired
	RoomsService roomService;
	@Autowired
	RoomController roomControler;

	@PostConstruct
	private void postFunction() {
		updateGroupRoom(1);
	}

	@Transactional
	public void updateGroupRoom(Integer groupId) {
		OfflineSubGroup group = offlineSubGroupRespository.findOne(groupId);
		if (group == null)
			return;
		Room room = group.getChatRoom();
		if (room == null) {
			ChatUser author = chatUsersService.getChatUserFromIntitaId((long) group.getIdUserCreated(), false);
			if (author == null)
				return;
			room = roomService.register(group.getName(), author, Room.RoomType.STUDENTS_GROUP);
			group.setChatRoom(room);
			offlineSubGroupRespository.save(group);
		}
		ArrayList<Integer> list = offlineStudentRespository.getStudentsIdByIdSubGroup(groupId);
		ArrayList<ChatUser> chatUserList = new ArrayList<>();
		Set<ChatUser> roomUserList = room.getUsers();

		for (int i = 0; i < list.size(); i++) {
			Long l = list.get(i).longValue();
			chatUserList.add(chatUsersService.getChatUserFromIntitaId(l, false));
		}
		ArrayList<ChatUser> add = new ArrayList<>(chatUserList);
		add.removeAll(roomUserList);
		for (ChatUser chatUser : add) {
			roomService.addUserToRoom(chatUser, room);
		}

		ArrayList<ChatUser> remove = new ArrayList<>(roomUserList);
		remove.removeAll(chatUserList);
		for (ChatUser chatUser : remove) {
			roomService.removeUserFromRoom(chatUser, room);
		}

	}
}
