package com.intita.wschat.services;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.authentication.encoding.ShaPasswordEncoder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.intita.wschat.models.ChatUser;
import com.intita.wschat.models.Room;
import com.intita.wschat.models.User;
import com.intita.wschat.models.User.Permissions;
import com.intita.wschat.repositories.RoomRepository;
import com.intita.wschat.repositories.UserRepository;

import org.apache.commons.collections4.IteratorUtils;

@Service
public class RoomsService {
	
	@Autowired
	private RoomRepository roomRepo;
	@Autowired private UsersService userService;
	@Autowired private ChatUsersService chatUserService;
	
	@PostConstruct
	@Transactional
	public void createDefRoom() {
		
		/*userService.register("kaka", "kaka", "kaka");
		System.out.println("kaka created");
		User u = userService.getUser("kaka");
		if(u != null)
		register("Deffine", u);
		System.out.println("room created");*/
				//register("user", "user@mail.com", "qwerty",Permissions.PERMISSIONS_USER);

	}
	
	@Transactional
	   public Page<Room> getRooms(int page, int pageSize){
			return roomRepo.findAll(new PageRequest(page-1, pageSize)); // spring рахує сторінки з нуля
			
	   }
	@Transactional
	public ArrayList<Room> getRooms(){
		return (ArrayList<Room>) IteratorUtils.toList(roomRepo.findAll().iterator()); // spring рахує сторінки з нуля
	}
	@Transactional
	public Room getRoom(Long id){
		return roomRepo.findOne(id);
	}
	
	@Transactional
	public Room getRoom(String name) {
		return roomRepo.findByName(name);
	}
	public ArrayList<Room> getRoomByAuthor(String author) {
		
		return roomRepo.findByAuthor(userService.getUser(author));
	}
	
	@Transactional(readOnly = false)
	public Room register(String name, ChatUser author) {
		Room r = new Room();
		r.setAuthor(author);
		r.setName(name);
		roomRepo.save(r);
		return r;
	}
	@Transactional(readOnly = false)
	public boolean unRegister(String name, ChatUser author) {
		Room room = roomRepo.findByName(name);
		if(!author.getRootRooms().contains(room))
			return false;
		room.setActive(false);
		roomRepo.save(room);//@NEED_ASK@
		return true;
	}
	
	@Transactional(readOnly = false)
	public boolean addUserToRoom(Long id, User user) {
		Room room = roomRepo.findOne(id);
		addUserToRoom(chatUserService.getChatUser(id), room);		
		return true;
	}
	@Transactional(readOnly = false)
	public boolean update(Room room){
		roomRepo.save(room);
		return true;
	}

	@Transactional(readOnly = false)
	public boolean addUserByNameToRoom(Long id, String name) {
		Room room = roomRepo.findOne(id);
		return addUserToRoom(chatUserService.getChatUser(name), room);
	}
	
	@Transactional(readOnly = false)
	public boolean addUserToRoom(ChatUser user, Room room) {
		if(room == null)
			return false;
		if(user == null)
			return false;
		//have premition?
	
		room.addUser(user);
		roomRepo.save(room);
		return true;
	}
	
	@Transactional(readOnly = false)
	public boolean removeUserFromRoom(User user, Room room) {
		if(room == null)
			return false;
		if(user == null)
			return false;
		//have premition?
	
		room.removeUser(user);
		roomRepo.save(room);
		return true;
	}
}

