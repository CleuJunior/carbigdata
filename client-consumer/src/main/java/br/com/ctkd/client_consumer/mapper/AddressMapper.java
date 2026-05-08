package br.com.ctkd.client_consumer.mapper;

import br.com.ctkd.client_consumer.domain.AddressView;
import br.com.ctkd.client_consumer.domain.ClientView;
import br.com.ctkd.client_consumer.dto.event.AddressEventDto;
import br.com.ctkd.client_consumer.dto.event.ClientEventDto;
import org.springframework.stereotype.Component;

@Component
public class AddressMapper {

    public AddressView fromDto(AddressEventDto dto) {
        return AddressView.builder()
                .addressId(dto.id())
                .streetName(dto.streetName())
                .neighborhood(dto.neighborhood())
                .zipCode(dto.zipCode())
                .city(dto.city())
                .state(dto.state())
                .build();
    }
}