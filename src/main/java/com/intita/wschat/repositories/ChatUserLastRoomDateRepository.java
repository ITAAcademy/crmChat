package com.intita.wschat.repositories;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.CrudRepository;

import com.intita.wschat.models.ChatTenant;
import com.intita.wschat.models.ChatUser;
import com.intita.wschat.models.ChatUserLastRoomDate;
import com.intita.wschat.models.Room;
import com.intita.wschat.models.User;

@Qualifier("IntitaConf") 
public interface ChatUserLastRoomDateRepository extends CrudRepository<ChatUserLastRoomDate, Long> {
	   Page<ChatUserLastRoomDate> findById(Long id, Pageable pageable);
	   ChatUserLastRoomDate findById(Long id);
	   ChatUserLastRoomDate findByRoomAndChatUser(Room room, ChatUser chatUser);
	   ChatUserLastRoomDate findFirstByRoomAndChatUser(Room room, ChatUser chatUser);
	   Page<ChatUserLastRoomDate> findAll(Pageable pageable);
	   List<ChatUserLastRoomDate> findAll();
	   List<ChatUserLastRoomDate> findByChatUser(ChatUser user);
		List<ChatUserLastRoomDate> findByChatUser(ChatUser user,Pageable pageable);
	   List<ChatUserLastRoomDate> findByRoom(Room user);
	   ChatUserLastRoomDate findFirstByRoomAndChatUserNotAndLastLogoutAfter(Room room, ChatUser user, Date after);
	   ChatUserLastRoomDate findFirstByRoomAndChatUserNotOrderByLastLogout(Room room, ChatUser user);
	   List<ChatUserLastRoomDate> findByChatUserAndRoomIn(ChatUser user, ArrayList<Room> rooms);
	 /*  ChatTenant findOneByIntitaUser(User user);
	   List<ChatTenant> findFirst10ByIdNotIn(List<Long> users);
	 //  List<User> findFirst5ByLoginAndByPassword( String users, String login);
	   List<ChatTenant> findFirst5ByNickNameNotInAndNickNameLike( List<String> users, String login);*/
	 }
