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
	private ChatUserRepository chatUsersRepo;
	@Autowired
	private UsersService userService;

	@PostConstruct
	@Transactional
	public void createAdminUser() {
		System.out.println("admin user created");
		//register("user", "user", "user");

	}

	@Transactional
	public Page<ChatUser> getChatUsers(int page, int pageSize){
		return chatUsersRepo.findAll(new PageRequest(page-1, pageSize)); // spring рахує сторінки з нуля

	}

	@Transactional
	public List<String> getUsersNickNameFist5(String nickName, List<String> logins){
		List<ChatUser> users = chatUsersRepo.findFirst5ByNickNameNotInAndNickNameLike( logins, nickName + "%");
		List<String> nickNames = new ArrayList<String>();
		for(int i = 0; i < users.size(); i++)
			nickNames.add(users.get(i).getNickName());
		return nickNames;

	}
	@Transactional
	public ArrayList<ChatUser> getUsers(){
		return (ArrayList<ChatUser>) IteratorUtils.toList(chatUsersRepo.findAll().iterator()); // spring рахує сторінки з нуля
	}
	@Transactional
	public ChatUser getChatUser(Long id){
		return chatUsersRepo.findOne(id);
	}
	@Transactional
	public ChatUser getChatUserFromIntitaId(Long id){
		User currentUser = userService.getById(id);
		if(currentUser == null)
			return null;
		ChatUser tempChatUser = chatUsersRepo.findOneByIntitaUser(currentUser);
		if(tempChatUser == null)
		{
			if(currentUser == null)
			{
				tempChatUser = register("ANONIM", null);
			}
			else
			{
				tempChatUser = register(currentUser.getLogin(), currentUser);
			}

		}
		return tempChatUser;
	}

	@Transactional
	public User getUsersFromChatUserId(Long id) {
		ChatUser cUser = getChatUser(id);
		return cUser.getIntitaUser();
	}

	@Transactional
	public List<ChatUser> getChatUsers(String login) {
		return chatUsersRepo.findByNickName(login);
	}

	@Transactional(readOnly = false)
	public ChatUser register(String nickName, User intitaUser) {
		ChatUser u = new ChatUser(nickName,intitaUser);
		chatUsersRepo.save(u);
		return u;
	}

	public void updateChatUserInfo(ChatUser u){
		chatUsersRepo.save(u);
	}

	public void removeUser(Long id){
		chatUsersRepo.delete(id);
	}
	/*public ChatUser getById(Long id){
		return usersRepo.findOne(id);
	}*/


}
