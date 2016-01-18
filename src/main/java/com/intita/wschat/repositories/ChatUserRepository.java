package com.intita.wschat.repositories;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.CrudRepository;

import com.intita.wschat.models.ChatUser;
import com.intita.wschat.models.User;

public interface ChatUserRepository extends CrudRepository<ChatUser, Long> {
	List<ChatUser> findByNickName(String nickName);
	   //User findByEmail(String email);
	   Page<ChatUser> findById(Long id, Pageable pageable);
	   ChatUser findById(Long id);
	   Page<ChatUser> findAll(Pageable pageable);
	   List<ChatUser> findFirst10ByIdNotIn(List<Long> users);
	 //  List<User> findFirst5ByLoginAndByPassword( String users, String login);
	   List<ChatUser> findFirst5ByNickNameNotInAndNickNameLike( List<String> users, String login);
	 }