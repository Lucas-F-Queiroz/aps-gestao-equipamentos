-- A coluna gerada contem o equipamento apenas para emprestimos ABERTOS.
-- O indice UNIQUE permite multiplos NULL (historico DEVOLVIDO), mas
-- impede dois emprestimos ABERTOS para o mesmo equipamento.
ALTER TABLE emprestimo
    ADD COLUMN equipamento_aberto_id INT
        GENERATED ALWAYS AS (
            CASE WHEN status = 'ABERTO' THEN equipamento_id ELSE NULL END
        ) STORED,
    ADD CONSTRAINT uq_emprestimo_equipamento_aberto
        UNIQUE (equipamento_aberto_id),
    ADD CONSTRAINT chk_emprestimo_datas
        CHECK (data_prevista > data_retirada
            AND (data_devolucao IS NULL OR data_devolucao >= data_retirada)),
    ADD CONSTRAINT chk_emprestimo_devolucao
        CHECK (
            (status = 'ABERTO' AND data_devolucao IS NULL)
            OR (status = 'DEVOLVIDO' AND data_devolucao IS NOT NULL)
        );
