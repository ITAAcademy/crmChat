package com.intita.wschat.repositories;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import com.intita.wschat.models.ChatTenant;
import com.intita.wschat.models.ChatUser;

@Qualifier("IntitaConf") 
public interface ChatTenantRepository extends CrudRepository<ChatTenant, Long> {
	   Page<ChatTenant> findById(Long id, Pageable pageable);
	   ChatTenant findById(Long id);
	   ChatTenant findByChatUser(ChatUser user);
	   Long findIdByChatUser(ChatUser user);
	   Page<ChatTenant> findAll(Pageable pageable);
	   List<ChatTenant> findAll();
	   Set<ChatTenant> findDistinctByEndDateAfterOrEndDateIsNull(Date date);
	   @Query(value="select t from user_tenant t where t.chatUser.id in :usersIds")
	   List<ChatTenant> findWhereChatUserIdInList(@Param("usersIds") ArrayList usersIds);
	   ArrayList<ChatTenant> findAllByEndDateAfterOrEndDateIsNull(Date date);
	 /*  ChatTenant findOneByIntitaUser(User user);
	   List<ChatTenant> findFirst10ByIdNotIn(List<Long> users);
	 //  List<User> findFirst5ByLoginAndByPassword( String users, String login);
	   List<ChatTenant> findFirst5ByNickNameNotInAndNickNameLike( List<String> users, String login);*/
	 }
