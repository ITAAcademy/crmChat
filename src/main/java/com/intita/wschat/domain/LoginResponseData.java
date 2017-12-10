package com.intita.wschat.domain;

import com.intita.wschat.dto.model.ChatRoomDTO;
import com.intita.wschat.dto.model.ChatUserDTO;
import com.intita.wschat.models.RoomModelSimple;
import com.intita.wschat.services.NotificationsService.ChatNotification;

import java.util.List;
import java.util.Set;

import javax.management.Notification;

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

    public List<ChatNotification> getNotifications() {
		return notifications;
	}

	public void setNotifications(List<ChatNotification> notifications) {
		this.notifications = notifications;
	}

    public Long getDestinationRoomId() {
        return destinationRoomId;
    }

    public LoginResponseData setDestinationRoomId(Long destinationRoomId) {
        this.destinationRoomId = destinationRoomId;
        return this;
    }

    public List<ChatRoomDTO> getRooms() {
        return rooms;
    }

    public LoginResponseData setRooms(List<ChatRoomDTO> rooms) {
        this.rooms = rooms;
        return this;
    }

	String nextWindow;
    String userAvatar;
    Set<Long> activeUsers;
    ChatUserDTO chatUser;
    List<ChatRoomDTO> rooms;
    List<ChatUserDTO> tenants;
    List<ChatNotification> notifications;
    ChatUserDTO trainer;
    Long destinationRoomId;

}
