# EquipamentoDAO

`inserir`, `listar`, `buscarPorId`, `buscarPorPatrimonio`,
`buscarPorNome`, `listarPorStatus`, `atualizar` e `excluir` usam JDBC
com PreparedStatement e try-with-resources.

Importante: `atualizar()` modifica somente dados do cadastro, **nao o status**.
O metodo `alterarStatusDisponibilidade()` usa UPDATE condicional atomico
para marcar equipamentos DISPONIVEL ou INDISPONIVEL sem interferir em
emprestimos abertos.

A V1 do Flyway define as restricoes de integridade; veja README.
