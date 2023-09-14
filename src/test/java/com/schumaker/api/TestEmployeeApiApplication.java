package com.schumaker.api;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Bean;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.containers.RabbitMQContainer;
import org.testcontainers.utility.DockerImageName;

@TestConfiguration(proxyBeanMethods = false)
public class TestEmployeeApiApplication {

	@Bean
	@ServiceConnection
	PostgreSQLContainer<?> postgreSQLContainer() {
		return new PostgreSQLContainer<>(DockerImageName.parse("postgres:15"));
	}

	@Bean
	@ServiceConnection
	RabbitMQContainer rabbitContainer() {
		return new RabbitMQContainer(DockerImageName.parse("rabbitmq:3.8"));
	}

	public static void main(String[] args) {
		SpringApplication.from(EmployeeApiApplication::main).with(TestEmployeeApiApplication.class).run(args);
	}
}
