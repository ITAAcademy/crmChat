package com.sergialmar.wschat.services;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.apache.commons.collections4.IteratorUtils;

import com.sergialmar.wschat.models.User;
import com.sergialmar.wschat.models.User.Permissions;
import com.sergialmar.wschat.repositories.UserRepository;

@Service
public class UsersService {
	
	@Autowired
	private UserRepository usersRepo;
	
	@PostConstruct
	@Transactional
	public void createAdminUser() {
		System.out.println("admin user created");
		register("admin", "admin@mail.com", "qwerty",Permissions.PERMISSIONS_ADMIN);
		register("user", "user@mail.com", "qwerty",Permissions.PERMISSIONS_USER);
	}
	
	@Transactional
	   public Page<User> getUsers(int page, int pageSize){
			return usersRepo.findAll(new PageRequest(page-1, pageSize)); // spring рахує сторінки з нуля
			
	   }
	@Transactional
	public ArrayList<User> getUsers(){
		return (ArrayList<User>) IteratorUtils.toList(usersRepo.findAll().iterator()); // spring рахує сторінки з нуля
	}
	@Transactional
	public User getUser(Long id){
		return usersRepo.findOne(id);
	}
	
	@Transactional(readOnly = false)
	public void register(String login, String email, String pass) {
		String passHash = new BCryptPasswordEncoder().encode(pass);
		//String passHash = pass;
		
		User u = new User(login, email.toLowerCase(), passHash);

		// підпишемо користувача на самого себе

		usersRepo.save(u);
	}
	@Transactional(readOnly = false)
	public void register(String login, String email, String pass,Permissions permission) {
		String passHash = new BCryptPasswordEncoder().encode(pass);
		//String passHash = pass;
		User u = new User(login, email.toLowerCase(), passHash);
		u.setPermission(permission);
		// підпишемо користувача на самого себе

		usersRepo.save(u);
	}
	@Transactional(readOnly = false)
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

}

