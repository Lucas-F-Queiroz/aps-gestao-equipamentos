# EmprestimoDAO

O banco novo tem somente `V1__estrutura_inicial.sql`, que define as tabelas
`usuario`, `equipamento` e `emprestimo`, chaves estrangeiras e integridade.

`EmprestimoDAO.inserir()` verifica usuario ativo, muda equipamento disponivel
para emprestado, confere ausencia de emprestimo aberto e registra o emprestimo
em uma **transacao JDBC**. Se uma etapa falha, executa rollback.

`registrarDevolucao()` atualiza status e data do emprestimo e devolve a
disponibilidade do equipamento na mesma transacao.

`listarDetalhado()` utiliza INNER JOIN para retornar um DTO de leitura
`EmprestimoDetalhe`, sem criar tabela adicional. `ATRASADO` e calculado,
nao armazenado.

Para executar: `iniciar.bat` ou `sh iniciar.sh`; veja README.
