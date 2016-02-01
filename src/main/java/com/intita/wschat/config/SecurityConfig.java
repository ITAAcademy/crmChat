package com.intita.wschat.config;

import javax.servlet.Filter;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true)
public class SecurityConfig extends WebSecurityConfigurerAdapter {

	@Autowired
	private UserDetailsService userDetailsService;
	@Autowired
	private CustomAuthenticationProvider authenticationProvider;
	@Autowired
	private CustomFilter authenticationTokenFilter;

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
		//.addFilterAfter(authenticationTokenFilter, BasicAuthenticationFilter.class)
		.formLogin()
		.loginPage("/index.html")
		.passwordParameter("password")
		//.defaultSuccessUrl("/chatFrame.html")
		.permitAll()
		.and()
		.logout()
		//.logoutSuccessUrl("/index.html")
		.permitAll()
		.and()
		.authorizeRequests()
		.antMatchers("/js/**", "/lib/**", "/images/**", "/css/**","/chatFrame.html", "/index.html", "/","/getusersemails","/ws/**","/chatrooms_view/**","/dialog_view/**").permitAll()
		.antMatchers("/websocket").hasRole("ADMIN")
		.anyRequest().permitAll();
		
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


