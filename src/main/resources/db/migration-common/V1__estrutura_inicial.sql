CREATE TABLE usuario (
    id INT AUTO_INCREMENT PRIMARY KEY,
    nome VARCHAR(100) NOT NULL,
    email VARCHAR(120) NOT NULL UNIQUE,
    tipo VARCHAR(30) NOT NULL,
    ativo BOOLEAN NOT NULL DEFAULT TRUE,

    CONSTRAINT chk_usuario_tipo
        CHECK (tipo IN ('ALUNO', 'PROFESSOR', 'FUNCIONARIO'))
);

CREATE TABLE equipamento (
    id INT AUTO_INCREMENT PRIMARY KEY,
    patrimonio VARCHAR(50) NOT NULL UNIQUE,
    nome VARCHAR(100) NOT NULL,
    categoria VARCHAR(50) NOT NULL,
    estado VARCHAR(30) NOT NULL,
    status VARCHAR(30) NOT NULL DEFAULT 'DISPONIVEL',

    CONSTRAINT chk_equipamento_estado
        CHECK (estado IN ('NOVO', 'BOM', 'REGULAR', 'DANIFICADO')),

    CONSTRAINT chk_equipamento_status
        CHECK (status IN ('DISPONIVEL', 'EMPRESTADO', 'INDISPONIVEL'))
);

CREATE TABLE emprestimo (
    id INT AUTO_INCREMENT PRIMARY KEY,
    usuario_id INT NOT NULL,
    equipamento_id INT NOT NULL,
    data_retirada DATETIME NOT NULL,
    data_prevista DATETIME NOT NULL,
    data_devolucao DATETIME NULL,
    status VARCHAR(30) NOT NULL DEFAULT 'ABERTO',

    CONSTRAINT chk_emprestimo_status
        CHECK (status IN ('ABERTO', 'DEVOLVIDO')),

    CONSTRAINT fk_emprestimo_usuario
        FOREIGN KEY (usuario_id)
        REFERENCES usuario(id)
        ON UPDATE CASCADE
        ON DELETE RESTRICT,

    CONSTRAINT fk_emprestimo_equipamento
        FOREIGN KEY (equipamento_id)
        REFERENCES equipamento(id)
        ON UPDATE CASCADE
        ON DELETE RESTRICT
);

CREATE INDEX idx_emprestimo_usuario
    ON emprestimo(usuario_id);

CREATE INDEX idx_emprestimo_equipamento
    ON emprestimo(equipamento_id);

CREATE INDEX idx_emprestimo_status
    ON emprestimo(status);
