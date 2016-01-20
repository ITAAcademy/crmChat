package com.intita.wschat.config;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import redis.clients.jedis.JedisPoolConfig;

@Configuration
@EnableAutoConfiguration 
public class RedisDBConfig {
	@Value("${redis.host}")
	private String redisHost;
	@Value("${redis.port ?:6379}")
	private Integer redisPort;
	@Value("${redis.password ?:}")
	private String redisPassword;
	
	@Bean
	public RedisConnectionFactory jedisConnectionFactory() {
		JedisPoolConfig poolConfig = new JedisPoolConfig();
		poolConfig.setMaxTotal(5);
		poolConfig.setTestOnBorrow(true);
		poolConfig.setTestOnReturn(true);
		JedisConnectionFactory ob = new JedisConnectionFactory(poolConfig);
		ob.setUsePool(true);
		ob.setHostName(redisHost);//"127.0.0.1"
		ob.setPort(redisPort);//6379
		if(!redisPassword.isEmpty())
			ob.setPassword(redisPassword);
		return ob;
	}

	@Bean(name = "RedisConf") 
	public StringRedisTemplate  stringRedisTemplate(){
		return new StringRedisTemplate(jedisConnectionFactory());
	}
	
} 