package com.ticket.booking_service;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import java.util.Properties;

@SpringBootApplication
public class BookingServiceApplication {

	public static void main(String[] args) {
		SpringApplication application = new SpringApplication(BookingServiceApplication.class);
		Properties props = new Properties();

		// Identity & Port Settings
		props.put("server.port", "8081");
		props.put("spring.application.name", "booking-service");

		// Database Configuration (Docker Port 5433)
		props.put("spring.datasource.url", "jdbc:postgresql://localhost:5433/ticket_db");
		props.put("spring.datasource.username", "postgres");
		props.put("spring.datasource.password", "postgres");
		props.put("spring.datasource.driver-class-name", "org.postgresql.Driver");
		props.put("spring.jpa.hibernate.ddl-auto", "update");
		props.put("spring.jpa.show-sql", "true");
		props.put("spring.jpa.properties.hibernate.dialect", "org.hibernate.dialect.PostgreSQLDialect");

		// Redis Lock Settings
		props.put("spring.data.redis.host", "localhost");
		props.put("spring.data.redis.port", "6379");

		// Kafka Broker Configuration
		props.put("spring.kafka.bootstrap-servers", "localhost:9092");
		props.put("spring.kafka.producer.key-serializer", "org.apache.kafka.common.serialization.StringSerializer");
		props.put("spring.kafka.producer.value-serializer", "org.apache.kafka.common.serialization.StringSerializer");

		application.setDefaultProperties(props);
		application.run(args);
	}

	// === GLOBAL CORS CONFIGURATION BEAN TO OPEN THE NETWORK CHANNELS ===
	@Bean
	public WebMvcConfigurer corsConfigurer() {
		return new WebMvcConfigurer() {
			@Override
			public void addCorsMappings(CorsRegistry registry) {
				registry.addMapping("/**")
						.allowedOrigins("*") // Allows IntelliJ's port 63342 to connect smoothly
						.allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
						.allowedHeaders("*");
			}
		};
	}
}