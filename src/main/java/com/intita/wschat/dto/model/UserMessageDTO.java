package com.intita.wschat.dto.model;

import com.intita.wschat.dto.interfaces.DataTransferObject;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by roma on 22.06.17.
 */
public class UserMessageDTO implements DataTransferObject {
    private ChatUserDTO author;

    public ChatUserDTO getAuthor() {
        return author;
    }

    public void setAuthor(ChatUserDTO author) {
        this.author = author;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public ArrayList<String> getAttachedFiles() {
        return attachedFiles;
    }

    public void setAttachedFiles(ArrayList<String> attachedFiles) {
        this.attachedFiles = attachedFiles;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    private String body;
    private Date date;

    public Date getUpdateat() {
        return updateat;
    }

    public void setUpdateat(Date updateat) {
        this.updateat = updateat;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public Long getRoomId() {
        return roomId;
    }

    public UserMessageDTO setRoomId(Long roomId) {
        this.roomId = roomId;
        return this;
    }

    private Long roomId;
    private Date updateat;
    private boolean active;
    private ArrayList<String> attachedFiles;
    private Long id;

}
