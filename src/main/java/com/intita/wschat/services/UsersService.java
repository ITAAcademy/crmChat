package com.intita.wschat.services;

import java.security.Principal;
import java.util.*;

import javax.annotation.PostConstruct;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

import com.intita.wschat.config.ChatPrincipal;
import com.intita.wschat.domain.UserRole;
import com.intita.wschat.dto.mapper.DTOMapper;
import com.intita.wschat.dto.model.ChatUserDTO;
import org.apache.commons.collections4.IteratorUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
import com.intita.wschat.models.User;
import com.intita.wschat.models.User.Permissions;
import com.intita.wschat.repositories.UserRepository;

@Service
public class UsersService {

	private final static Logger log = LoggerFactory.getLogger(UsersService.class);

	@Autowired
	private UserRepository usersRepo;

	@Autowired
	private ChatUsersService chatUsersService;

	@Autowired
	private ChatTenantService chatTenantService;

	@PersistenceContext
	EntityManager entityManager;

	@Autowired private DTOMapper dtoMapper;




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
	public ArrayList<User> getUsers(List<Long> ids){
		return usersRepo.findAllByIdIn(ids);
	}
	@Transactional
	public ArrayList<Long> getUsersIds(List<Long> ids){
		return usersRepo.findAllIdsByIdIn(ids);
	}

	@Transactional
	public User getUser(Authentication auth){
		ChatPrincipal chatPrincipal = (ChatPrincipal)auth.getPrincipal();
		return chatPrincipal.getIntitaUser();
	}
	@Transactional
	public List<User> getUsersFistNWhereUserNotIn(String login, List<Long> logins, int count){
		String loginLike = "%" + login + "%";
		return  usersRepo.findByNickNameLikeOrLoginLikeOrFirstNameLikeOrSecondNameLikeAndNotInCustom(loginLike, loginLike, loginLike,loginLike, logins, new PageRequest(0, count));
	}

	@Transactional
	public List<String> getUsersEmailsFist5WhereUserNotIn(String login, List<Long> logins){
		List<User> users = getUsersFistNWhereUserNotIn(login, logins, 5);
		List<String> emails = new ArrayList<String>();
		for(int i = 0; i < users.size(); i++)
			emails.add(users.get(i).getEmail());
		return emails;

	}

	@Transactional
	public List<User> getUsersFistN(String login, int count){
		String loginLike = "%" + login + "%";
		return usersRepo.findByNickNameLikeOrLoginLikeOrFirstNameLikeOrSecondNameLike( loginLike,loginLike,loginLike,loginLike, new PageRequest(0, count));
	}

	@Transactional
	public List<User> getUsersFistNWithRole(String info, UserRole role, int count){
		ArrayList<Long> list =  getAllByRole(role);
		String loginLike = "%" + info + "%";
		return usersRepo.findByNickNameLikeOrLoginLikeOrFirstNameLikeOrSecondNameLikeAndIdIn(loginLike,loginLike,loginLike,loginLike, list, new PageRequest(0, count));
	}

