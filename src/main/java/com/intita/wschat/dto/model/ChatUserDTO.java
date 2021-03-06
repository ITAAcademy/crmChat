package com.intita.wschat.dto.model;

import com.fasterxml.jackson.annotation.JsonView;
import com.intita.wschat.domain.UserRole;
import com.intita.wschat.dto.interfaces.DataTransferObject;
import com.intita.wschat.models.*;
import jsonview.Views;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;

import javax.persistence.*;
import javax.validation.constraints.Size;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by roma on 18.04.17.
 */
public class ChatUserDTO implements DataTransferObject {

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNickName() {
        return nickName;
    }

    public void setNickName(String nickName) {
        this.nickName = nickName;
    }

    private Long id;
    private Long intitaUserId;
    private String nickName;
    String avatar;

    public Set<UserRole> getRoles() {
        return roles;
    }

    public void setRoles(Set<UserRole> roles) {
        this.roles = roles;
    }

    private Set<UserRole> roles;

    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

    public Long getIntitaUserId() {
        return intitaUserId;
    }

    public void setIntitaUserId(Long intitaUserId) {
        this.intitaUserId = intitaUserId;
    }

}
