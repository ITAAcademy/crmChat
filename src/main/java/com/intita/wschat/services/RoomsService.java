package com.intita.wschat.services;

import java.text.SimpleDateFormat;
import java.util.*;

import javax.annotation.PostConstruct;

import com.intita.wschat.domain.ChatRoomType;
import com.intita.wschat.domain.UserRole;
import com.intita.wschat.models.*;
import com.intita.wschat.repositories.*;
import com.intita.wschat.services.common.UsersOperationsService;
import com.intita.wschat.util.HtmlUtility;
import com.intita.wschat.web.BotController;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.context.annotation.Lazy;
import org.springframework.core.env.Environment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.intita.wschat.event.LoginEvent;
import com.intita.wschat.event.ParticipantRepository;
import com.intita.wschat.web.ChatController;

@Service
public class RoomsService {

	@Autowired private RoomRepository roomRepo;
	@Autowired private PrivateRoomInfoRepository privateRoomInfoRepo;
	@Autowired private ChatPhrasesRepository phrasesRepo;
	@Autowired private UsersService userService;
	@Autowired private ChatUsersService chatUserService;
	@Autowired private ChatUserLastRoomDateService chatLastRoomDateService;
	@Autowired private UserMessageService userMessageService;
	@Autowired private SimpMessagingTemplate simpMessagingTemplate;
	@Autowired private ParticipantRepository participantRepository;

	@Autowired private RoomPermissionsService roomPermissionsServcie;
	@Autowired private RoomRolesRepository roomRolesRepository;
	@Autowired
	@Lazy
	private UsersOperationsService usersOperationsService;

	private final static Logger log = LoggerFactory.getLogger(RoomsService.class);
	private String defaultTableNames = "user_admin,user_student,user_super_visor,user_teacher_consultant,user_accountant,user_consultant,user_tenant,user_trainer,user_auditor,user_author,user_director";
	private String defaultRoleNames = "Administrators, Students, Supervisors, Teachers and consultants, Accountants, Consultants, Tenants, Trainers, Auditors, Authors, Directors";

	@Autowired
	private Environment env;

	//@Value("${crmchat.roles.tableNames}")
	private List<String> rolesTablesNames;
	//@Value("${crmchat.roles.names}")
	private List<String> rolesNames;

	@PostConstruct
	public void initParams() {
		String rolesTablesNamePropertyValue =  env.getProperty("crmchat.roles.tableNames");
		String rolesNamesProperyValue =  env.getProperty("crmchat.roles.names");
		if(rolesTablesNamePropertyValue==null)rolesTablesNamePropertyValue=defaultTableNames;
		if(rolesNamesProperyValue==null)rolesNamesProperyValue=defaultRoleNames;


		if (rolesTablesNamePropertyValue==null || rolesNamesProperyValue==null){
			log.error("crmchat.roles.tableNames or crmchat.roles.names not defined");
			return;
		}

		String[] rolesTablesNameArr =rolesTablesNamePropertyValue.split(",");
		rolesTablesNameArr = HtmlUtility.trimAllStrings(rolesTablesNameArr);
		rolesTablesNames = Arrays.asList(rolesTablesNameArr);

		String[] rolesNamesArr =  rolesNamesProperyValue.split(",");
		rolesNamesArr = HtmlUtility.trimAllStrings(rolesNamesArr);
		rolesNames = Arrays.asList(rolesNamesArr);

	}

	@Transactional
	public Page<Room> getRooms(int page, int pageSize){
		return roomRepo.findAll(new PageRequest(page-1, pageSize)); 

	}

	@Transactional
	public List<Room> getRooms(){
		return (List<Room>) roomRepo.findAll(); 
	}

	@Transactional
	public ArrayList<Room> getRoomsWithNameLike(String like){
		return roomRepo.findFirst10ByNameLike(like); 
	}



	@Transactional
	public Room getRoom(Long id){
		if(id < 0)
			return null;
		return roomRepo.findOne(id);
	}

	@Transactional
	public boolean addRooms(Iterable<Room> rooms) {
		if (rooms==null) return false;
		roomRepo.save(rooms);
		return true;
	}

	@Transactional
	public Room getRoom(String name) {
		return roomRepo.findByName(name);
	}

	@Transactional
	public PrivateRoomInfo getPrivateRoomInfo(ChatUser author, ChatUser privateUser) {
		return privateRoomInfoRepo.getByUsers(author, privateUser);
	}
	@Transactional
	public PrivateRoomInfo getPrivateRoomInfo(Room room) {
		PrivateRoomInfo info = privateRoomInfoRepo.findByRoom(room);
		if(info == null && room.getTypeEnum() == ChatRoomType.PRIVATE)
		{
			removeRoom(room);
			throw(new NullPointerException());
		}
		return info;
	}

