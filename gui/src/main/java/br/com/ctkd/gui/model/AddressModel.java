package br.com.ctkd.gui.model;

import java.time.OffsetDateTime;

public record AddressModel(
        String id,
        String streetName,
        String neighborhood,
        String zipCode,
        String city,
        String state,
        OffsetDateTime creationDate,
        OffsetDateTime updateDate,
        boolean deleted
) {}
