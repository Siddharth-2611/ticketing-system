package com.ticket.inventory_service;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import java.util.Properties;

@SpringBootApplication
public class InventoryServiceApplication {

	public static void main(String[] args) {
		SpringApplication application = new SpringApplication(InventoryServiceApplication.class);
		Properties props = new Properties();

		// 1. Identity & Port Settings for Inventory
		props.put("server.port", "8082");
		props.put("spring.application.name", "inventory-service");

		// 2. PostgreSQL Configuration (Routed to Docker Port 5433)
		props.put("spring.datasource.url", "jdbc:postgresql://localhost:5433/inventory_db");
		props.put("spring.datasource.username", "postgres");
		props.put("spring.datasource.password", "postgres");
		props.put("spring.datasource.driver-class-name", "org.postgresql.Driver");
		props.put("spring.jpa.hibernate.ddl-auto", "update");
		props.put("spring.jpa.show-sql", "true");

		// Explicitly force Dialect to bypass metadata autodetect lockouts
		props.put("spring.jpa.properties.hibernate.dialect", "org.hibernate.dialect.PostgreSQLDialect");

		application.setDefaultProperties(props);
		application.run(args);
	}
}