-- A coluna virtual so guarda o ID do equipamento quando ha emprestimo ABERTO.
-- UNIQUE permite multiplos NULL para registros DEVOLVIDOS (historico),
-- mas apenas um emprestimo ABERTO por equipamento.
--
-- VIRTUAL e usado porque a V1 possui FK com ON UPDATE CASCADE.
-- Uma coluna gerada STORED sobre equipamento_id conflita com essa regra
-- referencial no MySQL 8.4; VIRTUAL evita modificar a FK historica.
ALTER TABLE emprestimo
    ADD COLUMN equipamento_aberto_id INT
        GENERATED ALWAYS AS (
            CASE WHEN status = 'ABERTO' THEN equipamento_id ELSE NULL END
        ) VIRTUAL,
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
