package com.intita.wschat.models;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToMany;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.validation.constraints.Size;

import org.hibernate.validator.constraints.NotBlank;

/**
 * 
 * @author Samoenko Yuriy
 */
@Entity(name="chat_tenant")
public class ChatTenant implements Serializable {

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

}

