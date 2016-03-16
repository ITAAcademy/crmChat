package com.intita.wschat.config;
import java.io.IOException;
import java.net.InterfaceAddress;
import java.util.HashMap;
import java.util.List;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpRequest;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.intita.wschat.models.ChatUser;
import com.intita.wschat.services.ChatUsersService;
import com.intita.wschat.services.RedisService;
import com.intita.wschat.util.SerializedPhpParser;
import com.intita.wschat.util.SerializedPhpParser.PhpObject;
import com.intita.wschat.web.FileController;

import java.security.Principal;

@Component
public class CustomAuthenticationProvider implements AuthenticationProvider{
	@Autowired
	RedisService redisService; 
	@Autowired
	ChatUsersService chatUserServise;

	private	SerializedPhpParser serializedPhpParser;

	@Value("${redis.id}")
	private String redisId;
	private final static Logger log = LoggerFactory.getLogger(CustomAuthenticationProvider.class);

	@Override
	public Authentication authenticate(Authentication authentication) throws AuthenticationException {
		Authentication token = (Authentication) authentication;
		List<GrantedAuthority> authorities = "ADMIN".equals(token.getCredentials()) ? 
				AuthorityUtils.createAuthorityList("ROLE_ADMIN") : null;
				ServletRequestAttributes attr = (ServletRequestAttributes) RequestContextHolder.currentRequestAttributes();

				Cookie[] array = attr.getRequest().getCookies();
				HttpSession session = attr.getRequest().getSession();
				RequestContextHolder.currentRequestAttributes().getSessionId();				//session.getServletContext().getSessionCookieConfig().setName("CHAT_SESSION");
				session.setMaxInactiveInterval(3600*12);
				
				String value = null;
				String IntitaId = null;
				String IntitaLg = "en";
				String ChatId = null;
				if(array != null)
					for(Cookie cook : array)
					{
						if(cook.getName().equals("JSESSIONID"))
						{
							System.out.println("URAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA");
							System.out.println(cook.getValue());
							value = cook.getValue();
							session.setAttribute("id", value);
							String phpSession = redisService.getKeyValue(value);



							if(phpSession != null)
							{
								try {
									System.out.println(phpSession);
									serializedPhpParser = new SerializedPhpParser(phpSession);
									IntitaId = (String)serializedPhpParser.findPatern(redisId);
									IntitaLg = (String)serializedPhpParser.find("lg");
								} catch (Exception e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
								}
							}
							break;
						}
					}
				Long intitaIdLong = null;
				try{
						intitaIdLong = Long.parseLong(IntitaId);
				}
				catch(NumberFormatException e){
				log.info(e.getMessage());
				}
				if(intitaIdLong != null)
					ChatId = chatUserServise.getChatUserFromIntitaId(intitaIdLong , true).getId().toString();
				else
				{
					Object obj_s = session.getAttribute("chatId");
					if(obj_s == null)
					{
						System.out.println("CREATE NEW SESSION");
						ChatUser c_u_temp = chatUserServise.getChatUserFromIntitaId((long) -1, true);
						ChatId = c_u_temp.getId().toString();
						session.setAttribute("chatId", ChatId);
					}
					else
					{
						System.out.println("SESSION OK " + (String)obj_s);
						ChatId = (String) obj_s;
					}

				}
				session.setAttribute("chatLg", IntitaLg);
				Authentication auth = new UsernamePasswordAuthenticationToken(ChatId, token.getCredentials(), authorities);
				//	auth.setAuthenticated(true);
				return auth;
	}

	@Override
	public boolean supports(Class<?> authentication) {
		return UsernamePasswordAuthenticationToken.class.isAssignableFrom(authentication);
	}

	public boolean autorization()
	{
		return autorization(this);
	}

	public static boolean autorization(AuthenticationProvider authenticationProvider)
	{
		if(SecurityContextHolder.getContext().getAuthentication() != null && authenticationProvider != null)
		{
			System.out.println(SecurityContextHolder.getContext().getAuthentication().isAuthenticated());
			//if(!SecurityContextHolder.getContext().getAuthentication().isAuthenticated())
			SecurityContextHolder.getContext().setAuthentication(authenticationProvider.authenticate(SecurityContextHolder.getContext().getAuthentication()));
			return true;
		}
		else
			return false;

	}

}