	@Transactional
	public List<String> getUsersEmailsFist5(String login){
		List<User> users = getUsersFistN(login, 5);
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
	public void register(String login, String pass) {
		String passHash = new ShaPasswordEncoder().encodePassword(pass, null);
		//encode(pass);
		//String passHash = pass;

		User u = new User(login, passHash);


		usersRepo.save(u);
	}
	@Transactional(readOnly = false)
	public void register(String login, String pass,Permissions permission) {
		String passHash = new BCryptPasswordEncoder().encode(pass);
		//String passHash = pass;
		User u = new User(login, passHash);
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
		if (id==null) return false;
		if(usersRepo.findInAdminTable(id) != null)
			return true;
		return false;
	}
	public boolean isAdmin(User u){
		if (u==null) return false;
		return isAdmin(u.getId());
	}
	@Transactional
	public boolean isSuperVisor(Long id){
		if (id==null) return false;
		if(usersRepo.findInSupervisorTable(id) != null)
			return true;
		return false;
	}
	@Transactional
	public boolean isTenant(Long id){
		if (id==null) return false;
		if(usersRepo.findInTenantTable(id) != null)
			return true;
		return false;
	}
	@Transactional
	public boolean isTrainer(Long id){
		if (id==null) return false;
		if(usersRepo.findInTrainerTable(id) != null)
			return true;
		return false;
	}
	@Transactional
	public boolean isStudent(Long id){
		if (id==null) return false;
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
		ArrayList<Integer> studentsIdList = usersRepo.getStudentsByTeacherId(id);
		if(studentsIdList != null)
		{
			ArrayList<User> studentsList = new ArrayList<>();
			for (Integer studentId : studentsIdList) {
				User student = usersRepo.findFisrtById(new Long(studentId));
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

	@Transactional
	public User getTrenerByStudent(Long studentIntitaId){
		Long trenerId = usersRepo.getTrainerByUserId(studentIntitaId);
		if(trenerId != null)
			return getUser(trenerId);
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
	@Transactional
	public HashSet<Integer> getAllTrainersIds(){
		return usersRepo.findAllTrainers();
	}

	/**
	 * Returns all online & free tenants, ignoring users from argument ArrayList
	 * @return
	 */
	@Transactional
	public ArrayList<ChatUser> getAllFreeTenants(Long... ignoreUsers){
		ArrayList<Long> all = new ArrayList<Long>(Arrays.asList(usersRepo.findAllTenants()));//WTF
		ArrayList<Long> free = new ArrayList<Long>();
		List<Long> ignoreList = Arrays.asList(ignoreUsers);
		for (Long chatUserId : all ){
			if(chatUserId==null)continue;
			if(!ignoreList.contains(chatUserId) && chatTenantService.isTenantAvailable(chatUserId))
				free.add(chatUserId);

		}
		return new ArrayList<>(chatUsersService.getUsers(free));
	}

	public ArrayList<LoginEvent> getAllFreeTenantsLoginEvent(Long... ignoreUsers){
		ArrayList<ChatUser> tenants = getAllFreeTenants(ignoreUsers);
		ArrayList<LoginEvent> loginEvents = new ArrayList<LoginEvent>();
		for(ChatUser user : tenants) loginEvents.add(chatUsersService.getLoginEvent(user));
		return loginEvents;
	}

	public ArrayList<ChatUserDTO> getAllFreeTenantsDTO(Long... ignoreUsers){
		ArrayList<ChatUser> tenants = getAllFreeTenants(ignoreUsers);
		ArrayList<ChatUserDTO> loginEvents = new ArrayList<ChatUserDTO>();
		for(ChatUser user : tenants) loginEvents.add(dtoMapper.map(user));
		return loginEvents;
	}


	@Transactional
	public boolean checkRoleByUser(User user, UserRole role){
		if(user == null)
			return false;
		String tableName = role.getTableName();
		String columnUserName = "id_user";
		if (tableName.equals("user_tenant"))
			columnUserName = "chat_user_id";
		Query query = entityManager.createNativeQuery("SELECT "+columnUserName+" FROM " + tableName + " WHERE " +columnUserName+" = " + user.getId() + " AND ((start_date <= NOW() AND end_date >= NOW()) OR end_date IS NULL) LIMIT 1");
		try{
			Object queryResult = query.getSingleResult();
			String userIdStr = queryResult.toString();
			if (userIdStr.length()<1)
				return false;
			Long userId = Long.parseLong(userIdStr);
			return userId.equals(user.getId());
		}
		catch (NoResultException e) {
			return false;
		}
		catch (Exception e) {
			e.printStackTrace();
			return false;
		}

	}
	@Transactional
	public boolean checkRoleByAuthentication(Authentication auth, UserRole role){
		ChatPrincipal principal = (ChatPrincipal)auth.getPrincipal();
		return checkRoleByUser(principal.getIntitaUser(),role);

	}


	public Set<UserRole> getAllRoles(User user){
		Set<UserRole> roles = new HashSet<UserRole>();
		for(UserRole role : UserRole.values()){
			if (checkRoleByUser(user,role))
				roles.add(role);
		}
		return roles;
	}



	@Transactional
	public boolean checkRole(User user, UserRole role){
		if (user==null)return false;
		Query query = entityManager.createNativeQuery("SELECT id_user FROM " + role.getTableName() + " WHERE id_user = " + user.getId() + " AND ((start_date <= NOW() AND end_date >= NOW()) OR end_date IS NULL) LIMIT 1");
		Long userId = null;
		try {
			userId = Long.parseLong(query.getSingleResult().toString());
		}
		catch(Exception e){
			return false;
		}
		return userId.equals(user.getId());

	}
	@Transactional
	public ArrayList<Long> getAllByRole(UserRole role){
		String userFieldName = "id_user";
		if(role == UserRole.TENANTS)
			userFieldName = "chat_user_id";
		Query query = entityManager.createNativeQuery("SELECT " + userFieldName + " FROM " + role.getTableName() + " WHERE ((start_date <= NOW() AND end_date >= NOW()) OR end_date IS NULL) ");
		List<Object> queryResults = query.getResultList();

		ArrayList<Long> resultArray = new ArrayList<>();
		for(Object queryObject : queryResults)
		{
			Long userId = Long.parseLong(queryObject.toString());
			resultArray.add(userId);
		}
		return resultArray;
	}

	@Transactional
	public ArrayList<Long> getAllByRoleValue(int roleValue,String tableName){
		String userFieldName = "id_user";
		if(roleValue == UserRole.TENANTS.getValue())
			userFieldName = "chat_user_id";
		Query query = entityManager.createNativeQuery("SELECT DISTINCT " + userFieldName + " FROM " + tableName + " WHERE ((start_date <= NOW() AND end_date >= NOW()) OR end_date IS NULL) ");
		List<Object> queryResults = null;
		try{
			queryResults = query.getResultList();
		}
		catch(Exception e) {
			//e.printStackTrace();
			return new ArrayList<Long>();
		}

		ArrayList<Long> resultArray = new ArrayList<>();
		for(Object queryObject : queryResults)
		{
			Long userId = Long.parseLong(queryObject.toString());
			resultArray.add(userId);
		}
		return resultArray;
	}

	@Transactional
	public Page<User> getChatUsers(int page, int pageSize){
		return usersRepo.findAllChatUsers(new PageRequest(page-1, pageSize)); 

	}
	public Long getUsersCount(){
		return usersRepo.count();
	}	
	/*
	 * Birthday functionality
	 */
	@Transactional
	public Long[] getAllUserWithBirthdayToday(){
		return usersRepo.findAllByBirthdayToday();
	}
	
	@Transactional
	public ArrayList<Long> getAllUserWithBirthday(Date date){
		return usersRepo.findAllByBirthday(date, null);
	}
	@Transactional
	public ArrayList<Long> getAllUserWithBirthdayAndInList(Date date, List<Long> users){
		return usersRepo.findAllByBirthdayAndIdIn(date, users, null);
	}
}

