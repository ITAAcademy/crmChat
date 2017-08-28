package com.intita.wschat.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.web.ResourceProperties;
import org.springframework.boot.autoconfigure.web.WebMvcAutoConfiguration.EnableWebMvcConfiguration;
import org.springframework.boot.autoconfigure.web.WebMvcProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.http.CacheControl;
import org.springframework.web.servlet.config.annotation.*;
import org.springframework.web.servlet.mvc.WebContentInterceptor;

import java.util.concurrent.TimeUnit;

@Configuration
@EnableWebMvc
@ComponentScan
@Import(EnableWebMvcConfiguration.class)
@EnableConfigurationProperties({ WebMvcProperties.class, ResourceProperties.class })

public class CustomWebMvcAutoConfiguration extends  WebMvcConfigurerAdapter {
	@Value("${chat.resource.caching:true}")
	Boolean staticResourceCaching;

	private static final String[] CLASSPATH_RESOURCE_LOCATIONS = {
			"classpath:/META-INF/resources/", "classpath:/resources/",
			"classpath:/static/", "classpath:/public/" };
	@Override
	public void addResourceHandlers(ResourceHandlerRegistry registry) {

		if (!registry.hasMappingForPattern("/getForm/**")) {
			ResourceHandlerRegistration resHandler;
			resHandler = registry.addResourceHandler("/getForm/**").addResourceLocations(
					CLASSPATH_RESOURCE_LOCATIONS);
			if (staticResourceCaching){
				resHandler.setCachePeriod(31556926);
			}
		}
		if (!registry.hasMappingForPattern("/**")) {
			ResourceHandlerRegistration resHandler;
			resHandler = registry.addResourceHandler("/**").addResourceLocations(
					CLASSPATH_RESOURCE_LOCATIONS).setCachePeriod(31556926);
			if (staticResourceCaching){
				resHandler.setCachePeriod(31556926);
			}
		}
		if (!registry.hasMappingForPattern("/js/ITA.js")) {
			ResourceHandlerRegistration resHandler;
			resHandler = registry.addResourceHandler("/js/ITA.js").addResourceLocations(
					CLASSPATH_RESOURCE_LOCATIONS).setCachePeriod(0);
		}
	}


}
