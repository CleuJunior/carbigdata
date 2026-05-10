package br.com.ctkd.mail;

import br.com.ctkd.mail.service.EmailService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
@ConfigurationPropertiesScan
public class EmailSenderApplication {

	public static void main(String[] args) {
		SpringApplication.run(EmailSenderApplication.class, args);
	}


	@Bean
	CommandLineRunner runner(EmailService service) {
		return args -> {
			service.sendWelcome("mt4ft@wshu.net", "João", "http://localhost:8080");

		};
	}

}
