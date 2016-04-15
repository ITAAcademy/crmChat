package com.intita.wschat.services;

import java.security.Principal;
import java.util.ArrayList;
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
	public List<String> getUsersEmailsFist5(String login, List<Long> logins){
		List<User> users = usersRepo.findFirst5ByIdNotInAndLoginLike( logins, login + "%");
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
		return usersRepo.findOne(id);
	}
	@Transactional
	public boolean isAdmin(String id){
		if(usersRepo.findInAdminTable(id) != null)
			return true;
		return false;
	}

}

