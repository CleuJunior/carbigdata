INSERT INTO client (id, client_name, birthdate, cpf_number, creation_date, update_date, deleted)
VALUES (gen_random_uuid(), 'Carlos Silva', '1990-03-15', '123.456.789-00', NOW(), NOW(), false),
       (gen_random_uuid(), 'Ana Souza', '1985-07-22', '987.654.321-00', NOW(), NOW(), false),
       (gen_random_uuid(), 'Pedro Oliveira', '1992-11-08', '456.789.123-00', NOW(), NOW(), false),
       (gen_random_uuid(), 'Mariana Costa', '1995-01-30', '321.654.987-00', NOW(), NOW(), false),
       (gen_random_uuid(), 'Lucas Ferreira', '1988-09-12', '654.321.098-00', NOW(), NOW(), false);