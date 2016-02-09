package com.intita.wschat.config;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.zaxxer.hikari.HikariDataSource;
@Configuration
public class HikariCPConfig {
	 @Value("${hikari.driverclassname}")
	    private String driverClassName;

	    @Value("${hikari.jdbc.url}")
	    private String jdbcUrl;

	    @Value("${hikari.username}")
	    private String userName;

	    @Value("${hikari.password}")
	    private String password;

	    @Value("${hikari.pool.size}")
	    private int poolSize;

	    @Bean(destroyMethod = "close")
	    public DataSource dataSource() {
	        final HikariDataSource ds = new HikariDataSource();
	        ds.setMaximumPoolSize(poolSize);
	        ds.setDriverClassName(driverClassName);
	        ds.setJdbcUrl(jdbcUrl);
	        ds.setUsername(userName);
	        ds.setPassword(password);
	        return ds;
	    }
}
