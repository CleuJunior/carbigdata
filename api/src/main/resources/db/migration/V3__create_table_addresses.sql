CREATE TABLE addresses
(
    id            UUID PRIMARY KEY      DEFAULT gen_random_uuid(),
    street_name   VARCHAR(255) NOT NULL,
    neighborhood  VARCHAR(255) NOT NULL,
    zip_code      VARCHAR(10)  NOT NULL,
    city_name     VARCHAR(255) NOT NULL,
    state_name    VARCHAR(50)  NOT NULL,
    creation_date TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    update_date   TIMESTAMPTZ,
    deleted       BOOLEAN      NOT NULL DEFAULT FALSE
);