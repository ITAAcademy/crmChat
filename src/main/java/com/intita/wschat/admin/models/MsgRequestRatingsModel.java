package com.intita.wschat.admin.models;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonProperty;

public  class MsgRequestRatingsModel implements Serializable
{
	private final static long serialVersionUID = -4461936499118564786L;

	@JsonProperty("room_user_ids_list")
	private Long[] roomUserIds;
	
	@JsonProperty("is_user")
	private Boolean isUser;
	
	@JsonProperty("before_date")
	private Long beforeDate;
	
	@JsonProperty("after_date")
	private Long afterDate;

	
	public Long[] getRoomUserIds() {
		return roomUserIds;
	}
	public void setRoomUserIds(Long[] roomUserIds) {
		this.roomUserIds = roomUserIds;
	}
	public Boolean getIsUser() {
		return isUser;
	}
	public void setIsUser(Boolean isUser) {
		this.isUser = isUser;
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


}