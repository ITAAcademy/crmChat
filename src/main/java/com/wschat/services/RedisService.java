package com.wschat.services;

import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import com.wschat.config.RedisDBConfig;

@Service
public class RedisService {
	public RedisService() {
		/*   AnnotationConfigApplicationContext ctx = new AnnotationConfigApplicationContext();
	       ctx.register(RedisDBConfig.class);
	       ctx.refresh();
	       StringRedisTemplate stringRedisTemplate  = ctx.getBean(StringRedisTemplate.class);
	       // Using set to set value
	       stringRedisTemplate.opsForValue().set("R", "Ram");
	       stringRedisTemplate.opsForValue().set("S", "Shyam");
	       //Fetch values from set
	       System.out.println(stringRedisTemplate.opsForValue().get("R"));
	       System.out.println(stringRedisTemplate.opsForValue().get("S"));
	       //Using Hash Operation
	       String mohan = "Mohan";
	       stringRedisTemplate.opsForHash().put("M", String.valueOf(mohan.hashCode()),mohan);
	       System.out.println(stringRedisTemplate.opsForHash().get("M", String.valueOf(mohan.hashCode())));*/
	}
}
