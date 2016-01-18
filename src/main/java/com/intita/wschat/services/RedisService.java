package com.intita.wschat.services;


import org.springframework.context.annotation.ConfigurationClassPostProcessor;
import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ConfigurationClassPostProcessor;
import org.springframework.context.annotation.Description;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import com.intita.wschat.config.RedisDBConfig;
import redis.clients.jedis.Jedis;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Service
public class RedisService {
	@Autowired RedisDBConfig configureRedis;
	Jedis jedis;


	private StringRedisTemplate stringRedisTemplate;

	/*@Bean
	@Description("Jedis need it")
	public ConfigurationClassPostProcessor importRegistry() {
		return new ConfigurationClassPostProcessor();
	}*/

	@PostConstruct
	public void AutoUpdate() {
		stringRedisTemplate = configureRedis.stringRedisTemplate();

		// Using set to set value
		stringRedisTemplate.opsForValue().set("R", "Ram");
		stringRedisTemplate.opsForValue().set("S", "Shyam");
		//Fetch values from set
		System.out.println(stringRedisTemplate.opsForValue().get("R"));
		System.out.println(stringRedisTemplate.opsForValue().get("S"));
		//Using Hash Operation
		String mohan = "Mohan";
		stringRedisTemplate.opsForHash().put("M", String.valueOf(mohan.hashCode()),mohan);
		System.out.println(stringRedisTemplate.opsForHash().get("M", String.valueOf(mohan.hashCode())));

		jedis = new Jedis("localhost");

		System.out.println("<<<<<<<<<<<< " + getKeyValue("R"));

		/*Set<String> list_set = jedis.keys("*");
		List<String> list = new ArrayList<String>();
		list.addAll(list_set);
		for(int i=0; i<list.size(); i++) {
			System.out.println("List of stored keys:: "+list.get(i));
		}*/
	}

	public List<String> getAllKeys() {
		Set<String> list_set = jedis.keys("*");
		List<String> list = new ArrayList<String>();
		list.addAll(list_set);
		return list;
	}

	public String getKeyValue(String key) {
		return  jedis.get(key);
	}

}
