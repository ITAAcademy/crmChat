package com.intita.wschat.config;
import java.io.IOException;
import java.net.InterfaceAddress;
import java.util.HashMap;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.intita.wschat.services.ChatUsersService;
import com.intita.wschat.services.RedisService;
import java.security.Principal;

@Component
public class CustomAuthenticationProvider implements AuthenticationProvider{
	@Autowired
	RedisService redisService; 
	@Autowired
	ChatUsersService chatUserServise;

	@Override
	public Authentication authenticate(Authentication authentication) throws AuthenticationException {
		UsernamePasswordAuthenticationToken token = (UsernamePasswordAuthenticationToken) authentication;

		List<GrantedAuthority> authorities = "ADMIN".equals(token.getCredentials()) ? 
				AuthorityUtils.createAuthorityList("ROLE_ADMIN") : null;
				//System.out.println(		RequestContextHolder.currentRequestAttributes().getSessionId());
				String json = redisService.getKeyValue(token.getName());
				JsonFactory factory = new JsonFactory(); 
				ObjectMapper mapper = new ObjectMapper(factory); 
				TypeReference<HashMap<String,Object>>typeRef  = new TypeReference<HashMap<String,Object>>() {};

				HashMap<String, Object> o = null;
				try {
					o = mapper.readValue(json, typeRef);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} 
//				System.out.println("Got " + o);
				String IntitaId = (String) o.get("5189ad00951768aff20edc3fef6e92dd__id");
				String ChatId = chatUserServise.getChatUserFromIntitaId(Long.parseLong(IntitaId)).getId().toString();

				return new UsernamePasswordAuthenticationToken(ChatId, token.getCredentials(), authorities);
	}

	@Override
	public boolean supports(Class<?> authentication) {
		return UsernamePasswordAuthenticationToken.class.isAssignableFrom(authentication);
	}

}