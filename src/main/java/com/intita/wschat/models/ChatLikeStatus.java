package com.intita.wschat.models;

import com.intita.wschat.enums.LikeState;

import javax.persistence.*;
import java.util.Date;

/**
 * Created by roma on 22.06.17.
 */
@Entity(name = "chat_like_status")
@Table(
        name = "chat_like_status",
        uniqueConstraints={@UniqueConstraint(columnNames = {"message_id","chat_user_id"})}
)
public class ChatLikeStatus {
    public ChatLikeStatus(){

    }

    public ChatLikeStatus(UserMessage message) {
        this.message = message;
        this.date = new Date();
    }
    public ChatLikeStatus(Long messageId,Long chatUserId,LikeState state) {
        this.message = UserMessage.forId(messageId);
        this.chatUser = ChatUser.forId(chatUserId);
        this.date = new Date();
        this.likeState = state;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public UserMessage getMessage() {
        return message;
    }

    public void setMessage(UserMessage message) {
        this.message = message;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }
    public LikeState getLikeState() {
        return likeState;
    }
    public void setLikeState(LikeState likeState) {
        this.likeState = likeState;
    }
    @Id
    @GeneratedValue
    private Long id;
    
    @ManyToOne
    @JoinColumn(name = "message_id")
    private UserMessage message;
    
    private Date date;
    
    @Column(name="like_state")
    private LikeState likeState;
    
    @ManyToOne
    @JoinColumn(name = "chat_user_id")
    private ChatUser chatUser;
}
