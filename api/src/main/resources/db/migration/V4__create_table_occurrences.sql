CREATE TYPE status_occurrence AS ENUM ('ACTIVE', 'FINISHED');

CREATE TABLE occurrences
(
    id              UUID PRIMARY KEY     DEFAULT gen_random_uuid(),
    client_id       UUID        NOT NULL,
    address_id      UUID        NOT NULL,
    occurrence_date DATE        NOT NULL,
    status          VARCHAR(20) NOT NULL,
    creation_date   TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    update_date     TIMESTAMPTZ,
    deleted         BOOLEAN     NOT NULL DEFAULT FALSE,

    CONSTRAINT fk_occurrence_client FOREIGN KEY (client_id) REFERENCES clients (id),
    CONSTRAINT fk_occurrence_address FOREIGN KEY (address_id) REFERENCES addresses (id)
);

CREATE INDEX idx_occurrences_client_id ON occurrences (client_id);
CREATE INDEX idx_occurrences_address_id ON occurrences (address_id);