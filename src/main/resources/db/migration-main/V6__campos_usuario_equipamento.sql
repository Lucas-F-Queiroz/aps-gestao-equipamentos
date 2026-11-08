-- Campos novos do cadastro (interface do sistema):
--   usuario: matricula (opcional, unica) e area_profissao (texto livre, substitui o tipo fixo)
--   equipamento: modelo, marca, numero_serie (unico), sistema_operacional, departamento
-- Nao altera a V1 nem a V2 (ja aplicadas). Linhas antigas recebem valores de preenchimento.

-- ---------- usuario ----------
ALTER TABLE usuario
    ADD COLUMN matricula VARCHAR(50) NULL,
    ADD COLUMN area_profissao VARCHAR(100) NULL;

UPDATE usuario
SET area_profissao = CASE tipo
    WHEN 'ALUNO' THEN 'Aluno'
    WHEN 'PROFESSOR' THEN 'Professor'
    ELSE 'Funcionário'
END;

ALTER TABLE usuario
    MODIFY COLUMN area_profissao VARCHAR(100) NOT NULL,
    ADD CONSTRAINT uq_usuario_matricula UNIQUE (matricula);

-- O tipo fixo (ALUNO/PROFESSOR/FUNCIONARIO) foi descartado pelo grupo.
-- A regra CHECK precisa sair antes da coluna.
ALTER TABLE usuario DROP CHECK chk_usuario_tipo;
ALTER TABLE usuario DROP COLUMN tipo;

-- ---------- equipamento ----------
-- O tipo do sistema (Notebook, Celular...) continua na coluna categoria.
ALTER TABLE equipamento
    ADD COLUMN modelo VARCHAR(100) NULL,
    ADD COLUMN marca VARCHAR(100) NULL,
    ADD COLUMN numero_serie VARCHAR(100) NULL,
    ADD COLUMN sistema_operacional VARCHAR(100) NULL,
    ADD COLUMN departamento VARCHAR(100) NULL;

UPDATE equipamento
SET modelo = 'NAO INFORMADO',
    marca = 'NAO INFORMADO',
    numero_serie = CONCAT('SEM-SERIE-', id);

ALTER TABLE equipamento
    MODIFY COLUMN modelo VARCHAR(100) NOT NULL,
    MODIFY COLUMN marca VARCHAR(100) NOT NULL,
    MODIFY COLUMN numero_serie VARCHAR(100) NOT NULL,
    ADD CONSTRAINT uq_equipamento_numero_serie UNIQUE (numero_serie);
