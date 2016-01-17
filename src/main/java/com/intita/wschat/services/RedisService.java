package com.intita.wschat.services;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import com.intita.wschat.config.RedisDBConfig;

@Service
public class RedisService {
	@Autowired RedisDBConfig configureRedis;
	
	private StringRedisTemplate stringRedisTemplate;
	
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
	}
	
}
