package br.com.ctkd.client_consumer.mapper;

import br.com.ctkd.client_consumer.domain.ClientView;
import br.com.ctkd.client_consumer.dto.event.ClientEventDto;
import org.springframework.stereotype.Component;

@Component
public class ClientMapper {

    public ClientView fromDto(ClientEventDto dto) {
        return ClientView.builder()
                .clientId(dto.id())
                .name(dto.name())
                .birthdate(dto.birthdate())
                .cpf(dto.cpf())
                .build();
    }
}