	@Transactional
	public boolean removeRoom(Room room) {
		chatLastRoomDateService.removeUserLastRoomDate(room.getChatUserLastRoomDate());
		userMessageService.removeAllUserMessagesByRoom(room);
		roomRepo.delete(room);
		return true;
	}
	public ArrayList<Long> getRoomUsersIds(Long roomId){
		//TODO write correct method
		List<ChatUser> roomUsers = getAllRoomUsers(roomId);
		ArrayList<Long> roomUsersIds = new  ArrayList<>();
		for(int i = 0; i <  roomUsers.size(); i++)
		{
			User intitaUser =roomUsers.get(i).getIntitaUser();
			if (intitaUser!=null)
				roomUsersIds.add(intitaUser.getId());
		}
		return roomUsersIds;
	}
	public List<ChatUser> getAllRoomUsers(Long roomId){
		List<ChatUser> users = new  ArrayList<ChatUser>();
		Set<ChatUser>  users_set = null;
		Room room = getRoom(roomId);
		users_set = room.getUsers();
		users.addAll(users_set);
		users.add(room.getAuthor());
		return users;
	}
	public ArrayList<Long> getRoomChatUsersIds(Long roomId){
		//TODO write correct method
		List<ChatUser> roomUsers = getAllRoomUsers(roomId);
		ArrayList<Long> roomUsersIds = new  ArrayList<>();
		for(int i = 0; i <  roomUsers.size(); i++)
		{
			roomUsersIds.add(roomUsers.get(i).getId());
		}
		return roomUsersIds;
	}


	@Transactional
	public Room getPrivateRoom(ChatUser author, ChatUser privateUser) {
		PrivateRoomInfo info = getPrivateRoomInfo(author, privateUser); 
		if(info == null)
			return null;
		return info.getRoom();
	}
	/*
	 * Check if room is private room where student online and trainer is not. Return trainer, which is
	 * one of users. Return null if it doesn't match criteria.
	 */
	@Transactional
	public ChatUser isRoomHasStudentWaitingForTrainer(Long roomId,ChatUser currentUser){
		Room room = getRoom(roomId);
		return isRoomHasStudentWaitingForTrainer(room,currentUser);
	}
	@Transactional
	public ChatUser isRoomHasStudentWaitingForTrainer(Room room,ChatUser currentUser){
		if (room.getTypeEnum() == ChatRoomType.PRIVATE){
			PrivateRoomInfo privateRoomInfo = getPrivateRoomInfo(room);
			ChatUser chatUser1 = privateRoomInfo.getFirtsUser();
			ChatUser chatUser2 = privateRoomInfo.getSecondUser();
			User user1 = chatUser1.getIntitaUser();
			User user2 = chatUser2.getIntitaUser();

			ChatUser tenantUser = null;
			boolean isTrainerUser1 = false;
			boolean isTrainerUser2 = false;
			boolean isStudentUser1 = false;
			boolean isStudentUser2 = false;

			boolean isCurrentUserIs1= false;
			boolean isCurrentUserIs2 = false;

			User trainerOfUser1 =(user1==null)? null : userService.getTrainer(user1.getId());
			User trainerOfUser2 =(user2==null)? null : userService.getTrainer(user2.getId());
			if(user1.equals(trainerOfUser2)){
				isTrainerUser1 = true;
			}
			if(user2.equals(trainerOfUser1)){
				isTrainerUser2 = true;
			}
			if(userService.isStudent(user1.getId()) && !isTrainerUser1){
				isStudentUser1 = true;
			}
			if(userService.isStudent(user2.getId()) && !isTrainerUser2){
				isStudentUser2 = true;
			}

			if (currentUser.getId()==chatUser1.getId()){

				isCurrentUserIs1 = true;
			}
			if (currentUser.getId().equals(chatUser2.getId())){
				isCurrentUserIs2 = true;
			}
			ChatUser hasStudentAndTenant = null;
			if ((isCurrentUserIs1 && isStudentUser1) || (isCurrentUserIs2 && isStudentUser2)){
				if(isCurrentUserIs1){
					hasStudentAndTenant =  isTrainerUser2 ? chatUser2: null;
				}
				else if (isCurrentUserIs2){
					hasStudentAndTenant = isTrainerUser1 ? chatUser1 : null;
				}
			}
			if(hasStudentAndTenant==null)return null;
			if (isTrainerUser1 && !participantRepository.isOnline(chatUser1.getId())) return chatUser1;
			if (isTrainerUser2 && !participantRepository.isOnline(chatUser2.getId())) return chatUser2;
		}
		return null;
	}

