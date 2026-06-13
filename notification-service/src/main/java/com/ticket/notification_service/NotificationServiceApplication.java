package com.ticket.notification_service;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import java.util.Properties;

@SpringBootApplication
public class NotificationServiceApplication {

	public static void main(String[] args) {
		SpringApplication application = new SpringApplication(NotificationServiceApplication.class);
		Properties props = new Properties();
		props.put("server.port", "8084");
		props.put("spring.application.name", "notification-service");


		props.put("spring.kafka.bootstrap-servers", "localhost:9092");
		props.put("spring.kafka.consumer.group-id", "notification-group");
		props.put("spring.kafka.consumer.auto-offset-reset", "earliest");
		props.put("spring.kafka.consumer.key-deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
		props.put("spring.kafka.consumer.value-deserializer", "org.apache.kafka.common.serialization.StringDeserializer");

		application.setDefaultProperties(props);
		application.run(args);
	}


	@Bean
	public JavaMailSender javaMailSender() {
		JavaMailSenderImpl mailSenderImpl = new JavaMailSenderImpl();
		mailSenderImpl.setHost("smtp.gmail.com");
		mailSenderImpl.setPort(587);

		mailSenderImpl.setUsername("jai07@gmail.com");
		mailSenderImpl.setPassword("pczrhxvhspcfzjks");

		Properties javaMailProps = mailSenderImpl.getJavaMailProperties();
		javaMailProps.put("mail.transport.protocol", "smtp");
		javaMailProps.put("mail.smtp.auth", "true");
		javaMailProps.put("mail.smtp.starttls.enable", "true");
		javaMailProps.put("mail.smtp.starttls.required", "true");
		javaMailProps.put("mail.debug", "true");

		return mailSenderImpl;
	}
}