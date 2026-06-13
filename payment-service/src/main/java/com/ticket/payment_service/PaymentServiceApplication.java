package com.ticket.payment_service;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.kafka.annotation.KafkaListener;
import java.util.Properties;

@SpringBootApplication
public class PaymentServiceApplication {

	public static void main(String[] args) {
		SpringApplication application = new SpringApplication(PaymentServiceApplication.class);
		Properties props = new Properties();


		props.put("server.port", "8083");
		props.put("spring.application.name", "payment-service");


		props.put("spring.datasource.url", "jdbc:postgresql://localhost:5433/payment_db");
		props.put("spring.datasource.username", "postgres");
		props.put("spring.datasource.password", "postgres");
		props.put("spring.datasource.driver-class-name", "org.postgresql.Driver");
		props.put("spring.jpa.hibernate.ddl-auto", "update");
		props.put("spring.jpa.show-sql", "true");
		props.put("spring.jpa.properties.hibernate.dialect", "org.hibernate.dialect.PostgreSQLDialect");


		props.put("spring.kafka.bootstrap-servers", "localhost:9092");
		props.put("spring.kafka.consumer.group-id", "payment-group");
		props.put("spring.kafka.consumer.auto-offset-reset", "earliest");
		props.put("spring.kafka.consumer.key-deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
		props.put("spring.kafka.consumer.value-deserializer", "org.apache.kafka.common.serialization.StringDeserializer");

		application.setDefaultProperties(props);
		application.run(args);
	}


	@KafkaListener(topics = "booking-initiated-topic")
	public void processTicketPayment(String message) {
		System.out.println("==================================================");
		System.out.println("KAFKA INTERCEPTED STREAM EVENT: Processing Payment for -> " + message);
		System.out.println("==================================================");
	}
}