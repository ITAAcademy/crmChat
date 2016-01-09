package com.sergialmar.wschat.config;

import java.io.File;
import java.util.ArrayList;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanDefinitionRegistryPostProcessor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.session.data.redis.config.annotation.web.http.EnableRedisHttpSession;

import redis.clients.jedis.Protocol;
import redis.embedded.RedisExecProvider;
import redis.embedded.RedisServer;

/**
 * Redis is required for {@link EnableRedisHttpSession} handling.
 */
@Configuration
public class RedisConfig {

	@Bean
	public static RedisServerBean redisServer() {
		return new RedisServerBean();
	}
	
	/**
	 * Implements BeanDefinitionRegistryPostProcessor to ensure this Bean is
	 * initialized before any other Beans. Specifically, we want to ensure that
	 * the Redis Server is started before RedisHttpSessionConfiguration attempts
	 * to enable Keyspace notifications.
	 */
	static class RedisServerBean implements InitializingBean, DisposableBean, BeanDefinitionRegistryPostProcessor {

		private RedisWithConfList redisServer;
		

		@Override
		public void afterPropertiesSet() throws Exception {
			ArrayList<String> list = new ArrayList<>();
			list.add("--dir");
			list.add(".\\");
			list.add("--maxheap");
			list.add("1gb");
			list.add("--heapdir");
			list.add(".\\");
			 //--maxheap 1gb --heapdir .\\
			
			redisServer = new RedisWithConfList(list, Protocol.DEFAULT_PORT);
		//	redisServer.enableConfigFile("Z:\\Desktop\\redis-config-master\\sentinel3\\windows\\redis.conf");
			redisServer.start();
		}

		@Override
		public void destroy() throws Exception {
			if (redisServer != null) {
				redisServer.stop();
			}
		}

		@Override
		public void postProcessBeanDefinitionRegistry(BeanDefinitionRegistry registry) throws BeansException {
		}

		@Override
		public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
		}

	}
}