	@Transactional
	public ArrayList<PrivateRoomInfo> getPrivateRoomsInfoByUser(ChatUser user) {
		return privateRoomInfoRepo.getByUser(user);
	}

	@Transactional
	public ArrayList<Room> getPrivateRooms(ChatUser user) {
		return privateRoomInfoRepo.getRoomsByUser(user);
	}

	@Transactional
	public ArrayList<ChatUser> getPrivateChatUsers(ChatUser user) {
		ArrayList<ChatUser> users = new ArrayList<>();
		ArrayList<PrivateRoomInfo> infoList = getPrivateRoomsInfoByUser(user);
		for (PrivateRoomInfo privateRoomInfo : infoList) {
			if(privateRoomInfo.getFirtsUser() == user)
				users.add(privateRoomInfo.getSecondUser());
			else
				users.add(privateRoomInfo.getFirtsUser());
		}
		return users;
	}
	@Transactional
	public ArrayList<LoginEvent> getPrivateRoomsLoginEvent(ChatUser user) {
		ArrayList<LoginEvent> users = new ArrayList<>();
		ArrayList<PrivateRoomInfo> infoList = getPrivateRoomsInfoByUser(user);
		for (PrivateRoomInfo privateRoomInfo : infoList) {
			ChatUser uTemp = null;
			if(privateRoomInfo.getFirtsUser() == user)
				uTemp = privateRoomInfo.getSecondUser();
			else
				uTemp = privateRoomInfo.getFirtsUser();
			users.add(new LoginEvent(uTemp));
		}
		return users;
	}

	@Transactional
	public ArrayList<Room> getRoomByAuthor(String author) {

		return roomRepo.findByAuthor(chatUserService.getChatUser(author));
	}
	@Transactional
	public ArrayList<Room> getRoomByAuthor(ChatUser user) {

		return roomRepo.findByAuthor(user);
	}
	@Transactional
	public Set<Room> getAllRoomByUsersAndAuthor(ChatUser user) {
		return roomRepo.findByAuthorOrUsersContaining(user, user);
	}
	@Transactional
	public ArrayList<Room> getRoomByUser(ChatUser user) {

		return roomRepo.findByUsersContaining(user);
	}


	@Transactional(readOnly = false)
	public Room register(String name, ChatUser author, ArrayList<ChatUser> users) {
		if (name==null || name.length()==0) return null;
		Room r = new Room();
		r.setAuthor(author);
		r.setName(name);
		r.setType((short) 0);
		r.addUsers(users);
		r = roomRepo.save(r);
		chatLastRoomDateService.addUserLastRoomDateInfo(author, r);
		for (ChatUser chatUser : users) {
			chatLastRoomDateService.addUserLastRoomDateInfo(chatUser, r);
		}
		return r;
	}
	@Transactional(readOnly = false)
	public Room register(String name, ChatUser author, ChatRoomType type) {
		if(type == ChatRoomType.PRIVATE)
		{
			throw new IllegalArgumentException("use register private function for Room with type ==" + ChatRoomType.PRIVATE);
		}
		Room r = new Room();
		r.setAuthor(author);
		r.setName(name);
		r.setType(type);
		r = roomRepo.save(r);
		chatLastRoomDateService.addUserLastRoomDateInfo(author, r);
		return r;
	}
	@Transactional(readOnly = false)
	public Room register(String name, ChatUser author) {
		return register(name, author, ChatRoomType.DEFAULT);
	}

	@Transactional(readOnly = false)
	public Room registerPrivate(ChatUser first, ChatUser second) {
		Room r = new Room();
		r.setAuthor(first);
		r.setName(first.getNickName() + "_" + second.getNickName());
		r.setType(ChatRoomType.PRIVATE);
		r = roomRepo.save(r);
		chatLastRoomDateService.addUserLastRoomDateInfo(first, r);
		if (first.getId()!= second.getId())
			addUserToRoom(second, r);
		PrivateRoomInfo info = privateRoomInfoRepo.save(new PrivateRoomInfo(r, first, second));
		roomPermissionsServcie.addPermissionsToUser(r, second, RoomPermissions.Permission.ADD_USER.getValue() | RoomPermissions.Permission.REMOVE_USER.getValue());
		return r;
	}

