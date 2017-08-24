package com.intita.wschat.config;

import java.io.Serializable;
import java.security.Permission;
import java.util.Collection;

import com.intita.wschat.domain.UserRole;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.access.PermissionEvaluator;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;

import com.intita.wschat.models.User;
import com.intita.wschat.services.UsersService;
/**
 * 
 * @author Nicolas Haiduchok
 */
@Configuration
public class UserPermissionEvaluator implements PermissionEvaluator {

	@Autowired UsersService userService;
	@Override
	public boolean hasPermission(Authentication authentication, Object targetDomainObject, Object permissions) {
		String []permitionsOrList = ((String) permissions).split(",");
		boolean has = false;
		for(String permitionsOr : permitionsOrList)
		{
			String [] permitionsAnd = permitionsOr.split("&");
			boolean _has = true;
			for(String permition : permitionsAnd)
			{
				_has = _has && userService.checkRoleByAuthentication(authentication,  UserRole.valueOf(permition.trim()));
			}
			has = has || _has;
			if(has)
				break;
		}		
 		//return userService.checkRoleByPrincipal(authentication,  UserRole.valueOf());
		return has;
	}

	@Override
	public boolean hasPermission(Authentication authentication, Serializable targetId, String targetType,
			Object permission) {

		return false;
	}

}