package com.intita.wschat.dto.model;

public class ExtendedIntitaUserDTO extends IntitaUserDTO {
    public Long getChatUserId() {
        return chatUserId;
    }

    public void setChatUserId(Long chatUserId) {
        this.chatUserId = chatUserId;
    }

    private Long chatUserId;

}