	@Transactional(readOnly = false)
	public boolean unRegister(String name, ChatUser author) {
		Room room = roomRepo.findByName(name);
	//	if(!author.getRootRooms().contains(room))
			return false;
		/*room.setActive(false);
		roomRepo.save(room);//@NEED_ASK@
		return true;*/
	}

	public List<RoomModelSimple> getRoomsContainingStringByOwner(String query, ChatUser user){
		List<RoomModelSimple> list = getRoomsModelByChatUser(user);
		List<RoomModelSimple> result = new ArrayList<RoomModelSimple>();
		for (RoomModelSimple model : list){
			String title = model.getString() == null ? "" : model.getString().toLowerCase();
			String lastMessage = model.getLastMessage() == null ? "" : model.getLastMessage().toLowerCase();
			String queryStr = query == null ? "" : query.toLowerCase();
			if (title.indexOf(queryStr)!=-1 ||
					lastMessage.indexOf(queryStr)!=-1)
				result.add(model);
		}
		return result;
	}

	public boolean addUserToRoom(Long id, User user) {
		Room room = roomRepo.findOne(id);
		addUserToRoom(chatUserService.getChatUser(id), room);		
		return true;
	}

	public void replaceUsersInRoom(Room room, ArrayList<ChatUser> chatUserList) {
		if(chatUserList==null)return;
		Set<ChatUser> roomUserList = room.getUsers();
		ArrayList<ChatUser> add = new ArrayList<>(chatUserList);
		int size = roomUserList.size();
		add.removeAll(roomUserList);
		addUsersToRoom(add, room);

		ArrayList<ChatUser> remove = new ArrayList<>(roomUserList);
		remove.removeAll(chatUserList);
		for (ChatUser chatUser : remove) {
			removeUserFromRoom(chatUser, room);
		}
	}


	public void setAuthor(ChatUser user, Room room)
	{
		room.setAuthor(user);
		chatLastRoomDateService.addUserLastRoomDateInfo(user, room);	
	}

	@Transactional(readOnly = false)
	public Room update(Room room,boolean notify){
		room = roomRepo.save(room);
		Set<ChatUser> users = new HashSet<>(room.getUsers());
		if(room.getAuthor() != null)
			users.add(room.getAuthor());
		for (ChatUser chatUser : users) {
			usersOperationsService.updateRoomByUser(chatUser, room,false);
		}
		/*Map<String, Object> sendedMap = new HashMap<>();
		sendedMap.put("updateRoom", new RoomModelSimple(0, new Date().toString(), room,userMessageService.getLastUserMessageByRoom(room)));

		String subscriptionStr = "/topic/users/info";
		ObjectMapper mapper =  new ObjectMapper();
		//mapper.disable(MapperFeature.DEFAULT_VIEW_INCLUSION);

		try {
			simpMessagingTemplate.convertAndSend(subscriptionStr, sendedMap);
		} catch (MessagingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		chatController.addFieldToInfoMap("updateRoom", sendedMap);*/
		return room;
	}

	@Transactional(readOnly = false)
	public boolean addUserByNameToRoom(Long id, String name) {
		Room room = roomRepo.findOne(id);
		return addUserToRoom(chatUserService.getChatUser(name), room);
	}

	@Transactional(readOnly = false, propagation = Propagation.SUPPORTS)
	public boolean addUserToRoom(ChatUser user, Room room) {
		if(room == null)
			return false;
		if(user == null)
			return false;
		//have premition?
		if(room.cloneChatUsers().contains(user))
			return false;

		room.addUser(user);
		roomRepo.save(room);
		chatLastRoomDateService.addUserLastRoomDateInfo(user, room);
		return true;
	}

	@Transactional(readOnly = false, propagation = Propagation.SUPPORTS)
	public boolean addUsersToRoom(ArrayList<ChatUser> users, Room room) {
		if(room == null)
			return false;
		if(users == null)
			return false;
		//have premition?
		ArrayList<ChatUser> compareArray =  new  ArrayList<>(users);
		compareArray.removeAll(room.cloneChatUsers());		
		System.out.println("QQQQQQQQQQQQQ " +  room.getUsers().size());
		System.out.println("QQQQQQQQQQQQQ " +  room.addUsers(compareArray));

		System.out.println("QQQQQQQQQQQQQ " +  room.getUsers().size());
		roomRepo.save(room);
		for (ChatUser chatUser : compareArray) {
			chatLastRoomDateService.addUserLastRoomDateInfo(chatUser, room);
		}
		return true;
	}

