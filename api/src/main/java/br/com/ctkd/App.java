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

            var client2 = new Client();
            client2.setId(UUID.randomUUID());
            client2.setName("Mais um teste");
            client2.setBirthdate(LocalDate.now());
            client2.setCpf("9938347123");

            var client3 = new Client();
            client3.setId(UUID.randomUUID());
            client3.setName("Teste final");
            client3.setBirthdate(LocalDate.now());
            client3.setCpf("8883893471");

            producer.publish(client, client2, client3);

        };
    }
}
