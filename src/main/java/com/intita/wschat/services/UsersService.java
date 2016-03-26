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
@Transactional
public class UsersService {

	@Autowired
	private UserRepository usersRepo;
	
	@Autowired
	private ChatUsersService chatUsersService;

	@PostConstruct
	
	public void createAdminUser() {
		System.out.println("admin user created");
		//register("user", "user", "user");

	}

	
	public Page<User> getUsers(int page, int pageSize){
		return usersRepo.findAll(new PageRequest(page-1, pageSize)); // spring рахує сторінки з нуля

	}
	
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

	
	public List<String> getUsersEmailsFist5(String login, List<String> logins){
		List<User> users = usersRepo.findFirst5ByLoginNotInAndLoginLike( logins, login + "%");
		List<String> emails = new ArrayList<String>();
		for(int i = 0; i < users.size(); i++)
			emails.add(users.get(i).getEmail());
		return emails;

	}
	
	
	
	public ArrayList<User> getUsers(){
		return (ArrayList<User>) IteratorUtils.toList(usersRepo.findAll().iterator()); // spring рахує сторінки з нуля
	}
	
	public User getUser(Long id){
		return usersRepo.findOne(id);
	}
	
	public User getUserFromChat(Long chatUserId){
		ChatUser chatUser= chatUsersService.getChatUser(chatUserId);
		if (chatUser==null) return null;
		return chatUser.getIntitaUser();
		
	}

	
	public User getUser(String login) {
		return usersRepo.findByLogin(login);
	}

	public void register(String login, String email, String pass) {
		String passHash = new ShaPasswordEncoder().encodePassword(pass, null);
		//encode(pass);
		//String passHash = pass;

		User u = new User(login, email.toLowerCase(), passHash);

		// підпишемо користувача на самого себе

		usersRepo.save(u);
	}

	public void register(String login, String email, String pass,Permissions permission) {
		String passHash = new BCryptPasswordEncoder().encode(pass);
		//String passHash = pass;
		User u = new User(login, email.toLowerCase(), passHash);
		u.setPermission(permission);
		// підпишемо користувача на самого себе

		usersRepo.save(u);
	}

	public void togglePermissionById(Long id){
		User u = getById(id);
		u.togglePermission();
		usersRepo.save(u);
	}

	public void updateUserInfo(User u){
		usersRepo.save(u);
	}


	public void removeUser(Long id){
		usersRepo.delete(id);
	}

	public User getById(Long id){
		return usersRepo.findOne(id);
	}

	public boolean isAdmin(String id){
		if(usersRepo.findInAdminTable(id) != null)
			return true;
		return false;
	}

}

