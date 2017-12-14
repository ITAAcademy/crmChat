package com.intita.wschat.domain;

import java.util.Date;

public class ChatRoomDetails {
    public Long getRoomId() {
        return roomId;
    }

    public ChatRoomDetails setRoomId(Long roomId) {
        this.roomId = roomId;
        return this;
    }

    public int getNewMessagesCount() {
        return newMessagesCount;
    }

    public ChatRoomDetails setNewMessagesCount(int newMessagesCount) {
        this.newMessagesCount = newMessagesCount;
        return this;
    }

    public String getLastMessageBody() {
        return lastMessageBody;
    }

    public ChatRoomDetails setLastMessageBody(String lastMessage) {
        this.lastMessageBody = lastMessage;
        return this;
    }

    public Long getLastMessageDate() {
        return lastMessageDate;
    }

    public ChatRoomDetails setLastMessageDate(Long lastMessageDate) {
        this.lastMessageDate = lastMessageDate;
        return this;
    }

    private Long roomId;
    private int newMessagesCount;
    private String lastMessageBody;
    private Long lastMessageDate;

    public boolean isEmailNotification() {
        return emailNotification;
    }

    public ChatRoomDetails setEmailNotification(boolean emailNotification) {
        this.emailNotification = emailNotification;
        return this;
    }

    private boolean emailNotification;
}
