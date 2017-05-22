package com.intita.wschat.services;


import javax.annotation.PostConstruct;

import com.intita.wschat.web.RoomController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;
import redis.clients.jedis.Protocol;
import redis.clients.jedis.exceptions.JedisConnectionException;

@Service
public class RedisService {

	/*@Bean
	@Description("Jedis need it")
	public ConfigurationClassPostProcessor importRegistry() {
		return new ConfigurationClassPostProcessor();
	}*/
	private final static Logger log = LoggerFactory.getLogger(RedisService.class);

	private JedisPool pool;
	private JedisPoolConfig poolConfig = new JedisPoolConfig();
	
	@PostConstruct
	public void AutoUpdate() {
		poolConfig.setMaxTotal(5);
		poolConfig.setTestOnBorrow(true);
		poolConfig.setTestOnReturn(true);
		// this(poolConfig, host, port, timeout, password, Protocol.DEFAULT_DATABASE, null);
		pool = new JedisPool(poolConfig, "localhost", 6379, Protocol.DEFAULT_TIMEOUT, "1234567");
	}


	public String getKeyValue(String key) {

		try (Jedis jedis = pool.getResource()){

		if(!jedis.isConnected())
		{
			//jedis.close();
			jedis.connect();
			return new String();
		}
		try{
		if(jedis != null)
			return  jedis.get(key);
		else
			return new String();
		}
		catch(JedisConnectionException ex)
		{
			/*jedis.close();
			jedis.connect();*/
			log.info("Jedis error:"+ex.getStackTrace());
			/*pool.close();
			pool = new JedisPool(poolConfig, "localhost", 6379, Protocol.DEFAULT_TIMEOUT, "1234567");
			jedis = pool.getResource();
			return new String();*/
		}
		return null;
		}
	}
	
	public void setValueByKey(String key, String value) {
		try (Jedis jedis = pool.getResource()) {
			if (jedis != null)
				jedis.set(key, value);
		}
	}

}
