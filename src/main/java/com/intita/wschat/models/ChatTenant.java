package com.intita.wschat.models;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToMany;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import org.hibernate.validator.constraints.NotBlank;
import org.springframework.util.ObjectUtils;

/**
 * 
 * @author Samoenko Yuriy
 */
@Entity(name="user_tenant")
public class ChatTenant implements Serializable,Comparable<ChatTenant> {

	/**
	 * 
	 */
	private static final long serialVersionUID = 894035383584426711L;

	@Id
	@NotBlank
	@GeneratedValue
	private Long id;

	@OneToOne(fetch = FetchType.EAGER)
	private ChatUser chatUser; 
	
	@NotNull
	@Column(name="start_date")
	private Date startDate;
	
	@Column(name="end_date")
	private Date endDate;

	
	public Date getStartDate() {
		return startDate;
	}
	public void setStartDate(Date startDate) {
		this.startDate = startDate;
	}
	public Date getEndDate() {
		return endDate;
	}
	public void setEndDate(Date endDate) {
		this.endDate = endDate;
	}
	public ChatTenant(){

	}
	public ChatTenant(Long id, ChatUser user_id){
		this.id=id;
		this.chatUser=user_id;
	}
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	public ChatUser getChatUser() {
		return chatUser;
	}
	public void setChatUser(ChatUser chatUser) {
		this.chatUser = chatUser;
	}
	@Override
	public int compareTo(ChatTenant o) {
		if (o==null)return -1;
		return this.getId().compareTo(o.getId());
	}
	
	@ManyToMany(fetch = FetchType.LAZY)
	 private Set<BotCategory> botCategories = new HashSet<>();

	 
	public Set<BotCategory> getBotCategories() {
		return botCategories;
	}
	public void setBotCategories(Set<BotCategory> botCategories) {
		this.botCategories = botCategories;
	};


}