	@Transactional(readOnly = false)
	public boolean removeUserFromRoom(ChatUser user, Room room) {
		if(room == null)
			return false;
		if(user == null)
			return false;
		//have premition?
		chatLastRoomDateService.removeUserLastRoomDate(user, room);
		room.removeUser(user);
		roomPermissionsServcie.removePermissionsOfUser(room, user);
		roomRepo.save(room);
		return true;
	}
	//@Transactional(readOnly = true)
	public ArrayList<Phrase> getPhrases(){
		return phrasesRepo.findAll(); 
	}

	//@Transactional(readOnly = true)
	public ArrayList<Phrase> getEvaluatedPhrases(ChatUser currentUser){

		String[] searchList = {"$date","$username"};
		String currentDate = new SimpleDateFormat("dd-MM-yyyy").format(new Date());
		User intitaUser = null;
		if (currentUser!=null) 
			intitaUser = currentUser.getIntitaUser();
		String currentUserName = "";

		if(intitaUser!=null) 
			currentUserName = intitaUser.getFullName();
		else 
			currentUserName = currentUser.getNickName();

		String[] replacementList = {currentDate,currentUserName};
		ArrayList<Phrase> phrases = getPhrases();
		for (Phrase phrase : phrases){
			String phraseText = phrase.getText();
			phraseText = StringUtils.replaceEach(phraseText, searchList, replacementList);
			phrase.setText(phraseText);
		}
		return phrases;	
	}
	@Transactional
	public List<RoomModelSimple> getRoomsModelByChatUser(ChatUser currentUser) {
		return getRoomsByChatUserAndList(currentUser, null,null);
	}

	@Transactional
	public List<RoomModelSimple> getRoomsModelByChatUser(ChatUser currentUser, Integer count) {
		return getRoomsByChatUserAndList(currentUser, null,count);
	}

	@Transactional
	public List<RoomModelSimple> getRoomsModelByChatUserAndRoomList(ChatUser currentUser, ArrayList<Room> list) {
		return getRoomsByChatUserAndList(currentUser, list,null);
	}

	@Transactional
	public List<RoomModelSimple> getRoomsByChatUserAndList(ChatUser currentUser, ArrayList<Room> sourseRooms, Integer count) {
		//Map<Long, String>  rooms_map = convertToNameList(room_array);		
		List<RoomModelSimple> result = new ArrayList <RoomModelSimple> ();

		List<ChatUserLastRoomDate> rooms_lastd = null;
		PageRequest pageRequest =null;
		if (count !=null) pageRequest = new PageRequest(0,count);
		if(sourseRooms == null)
			rooms_lastd = chatLastRoomDateService.getUserLastRoomDates(currentUser,pageRequest);
		else
			rooms_lastd = chatLastRoomDateService.getUserLastRoomDatesInList(currentUser, sourseRooms);

		//Set<UserMessage> messages =  userMessageService.getMessagesByNotUser(currentUser);
		for (int i = 0; i < rooms_lastd.size() ; i++)
		{
			ChatUserLastRoomDate entry = rooms_lastd.get(i);
			Date date = entry.getLastLogout();
			int messages_cnt =  userMessageService.getMessagesCountByRoomDateNotUser(entry.getRoom(), date, entry.getChatUser()).intValue();

			if (entry.getLastRoom()==null /*|| entry.getLastRoom().getType() == Room.RoomType.CONSULTATION*/) 
				continue;
			RoomModelSimple sb = RoomModelSimple.buildSimpleModelForRoom(this, currentUser, messages_cnt , date.toString(),
					entry.getLastRoom(),userMessageService.getLastUserMessageByRoom(entry.getLastRoom()));
			sb = extendSimpleModelByUserPermissionsForRoom(sb, currentUser, entry.getLastRoom());
			result.add(sb);
		}
		return result;				
	}
	@Transactional
	public RoomModelSimple getSimpleModelByUserPermissionsForRoom(ChatUser user, Integer nums, String date,Room room,UserMessage lastMessage){
		RoomModelSimple model = RoomModelSimple.buildSimpleModelForRoom(this, user, nums, date, room, lastMessage);
		model = extendSimpleModelByUserPermissionsForRoom(model,user,room);
		return model;
	}
	public RoomModelSimple extendSimpleModelByUserPermissionsForRoom(RoomModelSimple model,ChatUser user,Room room){
		Integer userPermissions = roomPermissionsServcie.getPermissionsOfUser(room, user);
		model.setUserPermissions(userPermissions);
		return model;
	}
	@Transactional
	public ArrayList<Room> getRoomsByIds(ArrayList<Long> intitaUsersIds){
		return roomRepo.findRoomsByIds(intitaUsersIds);
	}

