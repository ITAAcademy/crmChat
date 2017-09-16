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

import com.intita.wschat.models.ChatUser;
import com.intita.wschat.models.Room;
import com.intita.wschat.models.UserMessage;

@Qualifier("IntitaConf") 
public interface UserMessageRepository  extends CrudRepository<UserMessage, Long>{
	  Page<UserMessage> findById(Long id, Pageable pageable);
	  ArrayList<UserMessage> findAll(Pageable pageable);
	  ArrayList<UserMessage> findByAuthor(ChatUser author);
	  ArrayList<UserMessage> findByRoom(Room room);
	  ArrayList<UserMessage> findFirst20ByRoomOrderByIdDesc(Room room);
	  ArrayList<UserMessage> findFirst20ByRoomAndDateAfterOrderByIdDesc(Room room, Date date);

	  ArrayList<UserMessage> findFirst10ByRoomAndDateAfter(Room room, Date date);

	ArrayList<UserMessage> findAllByAuthorAndDateIsBetween(ChatUser user, Date date1,Date date2);
	ArrayList<UserMessage> findAllByAuthorAndRoomAndDateIsBetween(ChatUser user,Room room, Date date1,Date date2);
	  ArrayList<UserMessage> findAllByRoomAndDateAfterAndActiveIsTrue(Room room, Date date);
	  ArrayList<UserMessage> findAllByRoomAndDateAfterAndAuthorNot(Room room, Date date, ChatUser user);
	  Long countByRoomAndDateAfterAndAuthorNot(Room room, Date date, ChatUser user);
	  Long countByDateAfterAndDateBefore(Date dateEarlier, Date dateLater);
	Long countByDateAfterAndDateBeforeAndActive(Date dateEarlier, Date dateLater,boolean active);
	  List<UserMessage> findAllByDateAfterAndAuthorNot( Date date, ChatUser user);
	  Set<UserMessage> findAllByAuthorNot(ChatUser user);
	  ArrayList<UserMessage> findAllByDateAfter(Date date);
	  UserMessage findFirstByRoomOrderByDateDesc(Room room);
	  @Query("select m.date from chat_user_message m where m.author.id=?1 and Date(m.date) >= Date(?2) and Date(m.date) <= Date(?3)")
	  List<Date> getMessagesDatesByChatUserAndDateBetween(Long userId,Date earlyDate,Date lastDate);
	@Query("select m.date from chat_user_message m where m.author.id=?1 and m.room.id=?2 and Date(m.date) >= Date(?3) and Date(m.date) <= Date(?4)")
	List<Date> getMessagesDatesByChatUserAndRoomAndDateBetween(Long userId,Long roomId,Date earlyDate,Date lastDate);

	  Long countByIdAndAuthorId(Long messageId,Long authorId);
}
