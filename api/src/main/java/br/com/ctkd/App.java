package br.com.ctkd;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = "br.com.ctkd")
public class App {

	public static void main(String[] args) {
		SpringApplication.run(App.class, args);
	}

}
