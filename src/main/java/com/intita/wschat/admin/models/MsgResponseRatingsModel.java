package com.intita.wschat.admin.models;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.hibernate.Hibernate;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.intita.wschat.event.LoginEvent;
import com.intita.wschat.models.ChatConsultationResult;
import com.intita.wschat.models.ChatConsultationResultValue;


@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public  class MsgResponseRatingsModel implements Serializable
{
	/**
	 * 
	 */
	private static final long serialVersionUID = -957287346006243536L;

	@JsonProperty("id")
	Long id;

	@JsonProperty("values")
	@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
	private Map<Long, Integer> values;// key - id of question

	@JsonProperty("user")
	private LoginEvent userInfo;

	@JsonProperty("date")
	private Date date;

	public MsgResponseRatingsModel() {
		// TODO Auto-generated constructor stub
	}

	public MsgResponseRatingsModel(ChatConsultationResult result){
		this.id = result.getId();
		this.date = result.getDate();
		this.userInfo = new LoginEvent(result.getChatUser());
		this.values = new HashMap();
		for(ChatConsultationResultValue value : result.getValues()){
			this.values.put(value.getRating().getId(), value.getValue());
		}
	}

	public static ArrayList<MsgResponseRatingsModel> convertAllChatConsultationResults(List<ChatConsultationResult> chatConsultationResults){
		ArrayList<MsgResponseRatingsModel> res = new ArrayList<>();
		
		for(ChatConsultationResult chatConsultationResult : chatConsultationResults)
			res.add(new MsgResponseRatingsModel(chatConsultationResult));
		
		return res;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Map<Long, Integer> getValues() {
		return values;
	}

	public void setValues(Map<Long, Integer> values) {
		this.values = values;
	}

	public LoginEvent getUserInfo() {
		return userInfo;
	}

	public void setUserInfo(LoginEvent userInfo) {
		this.userInfo = userInfo;
	}

	public Date getDate() {
		return date;
	}

	public void setDate(Date date) {
		this.date = date;
	}


}