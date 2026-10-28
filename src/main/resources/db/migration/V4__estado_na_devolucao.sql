-- Guarda COMO o equipamento estava quando foi devolvido (alimenta a tela Relatorios).
-- Tabela nova e aditiva: nao altera usuario, equipamento nem emprestimo (V1, V2 e V3 ficam como estao).
-- Uma linha por emprestimo devolvido pela interface (emprestimos devolvidos antes da V4 nao tem linha).
CREATE TABLE devolucao (
    emprestimo_id INT NOT NULL PRIMARY KEY,
    estado VARCHAR(30) NOT NULL,
    observacao VARCHAR(255) NULL,

    CONSTRAINT chk_devolucao_estado
        CHECK (estado IN ('NOVO', 'BOM', 'REGULAR', 'DANIFICADO')),

    CONSTRAINT fk_devolucao_emprestimo
        FOREIGN KEY (emprestimo_id)
        REFERENCES emprestimo(id)
        ON UPDATE CASCADE
        ON DELETE RESTRICT
);
