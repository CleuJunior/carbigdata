package br.com.ctkd.gui.model;

import java.time.LocalDate;
import java.time.OffsetDateTime;

public record ClientModel(
        String id,
        String name,
        String cpf,
        LocalDate birthdate,
        OffsetDateTime creationDate,
        OffsetDateTime updateDate,
        boolean deleted
) {}
