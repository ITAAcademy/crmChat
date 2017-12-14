package com.intita.wschat.repositories;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.query.Procedure;
import org.springframework.data.repository.CrudRepository;

import com.intita.wschat.models.ChatUser;
import com.intita.wschat.models.Room;
import com.intita.wschat.models.User;
import org.springframework.data.repository.query.Param;


@Qualifier("IntitaConf") 
public interface RoomRepository extends JpaRepository<Room, Long> {
	Room findByName(String name);
	//User findByEmail(String email);
	Page<Room> findById(Long id, Pageable pageable);
	Room findById(Long id);
	
	Page<Room> findAll(Pageable pageable);
	List<Room> findFirst10ByIdNotIn(List<Long> users);
	
	Room findByAuthorAndTypeAndUsersContaining(ChatUser author, short type, ChatUser privateUser);
	ArrayList<Room> findByAuthor(ChatUser author);
	ArrayList<Room> findByUsersContaining(ChatUser user);
	List<Room> findByAuthorOrUsersContaining(ChatUser user, ChatUser author);

	@Query("select room from ChatRoom room where" +
			" (:user in elements(room.users) or room.author = :user)" +
			" and lower(room.name) like lower(concat('%',:likeStr,'%')) ")
	List<Room> findRoomsOfUser(@Param("user") ChatUser user,@Param("likeStr") String likeStr, Pageable pageable);
	
	ArrayList<Room> findFirst10ByNameLike(String like);
	
	@Query("select r from ChatRoom r where r.id in ?1")
	ArrayList<Room> findRoomsByIds(ArrayList<Long> ids);

	@Modifying(clearAutomatically = true)
	@Query("update ChatRoom r set r.name = ?2 where r.id = ?1")
	void setRoomName(Long roomId, String roomName);


	@Query(value="SELECT count(room) FROM ChatRoom room WHERE room = ?2 AND ((room.author = ?1) OR (?1 IN (SELECT users FROM room.users users)))")
	Long countByAuthorOrInUsers(ChatUser participant,Room room);

	@Query(value="SELECT r.users FROM ChatRoom r WHERE r = ?1 ")
	List<ChatUser> getChatUsers(Room room, Pageable pageable);

	@Query(value="SELECT id FROM chat_user WHERE id IN (SELECT users_id FROM chat_room_users WHERE rooms_from_users_id = ?1 AND users_id > ?2 ) LIMIT ?3 ",nativeQuery = true)
	List<Integer> getChatUsersIdsWherechatUserIdAfter(Long roomId,Long chatUserId, int count);

	@Query(value="SELECT room_id FROM chat_user_message as mesage WHERE author_id = ?#{#user.id} AND (SELECT active FROM chat_room WHERE id = room_id) = true ORDER BY date DESC LIMIT 1",nativeQuery = true)
	Long findChatUserRoomWithLastUserMessage(@Param("user") ChatUser user);

	@Query
	List<Object[]> countNewMessages(Long chatUserId);
	@Query
	List<Object[]> findLastMessages(Long chatUserId);
}