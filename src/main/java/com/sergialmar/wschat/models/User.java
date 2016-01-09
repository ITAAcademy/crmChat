package com.sergialmar.wschat.models;


import java.util.Collection;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Index;
import javax.validation.constraints.Size;

import org.hibernate.validator.constraints.NotBlank;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;


@Entity
@Table(indexes = {
  @Index(columnList="login", unique = true), 
  @Index(columnList="email", unique = true)
})
public class User implements UserDetails{
	private static final long serialVersionUID = -532710433531902917L;
	public enum Permissions{PERMISSIONS_ADMIN,PERMISSIONS_USER};
	
	@Id
	@GeneratedValue
	private Long id;
	
	@NotBlank
	@Size(min = 1, max = 512)
	@Column(unique = true)
	private String login;
	
	private boolean isAdmin = false;
	
	public boolean isAdmin() {
		return isAdmin;
	}

	@NotBlank
	  @Size(min = 1, max = 512)
	  @Column(unique = true)
	  private String email;

	@NotBlank
    @Size(min = 1, max = 100)
	private String password;
	  
	  private Permissions permission=Permissions.PERMISSIONS_USER;
	  
	public Long getId() {
		return id;
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
		return email;
	}
	
	public void setEmail(String email) {
		this.email = email;
	}

		public Permissions getPermission() {
		return permission;
	}


	public void setPermission(Permissions permission) {
		this.permission = permission;
		if (permission==Permissions.PERMISSIONS_ADMIN)isAdmin=true;
		else isAdmin=false;
	}
	@Override
	public Collection<? extends GrantedAuthority> getAuthorities() {
		//return AuthorityUtils.createAuthorityList("USER");
		switch(permission){
		case PERMISSIONS_ADMIN:
			return AuthorityUtils.createAuthorityList("ADMIN");
		case PERMISSIONS_USER:
			return AuthorityUtils.createAuthorityList("USER");
		default:
			return AuthorityUtils.createAuthorityList("HACKER");
			
		}
		
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
			this.email = email;
			this.password = password;
		}
		 public User(){
			  super();
		  }
		 public void togglePermission(){
			 if (permission==Permissions.PERMISSIONS_ADMIN)
			 permission=Permissions.PERMISSIONS_USER;
			 else permission=Permissions.PERMISSIONS_ADMIN;
			 if (permission==Permissions.PERMISSIONS_ADMIN)isAdmin=true;
			 else isAdmin=false;
		 }

		@Override
		public String getPassword() {
			return password;
		}

		@Override
		public String getUsername() {
			return getLogin();
		}
	  
}
