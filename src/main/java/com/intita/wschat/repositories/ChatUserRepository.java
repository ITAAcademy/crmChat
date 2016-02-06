package com.intita.wschat.repositories;

import java.util.List;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.CrudRepository;

import com.intita.wschat.models.ChatUser;
import com.intita.wschat.models.Room;
import com.intita.wschat.models.User;

@Qualifier("IntitaConf") 
public interface ChatUserRepository extends CrudRepository<ChatUser, Long> {
	ChatUser findOneByNickName(String nickName);
	   //User findByEmail(String email);
	   Page<ChatUser> findById(Long id, Pageable pageable);
	   ChatUser findById(Long id);
	   Page<ChatUser> findAll(Pageable pageable);
	   ChatUser findOneByIntitaUser(User user);
	   List<ChatUser> findFirst10ByIdNotIn(List<Long> users);
	 //  List<User> findFirst5ByLoginAndByPassword( String users, String login);
	   List<ChatUser> findFirst5ByNickNameNotInAndNickNameLike( List<String> users, String login);
	  
	 }
