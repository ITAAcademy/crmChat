package com.wschat;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.actuate.autoconfigure.WebSocketTraceChannelInterceptorAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.context.web.SpringBootServletInitializer;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Import;

import com.wschat.config.RedisDBConfig;

@SpringBootApplication
@Import(WebSocketTraceChannelInterceptorAutoConfiguration.class)
public class Application extends SpringBootServletInitializer  {

	@Override
    protected SpringApplicationBuilder configure(SpringApplicationBuilder application) {
        return application.sources(Application.class);
    }
	
	public static void main(String[] args) {
		SpringApplication.run(Application.class, args);
		
	}
}
