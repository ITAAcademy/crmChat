package com.intita.wschat.models;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import javax.persistence.*;
import java.util.Date;

@Entity
public class RoomConfig {

    public Long getId() {
        return id;
    }

    public RoomConfig setId(Long id) {
        this.id = id;
        return this;
    }

    @Id
    @GeneratedValue
    private Long id;

    public Room getRoom() {
        return room;
    }

    public RoomConfig setRoom(Room room) {
        this.room = room;
        return this;
    }
    @ManyToOne
    private Room room;

    @ManyToOne
    private ChatUser user;

    public ChatUser getUser() {
        return user;
    }

    public RoomConfig setUser(ChatUser user) {
        this.user = user;
        return this;
    }

    public boolean isEmailNotification() {
        return emailNotification;
    }

    public RoomConfig setEmailNotification(boolean emailNotification) {
        this.emailNotification = emailNotification;
        return this;
    }

    private boolean emailNotification = false;

    public Date getUpdateAt() {
        return updateAt;
    }

    public RoomConfig setUpdateAt(Date updateAt) {
        this.updateAt = updateAt;
        return this;
    }
    @UpdateTimestamp
    private Date updateAt;
}
