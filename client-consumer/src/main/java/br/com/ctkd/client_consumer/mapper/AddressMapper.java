package br.com.ctkd.client_consumer.mapper;

import br.com.ctkd.client_consumer.domain.AddressView;
import br.com.ctkd.client_consumer.dto.event.AddressEventDto;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class AddressMapper {

    public AddressView fromEvent(AddressEventDto dto) {
        return AddressView.builder()
                .addressId(dto.id())
                .streetName(dto.streetName())
                .neighborhood(dto.neighborhood())
                .zipCode(dto.zipCode())
                .city(dto.city())
                .state(dto.state())
                .build();
    }

    public AddressView apply(AddressView view, AddressEventDto dto) {
        Optional.ofNullable(dto.streetName()).ifPresent(view::setStreetName);
        Optional.ofNullable(dto.neighborhood()).ifPresent(view::setNeighborhood);
        Optional.ofNullable(dto.zipCode()).ifPresent(view::setZipCode);
        Optional.ofNullable(dto.city()).ifPresent(view::setCity);
        Optional.ofNullable(dto.state()).ifPresent(view::setState);

        return view;
    }
}