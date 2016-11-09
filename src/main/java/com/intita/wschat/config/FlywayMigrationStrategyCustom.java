package com.intita.wschat.config;

import org.flywaydb.core.Flyway;
import org.springframework.boot.autoconfigure.flyway.FlywayMigrationStrategy;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FlywayMigrationStrategyCustom implements FlywayMigrationStrategy {

	Flyway flyway;
	@Override
	public void migrate(Flyway flyway) {
		//System.out.println("AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA: " + flyway.getLocations());
		this.flyway = flyway; 
		
	}
	public Flyway getFlyway() {
		return flyway;
	}

}
