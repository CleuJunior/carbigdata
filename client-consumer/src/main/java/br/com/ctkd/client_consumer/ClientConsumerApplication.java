package br.com.ctkd.client_consumer;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.context.annotation.Bean;

import java.util.List;

@SpringBootApplication
@ConfigurationPropertiesScan
public class ClientConsumerApplication {

	public static void main(String[] args) {
		SpringApplication.run(ClientConsumerApplication.class, args);
	}

	@Bean
	CommandLineRunner run() {
		return args -> {
			var test = equacaoSegundoGrau(1, 6, 9);

			System.out.printf("");

		};
	}

	private List<Double> equacaoSegundoGrau(int a, int b, int c) {
		int delta = b * b - 4 * a * c;

		var mais = (-b + Math.sqrt(delta)) / (2 * a);
		var menos = (-b - Math.sqrt(delta)) / (2 * a);

		return List.of(mais, menos);
	}

	private List<Double> equacaoSegundoGrau(int a, int b) {
		int evidencia = a * (a + b);

		return List.of();
	}
}
