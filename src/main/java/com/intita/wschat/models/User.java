package com.intita.wschat.models;


import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Index;
import javax.persistence.ManyToMany;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import org.hibernate.validator.constraints.NotBlank;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;

import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * 
 * @author Nicolas Haiduchok
 */
@Entity
public class User implements UserDetails, Serializable,Comparable<User>{
	private static final long serialVersionUID = -532710433531902917L;
	public enum Permissions{PERMISSIONS_ADMIN,PERMISSIONS_USER};

	@Id
	@GeneratedValue
	private Long id;

	@NotBlank
	@Size(min = 1, max = 255)
	@Column(unique = false,name="email")
	private String login;

	//@NotNull
	@OneToOne(mappedBy = "intitaUser",fetch = FetchType.EAGER)
	private ChatUser chatUser;

	@NotBlank
	@Size(min = 1, max = 100)
	private String password;

	@Column(name="avatar")
	private String avatar;

	@Column(name="nickname")
	private String nickname;

	@Column(name="role")
	private int role;
	
	
	
	/*@OneToMany(mappedBy = "teacher_id", fetch = FetchType.LAZY)
	private List<IntitaConsultation> consultantedConsultation = new ArrayList<>();
	
	@OneToMany(mappedBy = "user_id", fetch = FetchType.LAZY)
	private List<IntitaConsultation> createdConsultation = new ArrayList<>();
	*/
	//private Permissions permission=Permissions.PERMISSIONS_USER;

	public Long getId() {
		return id;
	}
	public String getAvatar() {
		return avatar;
	}

	public void setAvatar(String avatar) {
		this.avatar = avatar;
	}

	public int getRole() {
		return role;
	}

	public void setRole(int role) {
		this.role = role;
	}
	public void setId(Long id) {
		this.id = id;
	}
	public String getLogin() {
		return login;
	}
	public void setLogin(String login) {
		this.login = login;
	}
	public String getEmail() {
		return login;
	}

	public void setEmail(String email) {
		this.login = email;
	}

	public Permissions getPermission() {
		return Permissions.PERMISSIONS_USER;
	}


	public void setPermission(Permissions permission) {
		//this.permission = permission;
		/*if (permission==Permissions.PERMISSIONS_ADMIN)isAdmin=true;
		else isAdmin=false;*/
	}
	@Override
	public Collection<? extends GrantedAuthority> getAuthorities() {
		//return AuthorityUtils.createAuthorityList("USER");
		/*switch(permission){
		case PERMISSIONS_ADMIN:
			return AuthorityUtils.createAuthorityList("ADMIN");
		case PERMISSIONS_USER:
			return AuthorityUtils.createAuthorityList("USER");
		default:
			return AuthorityUtils.createAuthorityList("HACKER");

		}*/
		return AuthorityUtils.createAuthorityList("ADMIN");
	}
	public static long getSerialversionuid() {
		return serialVersionUID;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	@Override
	public boolean isAccountNonExpired() {
		return true;
	}

	@Override
	public boolean isAccountNonLocked() {
		return true;
	}

	@Override
	public boolean isCredentialsNonExpired() {
		return true;
	}

	@Override
	public boolean isEnabled() {
		return true;
	}
	public static User getCurrentUser() {
		return (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
	}

	public static Long getCurrentUserId() {
		User u = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
		return u.getId();
	}

	public static boolean isAnonymous() {
		// Метод SecurityContextHolder.getContext().getAuthentication().isAuthenticated()
		// нічого не дасть, оскільки анонімний користувач теж вважається авторизованим
		return "anonymousUser".equals(SecurityContextHolder.getContext().getAuthentication().getName());
	}

	public User(String login, String email, String password) {
		this.login = login;
		//this.email = email;
		this.password = password;
	}
	public User(){
		super();
	}
	public void togglePermission(){
		/* if (permission==Permissions.PERMISSIONS_ADMIN)
			 permission=Permissions.PERMISSIONS_USER;
			 else permission=Permissions.PERMISSIONS_ADMIN;
			if (permission==Permissions.PERMISSIONS_ADMIN)isAdmin=true;
			 else isAdmin=false;*/
	}

	@Override
	public String getPassword() {
		return password;
	}

	@Override
	public String getUsername() {
		return getLogin();
	}

	public String getNickName() {
		if(nickname.isEmpty())
			return getLogin();

		return nickname;
	}

	public ChatUser getChatUser() {
		return chatUser;
	}

	public void setChatUser(ChatUser chatUser) {
		this.chatUser = chatUser;
	}

	@Override
	public int compareTo(User o) {
		if (o==null)return -1;
		return this.getId().compareTo(o.getId());
	}

}