	@Transactional
	public String updateRoomName(Room room, String roomName){
		String newName = roomName.trim();
		if (newName.length()>0 || room == null){
			room.setName(newName);
			roomRepo.save(room);
			return newName;
		}
		return room.getName();

	}


	public boolean isLastMsgReaded(Room room, UserMessage msg)
	{
		ChatUserLastRoomDate item = chatLastRoomDateService.findByRoomAndChatUserNotAndLast_logoutAfer(room, msg.getAuthor(), msg.getDate());
		if(item != null)
			System.out.println("IsLastMsgReaded: " + item.getChatUser().getNickName() + "  vs  " + msg.getAuthor().getNickName()); 
		return item != null;
	}

	public Date getLastMsgActivity(Room room, UserMessage msg)
	{
		ChatUserLastRoomDate item = chatLastRoomDateService.getLastNotUserActivity(room, msg.getAuthor());
		if(item != null)
			return item.getLastLogout();

		return null;
	}

	public void updateRoomsForAllRoles(boolean notifyUsers) {
		try {
			for (String table : rolesTablesNames) {
				updateRoomForRoleTable(table,notifyUsers);
			}
		}
		catch(Exception e){

		}
	}

	@Async
	public Boolean updateRoomsForAllRoles(String tableName,boolean notifyUsers){
		if(tableName==null) {
			updateRoomsForAllRoles(notifyUsers);
		}
		else {
			try {
				boolean updated = updateRoomForRoleTable(tableName,notifyUsers);
				if (!updated) return false;
			}
			catch(Exception e){
				//e.printStackTrace();
				return false;
			}
		}

		return true;
	}

	@Transactional
	public boolean updateRoomForRoleTable(String tableName,boolean notifyUsers){
		int indexOfTable = rolesTablesNames.indexOf(tableName);
		if (indexOfTable == -1) return false;
		String roleName = rolesNames.get(indexOfTable);
		int roleInt = 1 << indexOfTable;
		UserRole role = UserRole.getByTableName(tableName);
		RoomRoleInfo info =  roomRolesRepository.findOneByRoleId(roleInt);
		if(info == null)
		{
			Room room = register(roleName, chatUserService.getChatUser(BotController.BotParam.BOT_ID), ChatRoomType.ROLES_GROUP);
			info = roomRolesRepository.save(new RoomRoleInfo(room, roleInt));
		}
		Room room = info.getRoom();
		//roomsService.setAuthor(chatUsersService.getChatUser(BotParam.BOT_ID), room);
		//room = update(room,notifyUsers);
		room = update(room,false);
		ArrayList<ChatUser> cUsersList = null;

		ArrayList<Long> intitaUsers = userService.getAllByRoleValue(roleInt, tableName);
		if (intitaUsers.size()==0) return false;

			if (role == UserRole.TENANTS)
				cUsersList = chatUserService.getUsers(intitaUsers);
			else {
				cUsersList = chatUserService.getChatUsersFromIntitaIds(intitaUsers);
			}

		replaceUsersInRoom(room, cUsersList);
		return true;
	}

	public boolean isRoomParticipant(Long chatUserId, Long roomId) {
		ChatUser participant = ChatUser.forId(chatUserId);
		Room room = Room.forId(roomId);
		Long count = roomRepo.countByAuthorOrInUsers(participant,room);
		return count > 0;
	}
	public List<ChatUser> getChatUsers(Room room,int count){
		List<ChatUser> users = roomRepo.getChatUsers(room,new PageRequest(0,count));
		return users;
	}

	public List<ChatUser> getChatUsersAfterId(Room room,Long id,int count) {
		List<Integer> usersIds = roomRepo.getChatUsersIdsWherechatUserIdAfter(room.getId(),id,count);
		List<Long> idsLong = new ArrayList<Long>();
		for (Integer userId : usersIds) {
			idsLong.add(new Long(userId));
		}
		List<ChatUser> users = chatUserService.getUsers(idsLong);
		return users;
	}

	public List<Room> findChatUserRooms(Long userId, String nameLike,int count)  {
		ChatUser user = ChatUser.forId(userId);
		return roomRepo.findRoomsOfUser(user,nameLike,new PageRequest(0,count));
	}


}


