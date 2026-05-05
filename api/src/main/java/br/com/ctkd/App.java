package br.com.ctkd;

import br.com.ctkd.domain.Client;
import br.com.ctkd.producer.ClientEventProducer;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.context.annotation.Bean;

import java.time.LocalDate;
import java.util.UUID;

@SpringBootApplication(scanBasePackages = "br.com.ctkd")
@ConfigurationPropertiesScan
public class App {

	public static void main(String[] args) {
		SpringApplication.run(App.class, args);
	}


	@Bean
	CommandLineRunner runner(ClientEventProducer producer) {
		return args -> {
				var client = new Client();
				client.setId(UUID.randomUUID());
				client.setName("Cleonildo Teste Junior");
				client.setBirthdate(LocalDate.now());
				client.setCpf("92928371623");

			producer.publish(client, "CREATED");

		};
	}
}
