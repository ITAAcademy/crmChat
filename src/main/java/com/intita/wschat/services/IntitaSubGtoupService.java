package com.intita.wschat.services;

import java.math.BigInteger;
import java.util.ArrayList;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.intita.wschat.models.ChatUser;
import com.intita.wschat.models.IntitaSubGroup;
import com.intita.wschat.models.Room;
import com.intita.wschat.models.User;
import com.intita.wschat.repositories.IntitaSubGroupRespository;

@Service
public class IntitaSubGtoupService {

	@Autowired
	private IntitaSubGroupRespository intitaSubGroupRespository;
	@Autowired 	private UsersService userService;
	@Autowired 	private ChatUsersService chatUserService;
	@Autowired private RoomsService roomsService;

	@PostConstruct
	@Transactional
	public void postConstruct() {

	}

	@Transactional
	public ArrayList<IntitaSubGroup> getAll(){
		return intitaSubGroupRespository.findAll();
	}
	
	@Transactional
	public ArrayList<User> getIStudentsByGroup(IntitaSubGroup group){
		ArrayList<Long> ids = intitaSubGroupRespository.getStudentsBySubGroupId(group.getId());
		ArrayList<User> students = null;
		for (Long id : ids) {
			User student = userService.getById(id);
			if(student != null)
				students.add(student);
		}
		return students;
	}
	
	@Transactional
	public ArrayList<ChatUser> getCStudentsByGroup(IntitaSubGroup group){
		ArrayList<User> iStudents = getIStudentsByGroup(group);
		ArrayList<ChatUser> students = null;
		for (User student : iStudents) {
			if(student != null)
				students.add(chatUserService.getChatUserFromIntitaUser(student, false));
		}
		return students;
	}
	@Transactional
	public ArrayList<ChatUser> getTrainerStudents(Long trainerUserId){
		ArrayList<Integer> studentsOfTrainer = intitaSubGroupRespository.getStudentsByTrainer(trainerUserId.intValue());
		ArrayList<Long> studentLong = new ArrayList<Long>();
		for (Integer studentId : studentsOfTrainer){
			studentLong.add(studentId.longValue());
		}
		ArrayList<Long> students = userService.getUsersIds(studentLong);
		if (students.isEmpty())return new ArrayList<ChatUser>();
		else
		return chatUserService.getChatUsersFromIntitaIds(students);	
	}
	@Transactional
	public ArrayList<Room> getTrainerGroupRooms(Long trainerUserId){
		ArrayList<BigInteger> roomsOfTrainer = intitaSubGroupRespository.getRoomsByTrainer(trainerUserId.intValue());
		ArrayList<Long> roomsLong = new ArrayList<Long>();
		for (BigInteger roomId : roomsOfTrainer){
			if (roomId!=null)
			roomsLong.add(roomId.longValue());
		}
		if (roomsLong.isEmpty())
			return new ArrayList<Room>();
		else
		return roomsService.getRoomsByIds( roomsLong );
	}
	
 

}
