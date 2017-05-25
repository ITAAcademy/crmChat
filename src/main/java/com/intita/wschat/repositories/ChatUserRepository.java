package com.intita.wschat.repositories;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Repository;

import com.intita.wschat.models.ChatUser;
import com.intita.wschat.models.Room;
import com.intita.wschat.models.User;

@Qualifier("IntitaConf") 

public interface ChatUserRepository extends CrudRepository<ChatUser, Long> {
	ChatUser findOneByNickName(String nickName);
	ChatUser findFirstByIntitaUser(User user);
	//User findByEmail(String email);
	Page<ChatUser> findById(Long id, Pageable pageable);
	ChatUser findById(Long id);
	
	ArrayList<ChatUser> findAllByIdIn(List<Long> users);
	
	Page<ChatUser> findAll(Pageable pageable);
	ChatUser findOneByIntitaUser(User user);
	List<ChatUser> findFirst10ByIdNotIn(List<Long> users);
	//  List<User> findFirst5ByLoginAndByPassword( String users, String login);
	List<ChatUser> findFirst5ByNickNameNotInAndNickNameLike( List<String> users, String nickName);
	List<ChatUser> findFirst5ByNickName(String nickName);
	@Query("select u from chat_user u where u.nickName like ?1  and u.id not in ?2")
	List<ChatUser> findFirst5ByNickNameExcludeList( String nickName, List<Long> excludedChatUsers, Pageable pageable);
	List<ChatUser> findFirst5ByNickNameLike(String nickName);
	ChatUser findFirstByRoomsContainingOrRoomsFromUsersContainingAndId(ArrayList<Room> room, ArrayList<Room> room2, Long user_id);
	@Query("select u from chat_user u where u.intitaUser.id in ?1")
	ArrayList<ChatUser> findChatUsersByIntitaUsers(ArrayList<Long> intitaUsersIds);
	@Query("select u from chat_user u where u.nickName like %?1% or (u.intitaUser is not null and (u.intitaUser.nickName like %?1% or u.intitaUser.firstName like %?1% or u.intitaUser.secondName like %?1% or u.intitaUser.login like %?1%))")
	ArrayList<ChatUser> findChatUserByNameAndEmail(String name);
}
