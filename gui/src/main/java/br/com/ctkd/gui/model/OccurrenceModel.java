package br.com.ctkd.gui.model;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;

public record OccurrenceModel(
        // Occurrence
        String id,
        LocalDate occurrenceDate,
        String status,
        OffsetDateTime creationDate,
        OffsetDateTime updateDate,
        boolean deleted,

        // Client (nested)
        String clientId,
        String clientName,
        String clientCpf,
        LocalDate clientBirthdate,

        // Address (nested)
        String addressId,
        String addressStreetName,
        String addressNeighborhood,
        String addressZipCode,
        String addressCity,
        String addressState,

        // Photos
        List<PhotoModel> photos
) {}
