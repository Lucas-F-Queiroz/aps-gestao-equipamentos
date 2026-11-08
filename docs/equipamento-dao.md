> Referência da versão anterior. Para a configuração e o esquema atuais (V1–V4), consulte [README](../README.md). Pessoas usam área/profissão e matrícula; equipamentos incluem modelo, marca e série.

# Contrato do EquipamentoDAO

Responsável técnico: Lucas. Consumidor principal: Controller (Davi).

A camada DAO manipula SQL e devolve objetos `Equipamento`. Validação de entradas da tela
e regras de empréstimo pertencem ao Controller, não ao DAO.

## Métodos

| Método | Retorno | Uso |
|---|---|---|
| `inserir(Equipamento)` | `void` | Cadastra equipamento e atualiza o objeto com o ID do banco |
| `listar()` | `List<Equipamento>` | Ordena por nome, patrimônio e ID |
| `listarOrdenadoPorPatrimonio()` | `List<Equipamento>` | Ordenação por patrimônio |
| `listarOrdenadoPorCategoria()` | `List<Equipamento>` | Ordenação por categoria, depois nome |
| `buscarPorId(int)` | `Equipamento` | Retorna `null` se não encontrar |
| `buscarPorPatrimonio(String)` | `Equipamento` | Busca exata pelo patrimônio único, `null` se não encontrar |
| `buscarPorNome(String)` | `List<Equipamento>` | Busca parcial de nome |
| `listarPorStatus(String)` | `List<Equipamento>` | Filtra `DISPONIVEL`, `EMPRESTADO`, `INDISPONIVEL` |
| `atualizar(Equipamento)` | `boolean` | Retorna `false` se ID não existir |
| `excluir(int)` | `boolean` | Retorna `false` se ID não existir |

## Status e estado são coisas diferentes

- **Estado** descreve a condição física: `NOVO`, `BOM`, `REGULAR`, `DANIFICADO`.
- **Status** descreve a disponibilidade: `DISPONIVEL`, `EMPRESTADO`, `INDISPONIVEL`.

O campo `patrimonio` é único na tabela. Tentar cadastrar um patrimônio repetido
gera erro de banco.

Todos os métodos podem lançar `SQLException`; as mensagens de erro devem
ser adaptadas pelo Controller para a interface.

O MySQL protege o histórico: não é possível excluir equipamento referenciado por
empréstimo porque a FK usa `ON DELETE RESTRICT`.

O Controller deve impedir alterações manuais incoerentes de status durante empréstimos.
As operações de emprestar/devolver serão implementadas em uma etapa posterior com transação,
para atualizar `equipamento` e `emprestimo` de forma atômica.

## Teste manual no computador

1. Crie o banco pelo arquivo `database/create_database.sql`.
2. Configure as variáveis `APS_DB_URL`, `APS_DB_USER` e `APS_DB_PASSWORD`.
3. Execute `EquipamentoDAOManualCheck.main()` pela IDE:
   `src/test/java/br/com/aps/dao/EquipamentoDAOManualCheck.java`.

Ele aplica as migrations pendentes via Flyway e verifica INSERT, SELECT, pesquisas,
filtros, ordenações, UPDATE e DELETE usando um patrimônio temporário.
Essa verificação não roda automaticamente no Maven.
