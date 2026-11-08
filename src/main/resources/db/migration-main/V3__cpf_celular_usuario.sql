-- Campos opcionais preservam os cadastros anteriores sem inventar dados pessoais.
ALTER TABLE usuario
    ADD COLUMN cpf VARCHAR(11) NULL,
    ADD COLUMN celular VARCHAR(11) NULL,
    ADD CONSTRAINT uq_usuario_cpf UNIQUE (cpf),
    ADD CONSTRAINT chk_usuario_cpf_formato
        CHECK (cpf IS NULL OR REGEXP_LIKE(cpf, '^[0-9]{11}$', 'c')),
    ADD CONSTRAINT chk_usuario_celular_formato
        CHECK (celular IS NULL OR REGEXP_LIKE(celular, '^[1-9][0-9]9[0-9]{8}$', 'c'));
