package com.wschat.config;

import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceBuilder;
import org.springframework.boot.context.properties.ConfigurationProperties; 
import org.springframework.context.annotation.Bean; 
import org.springframework.context.annotation.Configuration; 
import org.springframework.jdbc.core.JdbcTemplate; 
 
import javax.sql.DataSource; 
 
@Configuration
@EnableAutoConfiguration 
public class IntitaDBConfig  { 
 
    @Bean(name = "IntitaDataSourse") 
    @ConfigurationProperties(prefix="datasource.intita")
    public DataSource DataSource() {
        return DataSourceBuilder.create().build();
    }
    
    @Bean(name = "IntitaConf") 
    public JdbcTemplate jdbcTemplate(DataSource dsUsers) { 
        return new JdbcTemplate(DataSource()); 
    } 
 
} 