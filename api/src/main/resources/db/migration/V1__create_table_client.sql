CREATE TABLE clients
(
    id            UUID PRIMARY KEY      DEFAULT gen_random_uuid(),
    client_name   VARCHAR(255) NOT NULL,
    birthdate     DATE         NOT NULL,
    cpf_number    VARCHAR(14)  NOT NULL UNIQUE,
    creation_date TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    update_date   TIMESTAMPTZ,
    deleted       BOOLEAN      NOT NULL DEFAULT FALSE
);