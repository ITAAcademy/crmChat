package com.intita.wschat.config;
import java.io.IOException;
import java.net.InterfaceAddress;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import com.intita.wschat.dto.mapper.DTOMapper;
import com.intita.wschat.dto.model.ChatUserDTO;
import com.intita.wschat.models.User;
import com.intita.wschat.services.UsersService;

import org.hibernate.Hibernate;
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
import com.intita.wschat.models.Room;
import com.intita.wschat.services.ChatUsersService;
import com.intita.wschat.services.RedisService;
import com.intita.wschat.util.SerializedPhpParser;
import com.intita.wschat.util.SerializedPhpParser.PhpObject;
import com.intita.wschat.web.FileController;

import java.security.Principal;
/**
 * 
 * @author Nicolas Haiduchok
 */
@Component
public class CustomAuthenticationProvider implements AuthenticationProvider{
	@Autowired
	RedisService redisService; 
	@Autowired
	ChatUsersService chatUserServise;
	@Autowired
	UsersService intitaUsersService;
	@Autowired
	DTOMapper dtoMapper;



	@Value("${redis.id}")
	private String redisId;
	private final static Logger log = LoggerFactory.getLogger(CustomAuthenticationProvider.class);

	@Override
	public Authentication authenticate(Authentication authentication) throws AuthenticationException {
		Authentication auth;
		SerializedPhpParser serializedPhpParser;

		Authentication token = (Authentication) authentication;
		List<GrantedAuthority> authorities = "ADMIN".equals(token.getCredentials()) ?
				AuthorityUtils.createAuthorityList("ROLE_ADMIN") : null;
				ServletRequestAttributes attr = (ServletRequestAttributes) RequestContextHolder.currentRequestAttributes();

				Cookie[] array = attr.getRequest().getCookies();
				HttpSession session = attr.getRequest().getSession();
				RequestContextHolder.currentRequestAttributes().getSessionId();                //session.getServletContext().getSessionCookieConfig().setName("CHAT_SESSION");
				session.setMaxInactiveInterval(3600 * 12);

				String value = null;
				String IntitaId = null;
				String IntitaLg = "ua";
				ChatUser chatUser = null;
				if (array != null)
					for (Cookie cook : array) {
						if (cook.getName().equals("JSESSIONID")) {
							value = cook.getValue();
							log.info("redis search cook:"+value);
							session.setAttribute("id", value);
							String phpSession = redisService.getKeyValue(value);
							System.out.println("cook value: " + phpSession);

							if (phpSession != null) {
								try {
									System.out.println(phpSession);
									serializedPhpParser = new SerializedPhpParser(phpSession);
									IntitaId = (String) serializedPhpParser.findPatern(redisId);
									IntitaLg = (String) serializedPhpParser.find("lg");
									System.out.println("cook intitaID: " + IntitaId);
								} catch (Exception e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
								}
							}
							break;
						}
					}
				Long intitaIdLong = null;
				try {
					intitaIdLong = Long.parseLong(IntitaId);
				} catch (NumberFormatException e) {
					log.info(e.getMessage());
				}


				Object obj_s = session.getAttribute("chatId");
				Long intitaIdSession = (Long) session.getAttribute("intitaId");
				if (obj_s == null || (intitaIdLong!=null && !intitaIdLong.equals(intitaIdSession))) {
					System.out.println("CREATE NEW SESSION");
					if (intitaIdLong != null) {
						chatUser = chatUserServise.getChatUserFromIntitaId(intitaIdLong, false);
						session.setAttribute("intitaId", intitaIdLong);
					} else {
						System.out.println("CREATE GUEST");
						ChatUser c_u_temp = chatUserServise.getChatUserFromIntitaId((long) -1, true);
						chatUser = c_u_temp;
						session.removeAttribute("intitaId");
					}
					session.setAttribute("chatId", chatUser.getId());
				} else {
					System.out.println("SESSION OK " + obj_s);
					String chatIdStr = obj_s.toString();
					if(chatIdStr!=null)
					{
						Long chatId = Long.parseLong(chatIdStr);
						chatUser = chatUserServise.getChatUserWithoutLazy(chatId);
					}
				}
				User intitaUser = intitaUsersService.getUserFromChat(chatUser.getId());

		
				
				if(intitaUser != null)
					Hibernate.initialize(intitaUser);
				
				ChatPrincipal principal = new ChatPrincipal(chatUser, intitaUser);

				auth = new UsernamePasswordAuthenticationToken(principal, token.getCredentials(), authorities);
				//	auth.setAuthenticated(true);
				return auth;
	}

	@Override
	public boolean supports(Class<?> authentication) {
		return UsernamePasswordAuthenticationToken.class.isAssignableFrom(authentication);
	}

	public Authentication autorization()
	{
		return autorization(this);
	}

	public Authentication autorization(AuthenticationProvider authenticationProvider)
	{
		if(SecurityContextHolder.getContext().getAuthentication() != null && authenticationProvider != null)
		{
			Authentication auth = authenticationProvider.authenticate(SecurityContextHolder.getContext().getAuthentication());
			SecurityContextHolder.getContext().setAuthentication(auth);
			return auth;
		}
		else
			return null;

	}

}