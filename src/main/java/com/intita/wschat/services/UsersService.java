package com.intita.wschat.services;

import java.security.Principal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.List;

import javax.annotation.PostConstruct;

import org.apache.commons.collections4.IteratorUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.authentication.encoding.ShaPasswordEncoder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.intita.wschat.models.ChatUser;
import com.intita.wschat.models.User;
import com.intita.wschat.models.User.Permissions;
import com.intita.wschat.repositories.UserRepository;

@Service
public class UsersService {

	@Autowired
	private UserRepository usersRepo;

	@Autowired
	private ChatUsersService chatUsersService;

	@PostConstruct
	@Transactional
	public void createAdminUser() {
		System.out.println("admin user created");
		//register("user", "user", "user");

	}

	@Transactional
	public Page<User> getUsers(int page, int pageSize){
		return usersRepo.findAll(new PageRequest(page-1, pageSize)); 

	}
	@Transactional
	public User getUser(Principal principal){
		String chatUserIdStr = principal.getName();
		Long chatUserId = 0L;
		try{
			chatUserId = Long.parseLong(chatUserIdStr);
		}
		catch(NumberFormatException e){
			System.out.println(e);
			return null;
		}
		User user = chatUsersService.getUsersFromChatUserId(chatUserId);
		return user;
	}
	@Transactional
	public List<User> getUsersFist5(String login, List<Long> logins){
		return  usersRepo.findFirst5ByLoginLikeCustom(login + "%", logins, new PageRequest(0, 5));
	}

	@Transactional
	public List<String> getUsersEmailsFist5(String login, List<Long> logins){
		List<User> users = getUsersFist5(login, logins);
		List<String> emails = new ArrayList<String>();
		for(int i = 0; i < users.size(); i++)
			emails.add(users.get(i).getEmail());
		return emails;

	}

	@Transactional
	public List<User> getUsersFist5(String login){
		return usersRepo.findFirst5ByLoginLikeOrFirstNameLikeOrSecondNameLike(login + "%",login + "%",login + "%");
	}
	@Transactional
	public List<String> getUsersEmailsFist5(String login){
		List<User> users = getUsersFist5(login);
		//System.out.println("FFFFFFFFFFFFFFFFFFF  " + login + " " + users);
		List<String> emails = new ArrayList<String>();
		for(int i = 0; i < users.size(); i++)
			emails.add(users.get(i).getEmail());
		return emails;

	}


	@Transactional
	public ArrayList<User> getUsers(){
		return (ArrayList<User>) IteratorUtils.toList(usersRepo.findAll().iterator()); 
	}
	@Transactional
	public User getUser(Long id){
		return usersRepo.findOne(id);
	}
	@Transactional
	public User getUserFromChat(Long chatUserId){
		ChatUser chatUser= chatUsersService.getChatUser(chatUserId);
		if (chatUser==null) return null;
		return chatUser.getIntitaUser();

	}

	@Transactional
	public User getUser(String login) {
		return usersRepo.findByLogin(login);
	}

	@Transactional(readOnly = false)
	public void register(String login, String email, String pass) {
		String passHash = new ShaPasswordEncoder().encodePassword(pass, null);
		//encode(pass);
		//String passHash = pass;

		User u = new User(login, email.toLowerCase(), passHash);


		usersRepo.save(u);
	}
	@Transactional(readOnly = false)
	public void register(String login, String email, String pass,Permissions permission) {
		String passHash = new BCryptPasswordEncoder().encode(pass);
		//String passHash = pass;
		User u = new User(login, email.toLowerCase(), passHash);
		u.setPermission(permission);

		usersRepo.save(u);
	}
	@Transactional(readOnly = false)
	public void togglePermissionById(Long id){
		User u = getById(id);
		u.togglePermission();
		usersRepo.save(u);
	}
	@Transactional
	public void updateUserInfo(User u){
		usersRepo.save(u);
	}

	@Transactional
	public void removeUser(Long id){
		usersRepo.delete(id);
	}
	@Transactional
	public User getById(Long id){
		return usersRepo.findFisrtById(id);
	}
	@Transactional
	public boolean isAdmin(Long id){
		if(usersRepo.findInAdminTable(id) != null)
			return true;
		return false;
	}
	@Transactional
	public boolean isTenant(Long id){
		if(usersRepo.findInTenantTable(id) != null)
			return true;
		return false;
	}
	@Transactional
	public boolean isTrainer(Long id){
		if(usersRepo.findInTrainerTable(id) != null)
			return true;
		return false;
	}
	@Transactional
	public boolean isStudent(Long id){
		if(usersRepo.findInStudentTable(id) != null)
			return true;
		return false;
	}
	@Transactional
	public User getTrainer(Long id){
		Long trainerId = usersRepo.getTrainerByUserId(id);
		if(trainerId != null)
			return usersRepo.findFisrtById(trainerId);
		return null;
	}
	@Transactional
	public ArrayList<User> getStudents(Long id){
		ArrayList<Long> studentsIdList = usersRepo.getStudentsByTeacherId(id);
		if(studentsIdList != null)
		{
			ArrayList<User> studentsList = new ArrayList<>();
			for (Long studentId : studentsIdList) {
				User student = usersRepo.findFisrtById(studentId);
				if(student == null)
				{
					System.out.println("NULL studen!!! Id = " + studentId);
					continue;
				}
				studentsList.add(student);
			}
			return studentsList;
		}
		return null;
	}
	/*
	@Transactional
	public boolean getAllTrainer(){
		ArrayList<Long> all = usersRepo.findAllTrainers(); 
		if(all != null)
			return true;
		return false;
	}
	*/
	@Transactional
	public ArrayList<ChatUser> getAllTenants(){
		ArrayList<ChatUser> result = new ArrayList<>();
		ArrayList<Long> all = new ArrayList<Long>(Arrays.asList(usersRepo.findAllTenants()));//WTF
		if(all == null)
			return result;
		return new ArrayList<>(chatUsersService.getUsers(all));
	}

}

