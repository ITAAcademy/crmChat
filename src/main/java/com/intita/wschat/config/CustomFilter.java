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
import org.springframework.web.filter.RequestContextFilter;
/**
 * 
 * @author Nicolas Haiduchok
 */
@Component
public class CustomFilter extends RequestContextFilter{

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

}