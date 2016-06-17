package com.intita.wschat.config;


import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.DefaultRedirectStrategy;
import org.springframework.security.web.RedirectStrategy;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

@Configuration
@EnableWebMvc
@ComponentScan
@Component(value = "customAuthenticationEntryPoint")
public class AuditEventConfiguration implements AuthenticationEntryPoint {
 
	private String loginPageUrl = "/";
 
	private boolean returnParameterEnabled = true;
 
	private String returnParameterName = "before";
 
	@Override
	public void commence(HttpServletRequest request, HttpServletResponse response,
			AuthenticationException reason) throws IOException, ServletException {
 
		if(null == loginPageUrl || "".equals(loginPageUrl))
			throw new RuntimeException("loginPageUrl has not been defined");
 
		String redirectUrl = loginPageUrl;
 
		if(isReturnParameterEnabled()) {
			String redirectUrlReturnParameterName = getReturnParameterName();
 
			if(null == redirectUrlReturnParameterName || "".equals(redirectUrlReturnParameterName))
				throw new RuntimeException("redirectUrlReturnParameterName has not been defined");
 
			redirectUrl += "?" + redirectUrlReturnParameterName + "=" + request.getServletPath();
		}
 
		RedirectStrategy redirectStrategy = new DefaultRedirectStrategy();
		redirectStrategy.sendRedirect(request, response, redirectUrl);
 
		return;
	}
 
	public String getLoginPageUrl() {
		return loginPageUrl;
	}
 
	public void setLoginPageUrl(String loginPageUrl) {
		this.loginPageUrl = loginPageUrl;
	}
 
	public boolean isReturnParameterEnabled() {
		return returnParameterEnabled;
	}
 
	public void setReturnParameterEnabled(boolean returnParameterEnabled) {
		this.returnParameterEnabled = returnParameterEnabled;
	}
 
	public String getReturnParameterName() {
		return returnParameterName;
	}
 
	public void setReturnParameterName(String returnParameterName) {
		this.returnParameterName = returnParameterName;
	}
 
}