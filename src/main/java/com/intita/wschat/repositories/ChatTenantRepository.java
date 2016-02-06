package com.intita.wschat.repositories;

import java.util.List;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.CrudRepository;

import com.intita.wschat.models.ChatTenant;
import com.intita.wschat.models.User;

@Qualifier("IntitaConf") 
public interface ChatTenantRepository extends CrudRepository<ChatTenant, Long> {
	   Page<ChatTenant> findById(Long id, Pageable pageable);
	   ChatTenant findById(Long id);
	   Page<ChatTenant> findAll(Pageable pageable);
	 /*  ChatTenant findOneByIntitaUser(User user);
	   List<ChatTenant> findFirst10ByIdNotIn(List<Long> users);
	 //  List<User> findFirst5ByLoginAndByPassword( String users, String login);
	   List<ChatTenant> findFirst5ByNickNameNotInAndNickNameLike( List<String> users, String login);*/
	 }
