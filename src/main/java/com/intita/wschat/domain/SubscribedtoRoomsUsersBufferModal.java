package com.intita.wschat.domain;

import com.intita.wschat.models.ChatUser;
import com.intita.wschat.models.Room;

import java.util.ArrayList;

/**
 * Created by roma on 27.06.17.
 */
public class SubscribedtoRoomsUsersBufferModal {
    public ChatUser getChatUser() {
        return chatUser;
    }

    public void setChatUser(ChatUser chatUser) {
        this.chatUser = chatUser;
    }

    public boolean isReplace() {
        return replace;
    }

    public void setReplace(boolean replace) {
        this.replace = replace;
    }

    public ArrayList<Room> getRoomsForUpdate() {
        return roomsForUpdate;
    }

    public void setRoomsForUpdate(ArrayList<Room> roomsForUpdate) {
        this.roomsForUpdate = roomsForUpdate;
    }

    ChatUser chatUser;
    boolean replace = true;
    ArrayList<Room> roomsForUpdate;

    public SubscribedtoRoomsUsersBufferModal() {
        chatUser = null;
    }

    public SubscribedtoRoomsUsersBufferModal(ChatUser chatUser, ArrayList<Room> arr) {
        this.chatUser = chatUser;
        replace = false;
        roomsForUpdate = arr;

    }

    public SubscribedtoRoomsUsersBufferModal(ChatUser chatUser) {
        this.chatUser = chatUser;
        replace = true;
    }
}
