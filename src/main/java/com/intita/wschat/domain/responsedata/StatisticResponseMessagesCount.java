package com.intita.wschat.domain.responsedata;

public class StatisticResponseMessagesCount {
    public Long getTotalMessagesCount() {
        return totalMessagesCount;
    }

    public void setTotalMessagesCount(Long totalMessagesCount) {
        this.totalMessagesCount = totalMessagesCount;
    }

    public Long getActiveMessagesCount() {
        return activeMessagesCount;
    }

    public void setActiveMessagesCount(Long activeMessagesCount) {
        this.activeMessagesCount = activeMessagesCount;
    }

    private Long totalMessagesCount;
    private Long activeMessagesCount;

    public String getRequestId() {
        return requestId;
    }

    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }

    private String requestId;
}
