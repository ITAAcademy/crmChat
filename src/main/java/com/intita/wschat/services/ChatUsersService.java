package com.intita.wschat.services;

import java.util.ArrayList;
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
import com.intita.wschat.repositories.ChatUserRepository;
import com.intita.wschat.repositories.UserRepository;

@Service
public class ChatUsersService {

	@Autowired
	private ChatUserRepository usersRepo;

	@PostConstruct
	@Transactional
	public void createAdminUser() {
		System.out.println("admin user created");
		//register("user", "user", "user");

	}

	@Transactional
	public Page<ChatUser> getChatUsers(int page, int pageSize){
		return usersRepo.findAll(new PageRequest(page-1, pageSize)); // spring рахує сторінки з нуля

	}

	@Transactional
	public List<String> getUsersNickNameFist5(String nickName, List<String> logins){
		List<ChatUser> users = usersRepo.findFirst5ByNickNameNotInAndNickNameLike( logins, nickName + "%");
		List<String> nickNames = new ArrayList<String>();
		for(int i = 0; i < users.size(); i++)
			nickNames.add(users.get(i).getNickName());
		return nickNames;

	}
	@Transactional
	public ArrayList<ChatUser> getUsers(){
		return (ArrayList<ChatUser>) IteratorUtils.toList(usersRepo.findAll().iterator()); // spring рахує сторінки з нуля
	}
	@Transactional
	public ChatUser getChatUser(Long id){
		return usersRepo.findOne(id);
	}

	@Transactional
	public List<ChatUser> getChatUsers(String login) {
		return usersRepo.findByNickName(login);
	}

	@Transactional(readOnly = false)
	public void register(String nickName, User intitaUser) {
		ChatUser u = new ChatUser(nickName,intitaUser);
		usersRepo.save(u);
	}

	public void updateChatUserInfo(ChatUser u){
		usersRepo.save(u);
	}

	public void removeUser(Long id){
		usersRepo.delete(id);
	}
	/*public ChatUser getById(Long id){
		return usersRepo.findOne(id);
	}*/

	
}
