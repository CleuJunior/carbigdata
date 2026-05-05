package br.com.ctkd.dto.event;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ClientEvent {

    private String id;
    private String name;
    private LocalDate birthdate;
    private String cpf;
    private String eventType;
}
