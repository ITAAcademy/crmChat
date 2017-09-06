package com.intita.wschat.domain.search;

import com.intita.wschat.models.ChatUser;
import com.intita.wschat.models.Room;

import java.util.Date;
import java.util.Optional;

public class UserMessageSearchCriteria {

    public UserMessageSearchCriteria(){

    }

    public  boolean  haveAnyParameterDefined(){
        return authorId != null || roomId != null  || searchValue != null
                || earlyDate != null  || lateDate != null;
    }

    public Long getAuthorId() {
        return authorId;
    }

    public UserMessageSearchCriteria setAuthorId(Long authorId) {
        this.authorId = authorId;
        return this;
    }

    public Long getRoomId() {
        return roomId;
    }

    public UserMessageSearchCriteria setRoomId(Long roomId) {
        this.roomId = roomId;
        return this;
    }

    public String getSearchValue() {
        return searchValue;
    }

    public UserMessageSearchCriteria setSearchValue(String searchValue) {
        this.searchValue = searchValue;
        return this;
    }

    public Date getEarlyDate() {
        return earlyDate;
    }

    public UserMessageSearchCriteria setEarlyDate(Date earlyDate) {
        this.earlyDate = earlyDate;
        return this;
    }

    public Date getLateDate() {
        return lateDate;
    }

    public UserMessageSearchCriteria setLateDate(Date lateDate) {
        this.lateDate = lateDate;
        return this;
    }

    Long authorId;
    Long roomId;
    String searchValue;
    Date earlyDate;
    Date lateDate;
}
