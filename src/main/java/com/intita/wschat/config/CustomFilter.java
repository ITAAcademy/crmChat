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


public class CustomFilter implements Filter{

    @Autowired
    private CustomAuthenticationProvider authenticationProvider;
    
    @Bean
	public void destroy() {
		// Do nothing
	}

    public CustomFilter(String req) {
		
	}
	@Override
	public void doFilter(ServletRequest request, ServletResponse response,
			FilterChain chain) throws IOException, ServletException {

		// principal is set in here as a header or parameter. you need to find out 
		// what it's named to extract it
		HttpServletRequest req = (HttpServletRequest) request;
		System.out.println("Okkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkk");
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