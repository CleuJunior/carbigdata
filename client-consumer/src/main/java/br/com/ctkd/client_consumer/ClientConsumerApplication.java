package br.com.ctkd.client_consumer;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class ClientConsumerApplication {

	public static void main(String[] args) {
		SpringApplication.run(ClientConsumerApplication.class, args);
	}

}
