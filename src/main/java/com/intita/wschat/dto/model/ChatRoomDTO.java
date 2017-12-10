package com.intita.wschat.dto.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonView;
import com.intita.wschat.dto.interfaces.DataTransferObject;
import com.intita.wschat.models.*;
import jsonview.Views;
import org.hibernate.validator.constraints.NotBlank;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.*;

/**
 * Created by roma on 18.04.17.
 */
public class ChatRoomDTO implements DataTransferObject {

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }


    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public short getType() {
        return type;
    }

    public void setType(short type) {
        this.type = type;
    }

    private Long id;

    private boolean active = true;

    private String name;

    private short type;

    public Date getLastVisit() {
        return lastVisit;
    }

    public ChatRoomDTO setLastVisit(Date lastVisit) {
        this.lastVisit = lastVisit;
        return this;
    }

    private Date lastVisit;

    public boolean isEmailNotification() {
        return emailNotification;
    }

    public ChatRoomDTO setEmailNotification(boolean emailNotification) {
        this.emailNotification = emailNotification;
        return this;
    }

    private boolean emailNotification = true;



}
