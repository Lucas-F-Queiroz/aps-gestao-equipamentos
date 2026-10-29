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
| `alterarStatusDisponibilidade(int, String)` | `boolean` | Troca DISPONIVEL/INDISPONIVEL somente sem empréstimo aberto |
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

`atualizar` altera somente patrimônio, nome, categoria e estado: preserva o status
operacional atual. A mudança manual de disponibilidade usa um UPDATE condicional,
que não altera equipamentos emprestados nem aqueles com empréstimo aberto.
Emprestar e devolver usam as transações do `EmprestimoDAO` para atualizar equipamento
e empréstimo de forma atômica.

Mesmo após a devolução, o histórico impede exclusão física. Para retirar de circulação,
use INDISPONIVEL; equipamentos nunca emprestados podem ser excluídos.

## Teste manual no computador

1. Crie o banco pelo arquivo `database/create_database.sql`.
2. Configure `src/main/resources/application.properties` a partir do `.example`.
3. Execute `EquipamentoDAOManualCheck.main()` pela IDE:
   `src/test/java/br/com/aps/dao/EquipamentoDAOManualCheck.java`.

Ele aplica as migrations pendentes via Flyway e verifica INSERT, SELECT, pesquisas,
filtros, ordenações, UPDATE e DELETE usando um patrimônio temporário.
Essa verificação não roda automaticamente no Maven.
