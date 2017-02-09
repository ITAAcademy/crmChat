package com.intita.wschat.services;

import java.util.ArrayList;
import java.util.Set;

import javax.annotation.PostConstruct;
import javax.persistence.EntityExistsException;
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
		
	}

	@Transactional
	public OfflineSubGroup getSubGroup(Integer subGroupId) {
		return offlineSubGroupRespository.findOne(subGroupId);
	}
	
	@Transactional
	public OfflineGroup getGroup(Integer groupId) {
		return offlineGroupRespository.findOne(groupId);
	}
	
	@Transactional()
	public void updateSubGroupRoom(OfflineSubGroup subGroup) {
		Room room = subGroup.getChatRoom();
		if (room == null) {
			ChatUser author = chatUsersService.getChatUserFromIntitaId((long) subGroup.getIdUserCreated(), false);
			if (author == null)
				return;
			
			room = roomService.register(subGroup.getGroup().getName() + subGroup.getName(), author, Room.RoomType.STUDENTS_GROUP);
			subGroup.setChatRoom(room);
			offlineSubGroupRespository.save(subGroup);
		}
		room.setName(subGroup.getGroup().getName() + " - "  + subGroup.getName());
		ArrayList<Integer> list = offlineStudentRespository.getStudentsIdByIdSubGroup(subGroup.getId());
		ArrayList<ChatUser> chatUserList = new ArrayList<>();
		for (int i = 0; i < list.size(); i++) 
			chatUserList.add(chatUsersService.getChatUserFromIntitaId(list.get(i).longValue(), false));
		roomService.replaceUsersInRoom(room, chatUserList);
	}

	public void updateGroupRoom(OfflineGroup group) {
		 Room room = group.getChatRoom();
		if (room == null) {
			ChatUser author = chatUsersService.getChatUserFromIntitaId((long) group.getIdUserCreated(), false);
			if (author == null)
				return;
			room = roomService.register(group.getName(), author, Room.RoomType.STUDENTS_GROUP);
			group.setChatRoom(room);
			offlineGroupRespository.save(group);
		}
		ArrayList<Integer> subGroupsId = new ArrayList<>();
		ArrayList<OfflineSubGroup> subGroups = new ArrayList<>(group.getSubGroups());
		for (OfflineSubGroup subGroup : subGroups) {
			subGroupsId.add(subGroup.getId());
		}
		if(subGroupsId.isEmpty())
			return;
		
		ArrayList<Integer> list = offlineStudentRespository.getStudentsIdByIdSubGroups(subGroupsId);
		ArrayList<ChatUser> chatUserList = new ArrayList<>();
		for (int i = 0; i < list.size(); i++) 
			chatUserList.add(chatUsersService.getChatUserFromIntitaId(list.get(i).longValue(), false));
		roomService.replaceUsersInRoom(room, chatUserList);
		
	}
}
