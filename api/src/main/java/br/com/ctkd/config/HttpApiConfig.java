package br.com.ctkd.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatusCode;
import org.springframework.web.client.RestClient;

@Configuration
public class HttpApiConfig {


    @Bean
    public RestClient client() {
        return RestClient.builder()
                .baseUrl("https://pokeapi.co/api/v2/pokemon?offset=20&limit=20")
                .defaultStatusHandler(HttpStatusCode::isError, (req, res) -> {})
                .build();
    }
}
