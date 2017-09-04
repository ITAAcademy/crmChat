package com.intita.wschat.services;

import java.util.*;

import javax.annotation.PostConstruct;

import com.intita.wschat.config.ChatPrincipal;
import org.apache.commons.collections4.IteratorUtils;
import org.hibernate.Hibernate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.authentication.encoding.ShaPasswordEncoder;
import org.springframework.security.core.Authentication;
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

	private Map<String,ChatUser> guestUsers = new HashMap<String,ChatUser>();

	public ChatUser generateNewGuest(){
		String nickName = generateRandomNickName();
		ChatUser chatUser = new ChatUser(nickName,null);
		guestUsers.put(nickName,chatUser);
		return chatUser;
	}

	public ChatUser persistGuest(String nickName){
		ChatUser chatUser = guestUsers.get(nickName);
		if (chatUser==null) return null;
		guestUsers.remove(chatUser.getNickName());
		return chatUsersRepo.save(chatUser);
	}

	private String generateRandomNickName(){
		return "Guest_" + new ShaPasswordEncoder().encodePassword(((Integer)new Random(new Date().getTime()).nextInt()).toString(), new BCryptPasswordEncoder());
	}


	@PostConstruct
	@Transactional
	public void createAdminUser() {
		System.out.println("admin user created");
		//register("user", "user", "user");

	}

	@Transactional
	public ChatUser getChatUser(Authentication auth){
		if (auth==null)return null;
		ChatPrincipal chatPrincipal = (ChatPrincipal)auth.getPrincipal();
		return chatPrincipal.getChatUser();
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
	public List<ChatUser> getUsersFist5ExcludeNicks(String nickName, List<String> excludedNicks){
		List<ChatUser> users = chatUsersRepo.findFirst5ByNickNameNotInAndNickNameLike( excludedNicks, nickName + "%");
		return users;
	}
	@Transactional
	public List<ChatUser> getUsersFist5(String nickName){
		List<ChatUser> users = chatUsersRepo.findFirst5ByNickName(nickName + "%");
		return users;
	}
	@Transactional
	public List<ChatUser> getUsersFist5ExcludingList(String nickName, List<Long> excludedIds){
		List<ChatUser> users = chatUsersRepo.findFirst5ByNickNameExcludeList( nickName,excludedIds,new PageRequest(0, 5));
		return users;
	}
	@Transactional
	public LoginEvent getLoginEvent(ChatUser chatUser){
		if (chatUser==null) return null;
		return new LoginEvent(chatUser);
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
	ChatUser finishLazy(ChatUser cUser){
		if(cUser != null)
		{
			Hibernate.initialize(cUser);
		}
		return cUser;
	}
	@Transactional
	public ChatUser getChatUserWithoutLazy(Long id){
		return finishLazy(chatUsersRepo.findOne(id));
	}
	@Transactional
	public ChatUser getChatUserFromIntitaId(Long id, boolean createGuest){
		User currentUser = userService.getById(id);
		return getChatUserFromIntitaUser(currentUser, createGuest);
	}
	@Transactional
	public ChatUser getByIntitaId(Long intitaId) {
		return chatUsersRepo.findByIntitaId(intitaId);
	}

	public ArrayList<ChatUser> getChatUsersFromIntitaIds(ArrayList<Long> intitaUsersIds){
		ArrayList<ChatUser> resultSet = new ArrayList<ChatUser>();
		if(intitaUsersIds==null || intitaUsersIds.size()<1)
			return resultSet;

		for(Long intitaId : intitaUsersIds)
		{
			resultSet.add(getChatUserFromIntitaId(intitaId, false));
		}
		return resultSet;
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
	public List<ChatUser> getChatUsersByEmailAndName(String queryParam){
		return chatUsersRepo.findChatUserByNameAndEmail(queryParam);
	}

	@Transactional
	public ChatUser isMyRoom(String roomId, String userId){
		ArrayList<Room> roomList = new ArrayList<>();
		roomList.add(new Room(Long.parseLong(roomId)));
		return chatUsersRepo.findFirstByRoomsContainingOrRoomsFromUsersContainingAndId(roomList, roomList, Long.parseLong(userId));
	}


	public int getActiveUsersCount(int days){
		return chatUsersRepo.countChatUserByMessagesDateAfter(days);
	}


	/*public ChatUser getById(Long id){
		return usersRepo.findOne(id);
	}*/


}
