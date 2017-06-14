package com.intita.wschat.config;

import com.intita.wschat.annotations.ServerAccess;
import com.intita.wschat.filters.ServerAuthenticationFilter;
import com.intita.wschat.util.ReflectionChatTools;
import org.springframework.aop.support.AopUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.context.embedded.FilterRegistrationBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.authentication.SavedRequestAwareAuthenticationSuccessHandler;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.authentication.logout.LogoutFilter;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;
import org.springframework.security.web.context.SecurityContextPersistenceFilter;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * 
 * @author Nicolas Haiduchok
 */
@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true)
@EnableAutoConfiguration
public class SecurityConfig extends WebSecurityConfigurerAdapter {

	@Autowired
	private UserDetailsService userDetailsService;
	@Autowired
	private CustomAuthenticationProvider authenticationProvider;
	@Autowired
	private ServerAuthenticationFilter authenticationTokenFilter;
	@Autowired
	private ApplicationContext appContext;
	
	/*@Autowired
	private RequestContextFilter requestContextFilter;*/

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

	@Bean
	public AuthenticationSuccessHandler successHandler() {
	    SimpleUrlAuthenticationSuccessHandler handler = new SimpleUrlAuthenticationSuccessHandler();
	    handler.setUseReferer(true);
	    return handler;
	}
	@Autowired
	AuditEventConfiguration sdfsd;
	
	@Override
	protected void configure(HttpSecurity http) throws Exception {
		
		http
		.csrf().disable()
		//.addFilterAfter(authenticationTokenFilter, BasicAuthenticationFilter.class)
		.addFilterBefore( authenticationTokenFilter, UsernamePasswordAuthenticationFilter.class)

		.formLogin()
		.loginPage("/")
		.successHandler(successHandler())
		.passwordParameter("password")
		//.defaultSuccessUrl("/chatFrame.html")
		.permitAll()
		.and().exceptionHandling().accessDeniedPage("/403")
		.and()
		.logout()
		//.logoutSuccessUrl("/index.html")
		.permitAll()
		.and()
		.authorizeRequests()
		.antMatchers("/static_templates/**", "/js/**", "/lib/**", "/images/**", "/fonts/**", "/data/**", "/css/**","/chatFrame.html", "/index.html", "/","/getusersemails","/ws/**","/wss/**").permitAll()
		.antMatchers("/websocket").hasRole("ADMIN")
		.anyRequest().authenticated();

		/*
		 * ATENTION 
		 * 
		 */
		http.exceptionHandling().authenticationEntryPoint(sdfsd);
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

	@Bean
	  public FilterRegistrationBean registration(ServerAuthenticationFilter filter) {
	    FilterRegistrationBean registration = new FilterRegistrationBean(filter);
	    ArrayList<String> urlPatterns = new ArrayList(getUrlPatternsForServerAccess());
	    registration.setUrlPatterns(urlPatterns);
	    registration.setEnabled(true);
	    registration.setOrder(3);
	    return registration;
	  }


	public List<String> getUrlPatternsForServerAccess(){
		List<String> urlParameters = new ArrayList<String>();

			List<Method> methods = ReflectionChatTools.getMethodsFromPackage("com.intita.wschat.web",Controller.class);

			for(Method m: methods){
				if(m.isAnnotationPresent(ServerAccess.class)){
					RequestMapping requestMappingAnnotation = m.getAnnotation(RequestMapping.class);
					if (requestMappingAnnotation==null) continue;
					String MappingPath = requestMappingAnnotation.value()[0];
					urlParameters.add("/" + MappingPath + "/*");
				}
			}
		return urlParameters;
	}
	/*
    @Override
    public void configure(AuthenticationManagerBuilder auth) throws Exception {
        auth.userDetailsService(userDetailsService).passwordEncoder(new ShaPasswordEncoder());
          //  .passwordEncoder(new BCryptPasswordEncoder()); 
    }
	 */
}


