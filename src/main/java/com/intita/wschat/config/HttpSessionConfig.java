package com.intita.wschat.config;

import java.util.Date;
import java.util.Random;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpSession;

import org.apache.catalina.util.SessionIdGeneratorBase;
import org.springframework.security.authentication.encoding.ShaPasswordEncoder;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

public class HttpSessionConfig extends SessionIdGeneratorBase 
{

	@Override
	public String generateSessionId(String route) {
		ServletRequestAttributes attr = (ServletRequestAttributes) RequestContextHolder.currentRequestAttributes();

		Cookie[] array = attr.getRequest().getCookies();
		HttpSession session = attr.getRequest().getSession();
		RequestContextHolder.currentRequestAttributes().getSessionId();				//session.getServletContext().getSessionCookieConfig().setName("CHAT_SESSION");
		session.setMaxInactiveInterval(3600*12);
		
		String value = null;
		String IntitaId = null;
		String ChatId = null;
		if(array != null)
			for(Cookie cook : array)
			{
				if(cook.getName().equals("JSESSIONID"))
					return cook.getValue();
			}
	
		return new ShaPasswordEncoder().encodePassword(((Integer)new Random(new Date().getTime()).nextInt()).toString(), 0);
	}
	
}