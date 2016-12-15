package com.intita.wschat.config;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.GenericFilterBean;
import org.springframework.web.filter.RequestContextFilter;
/**
 * 
 * @author Nicolas Haiduchok
 */
@Component
public class CustomFilter extends GenericFilterBean{

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
		chain.doFilter(request, response);
	}
	public CustomFilter() {

	}

}