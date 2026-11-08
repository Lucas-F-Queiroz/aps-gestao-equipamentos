-- Dados de EXEMPLO para apresentar o sistema. Use apenas em banco de desenvolvimento e VAZIO
-- (rodar duas vezes causa erro de ID/e-mail repetido). As datas sao calculadas a partir de hoje.
-- Execute depois que o sistema ja tiver aberto uma vez (o Flyway cria as tabelas) e com o banco
-- aps_equipamentos selecionado.
SET NAMES utf8mb4;

INSERT INTO usuario (id, nome, email, area_profissao, ativo) VALUES
    (1, 'Ana Souza',   'ana@empresa.com',   'FUNCIONARIO', TRUE),
    (2, 'Bruno Lima',  'bruno@empresa.com', 'PROFESSOR',   TRUE),
    (3, 'Carla Dias',  'carla@empresa.com', 'ALUNO',       TRUE),
    (4, 'Diego Rocha', 'diego@empresa.com', 'FUNCIONARIO', FALSE),
    (5, 'Elisa Prado', 'elisa@empresa.com', 'ALUNO',       TRUE);

INSERT INTO equipamento (id, patrimonio, nome, categoria, estado, status, modelo, marca, numero_serie) VALUES
    (1, 'PAT-001', 'Notebook Dell Latitude 5420',  'Notebook', 'BOM',        'EMPRESTADO', 'Modelo exemplo', 'Marca exemplo', 'PAT-001'),
    (2, 'PAT-002', 'Notebook Lenovo ThinkPad E14', 'Notebook', 'NOVO',       'DISPONIVEL', 'Modelo exemplo', 'Marca exemplo', 'PAT-002'),
    (3, 'PAT-003', 'Celular Samsung Galaxy S23',   'Celular',  'BOM',        'DISPONIVEL', 'Modelo exemplo', 'Marca exemplo', 'PAT-003'),
    (4, 'PAT-004', 'Notebook HP ProBook 450',      'Notebook', 'REGULAR',    'EMPRESTADO', 'Modelo exemplo', 'Marca exemplo', 'PAT-004'),
    (5, 'PAT-005', 'Celular Motorola Edge 40',     'Celular',  'DANIFICADO', 'INDISPONIVEL', 'Modelo exemplo', 'Marca exemplo', 'PAT-005'),
    (6, 'PAT-006', 'Notebook Acer Aspire 5',       'Notebook', 'REGULAR',    'DISPONIVEL', 'Modelo exemplo', 'Marca exemplo', 'PAT-006');

-- Emprestimos ja devolvidos (retirada, prazo de 7 dias, devolucao)
INSERT INTO emprestimo (id, usuario_id, equipamento_id, data_retirada, data_prevista, data_devolucao, status) VALUES
    (4, 1, 5, TIMESTAMP(CURDATE() - INTERVAL 62 DAY, '10:05:12'), TIMESTAMP(CURDATE() - INTERVAL 55 DAY, '23:59:59'),
        TIMESTAMP(CURDATE() - INTERVAL 60 DAY, '15:40:02'), 'DEVOLVIDO'),
    (5, 5, 2, TIMESTAMP(CURDATE() - INTERVAL 47 DAY, '09:20:30'), TIMESTAMP(CURDATE() - INTERVAL 40 DAY, '23:59:59'),
        TIMESTAMP(CURDATE() - INTERVAL 45 DAY, '11:05:18'), 'DEVOLVIDO'),
    (6, 4, 6, TIMESTAMP(CURDATE() - INTERVAL 33 DAY, '14:40:03'), TIMESTAMP(CURDATE() - INTERVAL 26 DAY, '23:59:59'),
        TIMESTAMP(CURDATE() - INTERVAL 30 DAY, '16:12:44'), 'DEVOLVIDO'),
    (3, 3, 3, TIMESTAMP(CURDATE() - INTERVAL 20 DAY, '10:30:45'), TIMESTAMP(CURDATE() - INTERVAL 13 DAY, '23:59:59'),
        TIMESTAMP(CURDATE() - INTERVAL 13 DAY, '16:20:05'), 'DEVOLVIDO');

-- Emprestimos abertos: o do Bruno esta atrasado (a data prevista ja passou)
INSERT INTO emprestimo (id, usuario_id, equipamento_id, data_retirada, data_prevista, data_devolucao, status) VALUES
    (2, 2, 4, TIMESTAMP(CURDATE() - INTERVAL 12 DAY, '14:02:11'), TIMESTAMP(CURDATE() - INTERVAL 5 DAY, '23:59:59'), NULL, 'ABERTO'),
    (1, 1, 1, TIMESTAMP(CURDATE() - INTERVAL 3 DAY, '09:15:20'),  TIMESTAMP(CURDATE() + INTERVAL 4 DAY, '23:59:59'), NULL, 'ABERTO');

-- Como cada equipamento estava na devolucao (tela Relatorios)
INSERT INTO devolucao (emprestimo_id, estado, observacao) VALUES
    (4, 'DANIFICADO', 'Tela trincada'),
    (5, 'NOVO',       'Entregue com todos os acessórios'),
    (6, 'REGULAR',    'Teclado com tecla solta'),
    (3, 'BOM',        NULL);
