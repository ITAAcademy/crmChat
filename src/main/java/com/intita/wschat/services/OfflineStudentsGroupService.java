package com.intita.wschat.services;

import java.util.ArrayList;
import java.util.Set;

import javax.annotation.PostConstruct;
import javax.persistence.EntityExistsException;
import javax.transaction.Transactional;

import com.intita.wschat.domain.ChatRoomType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.intita.wschat.models.ChatUser;
import com.intita.wschat.models.OfflineGroup;
import com.intita.wschat.models.OfflineSubGroup;
import com.intita.wschat.models.Room;
import com.intita.wschat.models.RoomPermissions;
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
	@Autowired RoomPermissionsService roomPermissionsService;

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
	public boolean updateSubGroupRoom(OfflineSubGroup subGroup, boolean withGroup) {
		Room room = subGroup.getChatRoom();
		ChatUser author = chatUsersService.getChatUserFromIntitaId((long) subGroup.getIdTrainer(), false);
		if (author == null)
			return false;

		if (room == null) {
			room = roomService.register(subGroup.getGroup().getName() + subGroup.getName(), author, ChatRoomType.STUDENTS_GROUP);
			subGroup.setChatRoom(room);
			offlineSubGroupRespository.save(subGroup);
		}
		room.setName(subGroup.getGroup().getName() + " - "  + subGroup.getName());
		roomControler.changeAuthor(author, room, false, author.getPrincipal() , true);

		ArrayList<Integer> list = offlineStudentRespository.getStudentsIdByIdSubGroup(subGroup.getId());
		ArrayList<ChatUser> chatUserList = new ArrayList<>();
		for (int i = 0; i < list.size(); i++) 
			chatUserList.add(chatUsersService.getChatUserFromIntitaId(list.get(i).longValue(), false));
		
		Set<ChatUser> roomUserList = room.getUsers();
		for(ChatUser user : roomUserList)
		{
			Integer permissionBitSetPrimitive = roomPermissionsService.getPermissionsOfUser(room, user);
			boolean ignore = RoomPermissions.Permission.INVITED_USER
					.checkNumberForThisPermission(permissionBitSetPrimitive);
			if(ignore)
				chatUserList.add(user);
			
		}
		
		roomService.replaceUsersInRoom(room, chatUserList);
		if(withGroup)
			updateGroupRoom(subGroup.getGroup(), false);
		return true;
	}

	public void updateGroupRoom(OfflineGroup group, boolean withSubGroups) {
		Room room = group.getChatRoom();
		ChatUser author = chatUsersService.getChatUserFromIntitaId((long) group.getIdUserCreated(), false);
		if (author == null)
			return;
		
		if (room == null) {
			room = roomService.register(group.getName(), author, ChatRoomType.STUDENTS_GROUP);
			group.setChatRoom(room);
			offlineGroupRespository.save(group);
		}
		room.setName(group.getName());
		roomControler.changeAuthor(author, room, false, author.getPrincipal() , true);
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
		
		Set<ChatUser> roomUserList = room.getUsers();
		for(ChatUser user : roomUserList)
		{
			Integer permissionBitSetPrimitive = roomPermissionsService.getPermissionsOfUser(room, user);
			boolean ignore = RoomPermissions.Permission.INVITED_USER
					.checkNumberForThisPermission(permissionBitSetPrimitive);
			if(ignore)
				chatUserList.add(user);
			
		}
		
		
		roomService.replaceUsersInRoom(room, chatUserList);
		if(withSubGroups)
		{
			for (OfflineSubGroup subGroup : subGroups) {
				updateSubGroupRoom(subGroup, false);
			}
		}

	}
}
