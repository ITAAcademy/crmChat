package com.intita.wschat.services;


import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;
import redis.clients.jedis.Protocol;

@Service
public class RedisService {

	/*@Bean
	@Description("Jedis need it")
	public ConfigurationClassPostProcessor importRegistry() {
		return new ConfigurationClassPostProcessor();
	}*/

	private Jedis jedis;
	private JedisPool pool;

	@PostConstruct
	public void AutoUpdate() {
		
		
		JedisPoolConfig poolConfig = new JedisPoolConfig();
		poolConfig.setMaxTotal(5);
		poolConfig.setTestOnBorrow(true);
		poolConfig.setTestOnReturn(true);
		// this(poolConfig, host, port, timeout, password, Protocol.DEFAULT_DATABASE, null);
		pool = new JedisPool(poolConfig, "localhost", 6379, Protocol.DEFAULT_TIMEOUT, "1234567");
		try {
			jedis = pool.getResource();
		} catch (Exception e) {
			System.out.println("@@@_REDIS_ERR@@@");
			System.out.println(e.getMessage());
		}
	}


	public String getKeyValue(String key) {
		return  jedis.get(key);
	}
	
	public void setValueByKey(String key, String value) {
		jedis.set(key, value);
	}

}
