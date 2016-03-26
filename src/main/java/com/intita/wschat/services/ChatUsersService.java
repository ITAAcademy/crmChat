package com.intita.wschat.services;

import java.security.Principal;
import java.util.ArrayList;
import java.util.Date;
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

import com.intita.wschat.models.ChatUser;
import com.intita.wschat.models.Room;
import com.intita.wschat.models.User;
import com.intita.wschat.models.User.Permissions;
import com.intita.wschat.repositories.ChatUserRepository;
import com.intita.wschat.repositories.UserRepository;

@Service
@Transactional
public class ChatUsersService {

	@Autowired
	private ChatUserRepository chatUsersRepo;
	@Autowired
	private UsersService userService;

	@PostConstruct
	
	public void createAdminUser() {
		System.out.println("admin user created");
		//register("user", "user", "user");

	}
	public ChatUser getChatUser(Principal principal){
		if (principal==null)return null;
		String chatUserIdStr = principal.getName();
		Long chatUserId = 0L;
		try{
			chatUserId = Long.parseLong(chatUserIdStr);
		}
		catch(NumberFormatException e){
			System.out.println(e);
			return null;
		}
		if(chatUserId < 0)
			return null;
		
		ChatUser user = getChatUser(chatUserId);
		return user;
	}

	public Page<ChatUser> getChatUsers(int page, int pageSize){
		return chatUsersRepo.findAll(new PageRequest(page-1, pageSize)); // spring рахує сторінки з нуля

	}

	public List<String> getUsersNickNameFist5(String nickName, List<String> excludedNicks){
		List<ChatUser> users = chatUsersRepo.findFirst5ByNickNameNotInAndNickNameLike( excludedNicks, nickName + "%");
		List<String> nickNames = new ArrayList<String>();
		for(int i = 0; i < users.size(); i++)
			nickNames.add(users.get(i).getNickName());
		return nickNames;

	}
	public List<ChatUser> getUsersFist5(String nickName, List<String> excludedNicks){
		List<ChatUser> users = chatUsersRepo.findFirst5ByNickNameNotInAndNickNameLike( excludedNicks, nickName + "%");
		return users;

	}
	public ArrayList<ChatUser> getUsers(){
		return (ArrayList<ChatUser>) IteratorUtils.toList(chatUsersRepo.findAll().iterator()); // spring рахує сторінки з нуля
	}
	public ChatUser getChatUser(Long id){
		return chatUsersRepo.findOne(id);
	}
	public ChatUser getChatUserFromIntitaId(Long id, boolean createGuest){
		User currentUser = userService.getById(id);
		return getChatUserFromIntitaUser(currentUser, createGuest);
	}
	public ChatUser getChatUserFromIntitaEmail(String email, boolean createGuest){
		User currentUser = userService.getUser(email);
		return getChatUserFromIntitaUser(currentUser, createGuest);
	}
	public ChatUser getChatUserFromIntitaUser(User currentUser, boolean createGuest){
		if(currentUser == null  )
		{
			if(createGuest)
				return register("Guest_" + new ShaPasswordEncoder().encodePassword(((Integer)new Random(new Date().getTime()).nextInt()).toString(), new BCryptPasswordEncoder()), null);
			else
				return null;
		}
		ChatUser tempChatUser = chatUsersRepo.findOneByIntitaUser(currentUser);
		if(tempChatUser == null )
		{
			tempChatUser = register(currentUser.getLogin(), currentUser);
		}
		return tempChatUser;
	}

	public User getUsersFromChatUserId(Long id) {
		ChatUser cUser = getChatUser(id);
		return cUser.getIntitaUser();
	}


	public ChatUser getChatUser(String nickName) {
		return chatUsersRepo.findOneByNickName(nickName);
	}

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

	public List<ChatUser> getChatUsersLike(String nickName){
		return chatUsersRepo.findFirst5ByNickNameLike(nickName + "%");
	}

	public ChatUser isMyRoom(String roomId, String userId){
		ArrayList<Room> roomList = new ArrayList<>();
		roomList.add(new Room(Long.parseLong(roomId)));
		return chatUsersRepo.findFirstByRoomsContainingOrRoomsFromUsersContainingAndId(roomList, roomList, Long.parseLong(userId));
	}
	/*public ChatUser getById(Long id){
		return usersRepo.findOne(id);
	}*/


}
