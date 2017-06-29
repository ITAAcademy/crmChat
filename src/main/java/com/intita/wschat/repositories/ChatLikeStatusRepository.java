package com.intita.wschat.repositories;

import com.intita.wschat.enums.LikeState;
import com.intita.wschat.models.ChatLikeStatus;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import javax.transaction.Transactional;
import java.util.Set;

/**
 * Created by roma on 22.06.17.
 */
public interface ChatLikeStatusRepository extends CrudRepository<ChatLikeStatus, Long> {
    @Query(value="SELECT COUNT(status.id) FROM chat_like_status status WHERE status.message.id=?1 AND status.likeState=?2 ")
    public Long countLikesByMessage(Long messageId,LikeState state);

    @Query(value="SELECT COUNT(id) FROM chat_like_status WHERE id=?1 AND author_id=?2 ",nativeQuery=true)
    public Long countLikesByMessageAndChatUser(Long messageId,Long chatUserId);

    @Query("SELECT status.likeState FROM chat_like_status status WHERE status.message.id = ?1 AND status.chatUser.id = ?2 ")
    public LikeState getChatLikeStateByMessageAndChatUser(Long messageId, Long chatUserId);

    @Query("SELECT status FROM chat_like_status status WHERE status.message.id = ?1 AND status.chatUser.id = ?2 ")
    public ChatLikeStatus getChatLikeByMessageAndChatUser(Long messageId, Long chatUserId);

    @Query("SELECT status.message.id FROM chat_like_status status WHERE status.message.room.id = ?1 AND status.chatUser.id = ?2 AND status.likeState = ?3")
    public Set<Long> getMessageIdsByLikeState(Long roomId,Long userId,LikeState state);

    @Modifying
    @Transactional
    @Query("DELETE FROM chat_like_status status WHERE status.message.id = ?1 AND status.chatUser.id = ?2 ")
    public void removeByMessageAndChatUser(Long messageId, Long chatUserId);

}
