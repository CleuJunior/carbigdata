CREATE TABLE photos_occurrence
(
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    cod_occurrence  UUID         NOT NULL,
    dsc_path_bucket VARCHAR(255) NOT NULL,
    dsc_hash        VARCHAR(255) NOT NULL,
    creation_date   TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    update_date     TIMESTAMPTZ,
    deleted         BOOLEAN     NOT NULL DEFAULT FALSE,

    CONSTRAINT fk_photos_occurrence_occurrence FOREIGN KEY (cod_occurrence) REFERENCES occurrences (id)
);