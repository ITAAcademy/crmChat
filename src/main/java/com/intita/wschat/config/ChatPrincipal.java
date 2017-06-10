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

	private User intitaUser;
	private ChatUser chatUser;
	

	public ChatPrincipal(){

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
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((chatUser == null) ? 0 : chatUser.hashCode());
		result = prime * result + ((intitaUser == null) ? 0 : intitaUser.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		ChatPrincipal other = (ChatPrincipal) obj;
		if (chatUser == null) {
			if (other.chatUser != null)
				return false;
		} else if (!chatUser.equals(other.chatUser))
			return false;
		if (intitaUser == null) {
			if (other.intitaUser != null)
				return false;
		} else if (!intitaUser.equals(other.intitaUser))
			return false;
		return true;
	}

	@Override
	public String toString(){
		return chatUser==null ? "" : ""+chatUser.getId();
	}

	@Override
	public String getName() {
		return chatUser==null ? "" : ""+chatUser.getId();
	}
}
