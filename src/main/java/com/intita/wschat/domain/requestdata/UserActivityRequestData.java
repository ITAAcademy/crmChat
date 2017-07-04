package com.intita.wschat.domain.requestdata;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Created by roma on 23.04.17.
 */
public class UserActivityRequestData {
    public Long getChatUserId() {
        return chatUserId;
    }

    public void setChatUserId(Long chatUserId) {
        this.chatUserId = chatUserId;
    }

    public Long getBeforeDate() {
        return beforeDate;
    }

    public void setBeforeDate(Long beforeDate) {
        this.beforeDate = beforeDate;
    }

    public Long getAfterDate() {
        return afterDate;
    }

    public void setAfterDate(Long afterDate) {
        this.afterDate = afterDate;
    }

    private Long chatUserId;
    private Long beforeDate;
    private Long afterDate;
}
