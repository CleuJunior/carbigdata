package br.com.ctkd.gui.model;

import java.time.OffsetDateTime;

public record PhotoModel(
        String id,
        String occurrenceId,
        String pathBucket,
        String url,
        String hash,
        OffsetDateTime creationDate,
        OffsetDateTime updateDate,
        boolean deleted
) {}
