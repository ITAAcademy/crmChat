package com.intita.wschat.domain;

import com.intita.wschat.dto.model.ChatUserDTO;
import com.intita.wschat.models.RoomModelSimple;

import java.util.List;
import java.util.Set;

/**
 * Created by roma on 18.04.17.
 */
public class LoginResponseData {
    public String getNextWindow() {
        return nextWindow;
    }

    public void setNextWindow(String nextWindow) {
        this.nextWindow = nextWindow;
    }

    public String getUserAvatar() {
        return userAvatar;
    }

    public void setUserAvatar(String userAvatar) {
        this.userAvatar = userAvatar;
    }

    public Set<Long> getActiveUsers() {
        return activeUsers;
    }

    public void setActiveUsers(Set<Long> activeUsers) {
        this.activeUsers = activeUsers;
    }

    public ChatUserDTO getChatUser() {
        return chatUser;
    }

    public void setChatUser(ChatUserDTO chatUser) {
        this.chatUser = chatUser;
    }
    public List<RoomModelSimple> getRoomModels() {
        return roomModels;
    }

    public void setRoomModels(List<RoomModelSimple> roomModels) {
        this.roomModels = roomModels;
    }
    public ChatUserDTO getTrainer() {
        return trainer;
    }
    public void setTrainer(ChatUserDTO trainer) {
        this.trainer = trainer;
    }
    public List<ChatUserDTO> getTenants() {
        return tenants;
    }

    public void setTenants(List<ChatUserDTO> tenants) {
        this.tenants = tenants;
    }

    String nextWindow;
    String userAvatar;
    Set<Long> activeUsers;
    ChatUserDTO chatUser;
    List<RoomModelSimple> roomModels;
    List<ChatUserDTO> tenants;
    ChatUserDTO trainer;

}
