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

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.fasterxml.jackson.annotation.JsonView;

import jsonview.Views;

/**
 * 
 * @author Nicolas Haiduchok
 */
@Entity
public class  User implements UserDetails, Serializable,Comparable<User>{
	private static final long serialVersionUID = -532710433531902917L;
	public enum Permissions{PERMISSIONS_ADMIN,PERMISSIONS_USER};

	@Id
	@GeneratedValue
	@JsonView(Views.Public.class)
	private Long id;

	@NotBlank
	@Size(min = 1, max = 255)
	@Column(unique = false,name="email")
	@JsonView(Views.Public.class)
	private String login;

	//@NotNull
	@OneToOne(mappedBy = "intitaUser",fetch = FetchType.LAZY)
	@JsonBackReference
	private ChatUser chatUser;

	@NotBlank
	@Size(min = 1, max = 100)
	@JsonView(Views.Public.class)
	private String password;

	@Column(name="avatar")
	private String avatar;

	@Column(name="nickname")
	private String nickName;

	@Column(name="firstname")
	private String firstName;
	
	@Column(name="secondname")
	private String secondName;


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
		// Method SecurityContextHolder.getContext().getAuthentication().isAuthenticated()
		// do nothing because anonimus user is considered authorized too
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
		if(nickName != null && !nickName.isEmpty())
			return nickName;
		
		if(firstName != null && !firstName.isEmpty() && secondName != null && !secondName.isEmpty())
			return firstName + " " + secondName;

		return getLogin();
	}
	
	public void setNickname(String nickname) {
		this.nickName = nickname;
	}
	public String getFirstName() {
		return firstName;
	}
	public String getFullName(){
		return firstName + secondName;
	}
	public void setFirstName(String firstName) {
		this.firstName = firstName;
	}
	public String getSecondName() {
		return secondName;
	}
	public void setSecondName(String secondName) {
		this.secondName = secondName;
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
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((id == null) ? 0 : id.hashCode());
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
		User other = (User) obj;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		return true;
	}

}
