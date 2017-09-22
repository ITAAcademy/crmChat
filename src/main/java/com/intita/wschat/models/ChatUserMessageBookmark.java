package com.intita.wschat.models;

import javax.persistence.*;

@Entity
@Table(name="chat_bookmark_user_message")
public class ChatUserMessageBookmark {
    public Long getId() {
        return id;
    }

    public ChatUserMessageBookmark setId(Long id) {
        this.id = id;
        return this;
    }

    public ChatUser getChatUser() {
        return chatUser;
    }

    public ChatUserMessageBookmark setChatUser(ChatUser chatUser) {
        this.chatUser = chatUser;
        return this;
    }

    public Room getChatRoom() {
        return chatRoom;
    }

    public ChatUserMessageBookmark setChatRoom(Room chatRoom) {
        this.chatRoom = chatRoom;
        return this;
    }

    public UserMessage getChatMessage() {
        return chatMessage;
    }

    public ChatUserMessageBookmark setChatMessage(UserMessage chatMessage) {
        this.chatMessage = chatMessage;
        return this;
    }

    @Id
    @GeneratedValue
    private Long id;

    @ManyToOne( fetch = FetchType.LAZY)
    private ChatUser chatUser;
    @ManyToOne( fetch = FetchType.LAZY)
    private Room chatRoom;
    @ManyToOne( fetch = FetchType.LAZY)
    private UserMessage chatMessage;



}
