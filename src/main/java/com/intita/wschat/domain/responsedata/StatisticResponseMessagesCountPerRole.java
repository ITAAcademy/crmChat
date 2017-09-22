package com.intita.wschat.domain.responsedata;

import java.util.Map;

public class StatisticResponseMessagesCountPerRole {
    public Map<String, Integer> getMessageCountPerRole() {
        return messageCountPerRole;
    }

    public StatisticResponseMessagesCountPerRole setMessageCountPerRole(Map<String, Integer> messageCountPerRole) {
        this.messageCountPerRole = messageCountPerRole;
        return this;
    }

    public String getRequestId() {
        return requestId;
    }

    public StatisticResponseMessagesCountPerRole setRequestId(String requestId) {
        this.requestId = requestId;
        return this;
    }

    Map<String,Integer> messageCountPerRole;
    private String requestId;
}
