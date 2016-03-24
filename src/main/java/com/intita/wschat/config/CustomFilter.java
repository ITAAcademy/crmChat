package com.intita.wschat.config;

import java.io.IOException;
import java.util.Set;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
/**
 * 
 * @author Nicolas Haiduchok
 */
@Component
public class CustomFilter implements Filter{

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
    public CustomFilter() {
		
	}
	@Override
	public void doFilter(ServletRequest request, ServletResponse response,
			FilterChain chain) throws IOException, ServletException {
/*
		// principal is set in here as a header or parameter. you need to find out 
		// what it's named to extract it
		HttpServletRequest req = (HttpServletRequest) request;
		System.out.println(req.getSession().getId());
		if(SecurityContextHolder.getContext().getAuthentication() != null && authenticationProvider != null)
		{
			System.out.println(SecurityContextHolder.getContext().getAuthentication().isAuthenticated());
			//if(!SecurityContextHolder.getContext().getAuthentication().isAuthenticated())
				SecurityContextHolder.getContext().setAuthentication(authenticationProvider.authenticate(SecurityContextHolder.getContext().getAuthentication()));
		}
			*/
			
		
		/*if (SecurityContextHolder.getContext().getAuthentication() == null) {
			// in here, get your principal, and populate the auth object with 
			// the right authorities
			Authentication auth = new CustomAuthentication();
			auth.setAuthenticated(true);
			
			SecurityContextHolder.getContext().setAuthentication(auth);*
		}*/
		
		chain.doFilter(request, response);
	}

	@Override
	public void init(FilterConfig arg0) throws ServletException {
		// Do nothing
	}

}