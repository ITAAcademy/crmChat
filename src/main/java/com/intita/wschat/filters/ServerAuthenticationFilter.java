package com.intita.wschat.filters;

import com.intita.wschat.config.ChatPrincipal;
import com.intita.wschat.config.CustomAuthenticationProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.GenericFilterBean;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

/**
 * 
 * @author Zinchuk Roman
 */
@Component
public class ServerAuthenticationFilter extends GenericFilterBean{

	@Autowired
	private CustomAuthenticationProvider authenticationProvider;

	public void destroy() {
		// Do nothing
	}
	/*
    public CustomFilter(String req) {
		authenticationProvider = new CustomAuthenticationProvider();
	}
	 */
	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws java.io.IOException,  ServletException
	{
		//authenticationProvider.autorization(authenticationProvider);
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		//System.out.println("CALL custom filter");
		if(auth == null)
			auth =  authenticationProvider.autorization(authenticationProvider);
		//SecurityContextHolder.getContext().setAuthentication(auth);
		//chatLangService.updateDataFromDatabase();
		//ChatPrincipal chatPrincipal = (ChatPrincipal)auth.getPrincipal();
		chain.doFilter(request, response);
	}
	public ServerAuthenticationFilter() {

	}

}