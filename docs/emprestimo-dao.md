# EmprestimoDAO

O banco novo tem somente `V1__estrutura_inicial.sql`, com
as tabelas `usuario`, `equipamento` e `emprestimo`, chaves estrangeiras e restricoes.

`inserir()` verifica se o usuario esta ativo, marca o equipamento como
emprestado e registra o emprestimo na **mesma transacao JDBC**.
Se ocorrer erro, utiliza `rollback()` para desfazer alteracoes parciais.

`registrarDevolucao()` registra a data de devolucao, muda o emprestimo para
`DEVOLVIDO` e devolve a disponibilidade do equipamento na mesma transacao.

`listarDetalhado()` usa `INNER JOIN` e retorna objetos
`EmprestimoDetalhe` para exibicao, sem criar tabela adicional no banco.
`ATRASADO` e calculado a partir da data prevista, nao armazenado.

Para executar o programa, abra `br.com.aps.Aplicacao` pelo Eclipse,
depois de configurar o MySQL conforme o README.
