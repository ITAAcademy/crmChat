package com.intita.wschat.config;

import com.intita.wschat.models.ChatUser;
import com.intita.wschat.models.User;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import javax.security.auth.Subject;
import java.security.Principal;
import java.util.Collection;

/**
 * Created by roma on 01.06.17.
 */
public class ChatPrincipal implements Principal {
	public ChatPrincipal(){

	}

	@Override
	public int hashCode() {
		int result = intitaUser.hashCode();
		result = 31 * result + chatUser.hashCode();
		return result;
	}

	public ChatPrincipal(ChatUser chatUser, User intitaUser){
		this.chatUser = chatUser;
		this.intitaUser = intitaUser;
	}
	public User getIntitaUser() {
		return intitaUser;
	}

	public void setIntitaUser(User intitaUser) {
		this.intitaUser = intitaUser;
	}

	public ChatUser getChatUser() {
		return chatUser;
	}

	public void setChatUser(ChatUser chatUser) {
		this.chatUser = chatUser;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		ChatPrincipal that = (ChatPrincipal) o;

		if (intitaUser != null ? !intitaUser.equals(that.intitaUser) : that.intitaUser != null) return false;
		return chatUser.equals(that.chatUser);
	}

	private User intitaUser;
	private ChatUser chatUser;



	@Override
	public String toString(){
		return chatUser==null ? "" : ""+chatUser.getId();
	}

	@Override
	public String getName() {
		return chatUser==null ? "" : ""+chatUser.getId();
	}
}
