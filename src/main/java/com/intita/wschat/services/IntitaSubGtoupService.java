package com.intita.wschat.services;

import java.security.Principal;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Random;

import javax.annotation.PostConstruct;

import org.apache.commons.collections4.IteratorUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.authentication.encoding.ShaPasswordEncoder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.intita.wschat.event.LoginEvent;
import com.intita.wschat.models.ChatUser;
import com.intita.wschat.models.IntitaSubGroup;
import com.intita.wschat.models.Room;
import com.intita.wschat.models.User;
import com.intita.wschat.repositories.ChatUserRepository;
import com.intita.wschat.repositories.IntitaSubGroupRespository;

@Service
public class IntitaSubGtoupService {

	@Autowired
	private IntitaSubGroupRespository intitaSubGroupRespository;
	@Autowired 	private UsersService userService;
	@Autowired 	private ChatUsersService chatUserService;

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


}
