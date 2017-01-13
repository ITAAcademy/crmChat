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
import com.intita.wschat.models.Room;
import com.intita.wschat.models.User;
import com.intita.wschat.repositories.ChatUserRepository;

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

	@Transactional
	public Page<ChatUser> getChatUsers(int page, int pageSize){
		return chatUsersRepo.findAll(new PageRequest(page-1, pageSize)); 

	}
	@Transactional
	public ArrayList<ChatUser> getAllTrainers(){
		HashSet<Integer> trainersIds =  userService.getAllTrainersIds();
		ArrayList<ChatUser> trainers = new ArrayList<ChatUser>();
		for (Integer userIdOfTrainer : trainersIds){
			User user = userService.getUser((long)userIdOfTrainer);
			ChatUser chatUser = user.getChatUser();
			if (chatUser != null)
			trainers.add(chatUser);
		}
		return trainers;
	}
	@Transactional
	public List<String> getUsersNickNameFist5(String nickName, List<String> excludedNicks){
		List<ChatUser> users = chatUsersRepo.findFirst5ByNickNameNotInAndNickNameLike( excludedNicks, nickName + "%");
		List<String> nickNames = new ArrayList<String>();
		for(int i = 0; i < users.size(); i++)
			nickNames.add(users.get(i).getNickName());
		return nickNames;

	}
	@Transactional
	public List<ChatUser> getUsersFist5(String nickName, List<String> excludedNicks){
		List<ChatUser> users = chatUsersRepo.findFirst5ByNickNameNotInAndNickNameLike( excludedNicks, nickName + "%");
		return users;
	}
	@Transactional
	public LoginEvent getLoginEvent(ChatUser chatUser,boolean isOnline){
		if (chatUser==null) return null;
		return new LoginEvent(chatUser, isOnline);
	}
	@Transactional
	public ArrayList<ChatUser> getUsers(List<Long> ids){
		return chatUsersRepo.findAllByIdIn(ids);
	}
	@Transactional
	public ArrayList<ChatUser> getUsers(){
		return (ArrayList<ChatUser>) IteratorUtils.toList(chatUsersRepo.findAll().iterator());
	}
	@Transactional
	public ChatUser getChatUser(Long id){
		return chatUsersRepo.findOne(id);
	}
	@Transactional
	public ChatUser getChatUserFromIntitaId(Long id, boolean createGuest){
		User currentUser = userService.getById(id);
		return getChatUserFromIntitaUser(currentUser, createGuest);
	}
	@Transactional
	public ChatUser getChatUserFromIntitaEmail(String email, boolean createGuest){
		User currentUser = userService.getUser(email);
		return getChatUserFromIntitaUser(currentUser, createGuest);
	}
	@Transactional
	public ChatUser getChatUserFromIntitaUser(User currentUser, boolean createGuest){
		if(currentUser == null  )
		{
			if(createGuest)
				return register("Guest_" + new ShaPasswordEncoder().encodePassword(((Integer)new Random(new Date().getTime()).nextInt()).toString(), new BCryptPasswordEncoder()), null);
			else
				return null;
		}
		ChatUser tempChatUser = chatUsersRepo.findFirstByIntitaUser(currentUser);
		if(tempChatUser == null )
		{
			tempChatUser = register(currentUser.getNickName(), currentUser);
		}
		return tempChatUser;
	}
	@Transactional
	public User getUsersFromChatUserId(Long id) {
		ChatUser cUser = getChatUser(id);
		return cUser.getIntitaUser();
	}

	@Transactional
	public ChatUser getChatUser(String nickName) {
		return chatUsersRepo.findOneByNickName(nickName);
	}

	@Transactional(readOnly = false)
	public ChatUser register(String nickName, User intitaUser) {
		ChatUser u = new ChatUser(nickName,intitaUser);
		return chatUsersRepo.save(u);
	}

	@Transactional
	public void updateChatUserInfo(ChatUser u){
		chatUsersRepo.save(u);
	}
	@Transactional
	public void removeUser(Long id){
		chatUsersRepo.delete(id);
	}
	@Transactional
	public List<ChatUser> getChatUsersLike(String nickName){
		return chatUsersRepo.findFirst5ByNickNameLike(nickName + "%");
	}

	@Transactional
	public ChatUser isMyRoom(String roomId, String userId){
		ArrayList<Room> roomList = new ArrayList<>();
		roomList.add(new Room(Long.parseLong(roomId)));
		return chatUsersRepo.findFirstByRoomsContainingOrRoomsFromUsersContainingAndId(roomList, roomList, Long.parseLong(userId));
	}
	/*public ChatUser getById(Long id){
		return usersRepo.findOne(id);
	}*/


}
