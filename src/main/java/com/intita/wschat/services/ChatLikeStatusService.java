package com.intita.wschat.services;

import com.intita.wschat.enums.LikeState;
import com.intita.wschat.models.ChatLikeStatus;
import com.intita.wschat.models.ChatUser;
import com.intita.wschat.models.UserMessage;
import com.intita.wschat.repositories.ChatLikeStatusRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Set;

/**
 * Created by roma on 22.06.17.
 */
@Service
public class ChatLikeStatusService {
    @Autowired
    private ChatLikeStatusRepository chatLikeStatusRepository;

    @Autowired
    private UserMessageService userMessageService;
    @Autowired
    private RoomsService roomsService;

    public Long getMessageLikesCount(Long messageId){
    Long count = chatLikeStatusRepository.countLikesByMessage(messageId,LikeState.LIKE);
    return count;
    }

    public Long getMessageDislikesCount(Long messageId){
        Long count = chatLikeStatusRepository.countLikesByMessage(messageId,LikeState.DISLIKE);
        return count;
    }

    public LikeState getLikeState(Long messageId, Long chatUserId){
        LikeState likeState = chatLikeStatusRepository.getChatLikeStateByMessageAndChatUser(messageId,chatUserId);
        if (likeState==null)
        {
            return LikeState.EMPTY;
        }
        return likeState;
    }

    public boolean likeMessage(Long messageId, ChatUser chatUser){
        return rateMessage(messageId,chatUser,LikeState.LIKE);
    }
    public boolean dislikeMessage(Long messageId, ChatUser chatUser){
        return rateMessage(messageId,chatUser,LikeState.DISLIKE);
    }
    private boolean rateMessage(Long messageId, ChatUser chatUser, LikeState state) {
       ChatLikeStatus likeStatus = chatLikeStatusRepository.getChatLikeByMessageAndChatUser(messageId,chatUser.getId());
       if (likeStatus==null) {
           UserMessage m = userMessageService.getUserMessage(messageId);
           boolean isParticipant = roomsService.isRoomParticipant(chatUser.getId(), m.getRoom().getId());
           if (!isParticipant) return false;
           likeStatus = new ChatLikeStatus(m.getId(), chatUser.getId(), state);
       } else
       {
           likeStatus.setLikeState(state);
       }
        chatLikeStatusRepository.save(likeStatus);
        return true;
    }
    public Set<Long> getLikedMessagesIdsByUserInRoom(Long chatUserId, Long roomId){
       return chatLikeStatusRepository.getMessageIdsByLikeState(roomId,chatUserId,LikeState.LIKE);
    }
    public Set<Long> getDislikedMessagesIdsByUserInRoom(Long chatUserId, Long roomId){
        return chatLikeStatusRepository.getMessageIdsByLikeState(roomId,chatUserId,LikeState.DISLIKE);
    }

}
