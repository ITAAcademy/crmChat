package com.intita.wschat.admin.models;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonProperty;

public  class MsgRequestModel implements Serializable
{

@JsonProperty("user_id_first")
private Long userIdFirst;
@JsonProperty("user_id_second")
private Long userIdSecond;
@JsonProperty("before_date")
private Long beforeDate;
@JsonProperty("after_date")
private Long afterDate;
private final static long serialVersionUID = -4461936499118564786L;

@JsonProperty("user_id_first")
public Long getUserIdFirst() {
return userIdFirst;
}

@JsonProperty("user_id_first")
public void setUserIdFirst(Long userIdFirst) {
this.userIdFirst = userIdFirst;
}


@JsonProperty("user_id_second")
public Long getUserIdSecond() {
return userIdSecond;
}

@JsonProperty("user_id_second")
public void setUserIdSecond(Long userIdSecond) {
this.userIdSecond = userIdSecond;
}

@JsonProperty("before_date")
public Long getBeforeDate() {
return beforeDate;
}

@JsonProperty("before_date")
public void setBeforeDate(Long beforeDate) {
this.beforeDate = beforeDate;
}

@JsonProperty("after_date")
public Long getAfterDate() {
return afterDate;
}

@JsonProperty("after_date")
public void setAfterDate(Long afterDate) {
this.afterDate = afterDate;
}
}