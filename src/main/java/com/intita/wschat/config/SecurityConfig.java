package com.intita.wschat.config;

import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.authentication.encoding.ShaPasswordEncoder;

import java.util.List;
import java.util.Random;

import org.apache.tomcat.jdbc.pool.DataSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.security.web.authentication.AbstractAuthenticationProcessingFilter;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;
import org.springframework.security.web.header.writers.frameoptions.XFrameOptionsHeaderWriter;

@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true)
public class SecurityConfig extends WebSecurityConfigurerAdapter {

	@Autowired
	private UserDetailsService userDetailsService;
	@Autowired
	private CustomAuthenticationProvider authenticationProvider;

	private CustomFilter authenticationTokenFilter = new CustomFilter("");

	/*  @Autowired
	DataSource dataSource;

    @Autowired
	public void configAuthentication(AuthenticationManagerBuilder auth) throws Exception {

	  auth.jdbcAuthentication().dataSource(dataSource)
		.usersByUsernameQuery(
			"select email,password, enabled from user where email=?")
		.authoritiesByUsernameQuery(
			"select email, role from user_roles where username=?");
	}	*/

	@Override
	protected void configure(HttpSecurity http) throws Exception {
		http
		.csrf().disable()
		.addFilterBefore(authenticationTokenFilter, BasicAuthenticationFilter.class)
		.formLogin()
		.loginPage("/index.html")
		.passwordParameter("password")
		//.defaultSuccessUrl("/chatFrame.html")
		.permitAll()
		.and()
		.logout()
		.logoutSuccessUrl("/index.html")
		.permitAll()
		.and()
		.authorizeRequests()
		.antMatchers("/js/**", "/lib/**", "/images/**", "/css/**","/chatFrame.html", "/index.html", "/","/getusersemails").permitAll()
		.antMatchers("/websocket").hasRole("ADMIN")
		.anyRequest().authenticated();
		
		/*
		 * ATENTION 
		 * 
		 */
		 http.headers()
		.frameOptions().sameOrigin()
		.httpStrictTransportSecurity().disable();
		 http.headers()
		 .defaultsDisabled()
		 .cacheControl();
		

		 //http.headers().addHeaderWriter(new XFrameOptionsHeaderWriter(XFrameOptionsHeaderWriter.XFrameOptionsMode.SAMEORIGIN));
		


	}
	private static final String SECURE_ADMIN_PASSWORD = "rockandroll";
	@Autowired
	public void configureGlobal(AuthenticationManagerBuilder auth) throws Exception {

		 auth.authenticationProvider(authenticationProvider);
	}
	/*
    @Override
    public void configure(AuthenticationManagerBuilder auth) throws Exception {
        auth.userDetailsService(userDetailsService).passwordEncoder(new ShaPasswordEncoder());
          //  .passwordEncoder(new BCryptPasswordEncoder()); 
    }
	 */
}


