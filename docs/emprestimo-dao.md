# Contrato do EmprestimoDAO

Responsável: Lucas (DAO / JDBC / transações). Consumidor: Davi (Controller).

## Operações

| Método | Retorno | Comportamento |
| --- | --- | --- |
| `inserir(Emprestimo)` | `void` | Cria empréstimo ABERTO e muda equipamento para EMPRESTADO em uma transação. Define ID no objeto ao confirmar. |
| `registrarDevolucao(int, LocalDateTime)` | `boolean` | Finaliza empréstimo e libera equipamento na mesma transação. Retorna `false` se ID não existir ou já estiver devolvido. |
| `registrarDevolucao(int)` | `boolean` | Igual ao anterior, usando `LocalDateTime.now()`. |
| `buscarPorId(int)` | `Emprestimo` | Retorna `null` se não encontrar. |
| `listar()` | `List<Emprestimo>` | Todo o histórico, mais recentes primeiro. |
| `listarAbertos()` | `List<Emprestimo>` | Apenas ABERTO, vencimento mais próximo primeiro. |
| `listarAtrasados()` | `List<Emprestimo>` | ABERTO com data prevista menor que `NOW()` do MySQL. |
| `listarPorUsuario(int)` | `List<Emprestimo>` | Histórico de um usuário. |
| `listarPorEquipamento(int)` | `List<Emprestimo>` | Histórico de um equipamento. |
| `listarDetalhado()` | `List<EmprestimoDetalhe>` | JOIN com nomes do usuário/equipamento e patrimônio. |

O objeto `EmprestimoDetalhe` é um **DTO para exibição**, não uma quarta entidade/tabela.

## Regras e integridade

- Usuário precisa existir e estar ativo.
- Equipamento precisa estar `DISPONIVEL`.
- Não é possível abrir dois empréstimos simultâneos do mesmo equipamento.
- `data_prevista` precisa ser posterior a `data_retirada`.
- Ao emprestar: empréstimo `ABERTO`, equipamento `EMPRESTADO`.
- Ao devolver: empréstimo `DEVOLVIDO`, data da devolução preenchida e equipamento `DISPONIVEL`.
- Devolução não pode ter data anterior à retirada.
- `ATRASADO` é calculado pela data e não é armazenado no campo `status`.
- A violação de regras de integridade que exige desfazer a operação gera `SQLException`. Argumentos de data inválidos geram `IllegalArgumentException`. O Controller deve apresentar mensagens adequadas à interface.
- **Não alterar manualmente o status para DISPONIVEL/EMPRESTADO pela tela de edição do equipamento** enquanto houver empréstimo ativo. As transações do `EmprestimoDAO` são a operação oficial para mudar esse status.

## Transações

Ambas as operações de escrita chamam `setAutoCommit(false)` e, em seguida:

- `commit()` quando o empréstimo e o equipamento foram atualizados corretamente;
- `rollback()` quando alguma etapa falha.

O registro do empréstimo usa `SELECT ... FOR UPDATE` no usuário e `UPDATE` condicional no equipamento. Duas requisições concorrentes não conseguem marcar o mesmo equipamento como disponível e emprestá-lo simultaneamente através deste DAO.

A migration `V2__integridade_emprestimos.sql` protege também o banco contra duas linhas `ABERTO` para o mesmo equipamento, mesmo fora do Java. Ela usa uma coluna gerada e um índice `UNIQUE`, além de constraints de datas e consistência da devolução. Como a `V1` é imutável depois de aplicada, a regra foi adicionada como `V2`.

## Testes de integração

`src/test/java/br/com/aps/dao/EmprestimoDAOManualCheck.java` executa um teste com dados temporários (sem depender dos DAOs de usuário e equipamento, ainda em outros PRs):

1. Cria usuário e equipamento temporários.
2. Registra empréstimo.
3. Confere status e bloqueio de duplicidade.
4. Verifica histórico, JOIN e atraso.
5. Força falha de devolução e verifica rollback.
6. Devolve corretamente e bloqueia devolução repetida.
7. Desativa o usuário e verifica bloqueio de novo empréstimo.
8. Remove os registros temporários.

O GitHub Actions executa esse teste em um MySQL 8.4 temporário. Para executar no computador, crie o banco com `database/create_database.sql`, configure `APS_DB_URL`, `APS_DB_USER` e `APS_DB_PASSWORD`, e execute o `main` da classe.

## Para a View/Controller

A View não faz SQL. O Controller verifica os campos antes de chamar o DAO e trata erros. A classe `EmprestimoDAO` protege a integridade do banco, mas não substitui o Controller.

O cadastro/edição de equipamento NÃO deve alterar livremente o status `EMPRESTADO`: somente empréstimo e devolução devem realizar essas transições.
