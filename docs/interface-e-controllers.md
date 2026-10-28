> Referência da versão anterior. Para a configuração e o esquema atuais (V1–V4), consulte [README](../README.md). Pessoas usam área/profissão e matrícula; equipamentos incluem modelo, marca e série.

# Interface e Controllers

Responsável: Davi (Controller, regras e validações). Consome os DAOs do Lucas e é consumido pela interface (View).

## Montagem

```text
Aplicacao.main
  1. Estilo.instalar()            escolhe o visual FlatLaf
  2. DatabaseMigrator.migrate()   aplica as migrations (V1, V2, V3)
  3. new JanelaPrincipal()        abre a janela em tela cheia
```

`JanelaPrincipal` troca o conteúdo do centro conforme o botão escolhido. Cada tela chama os Controllers por
`Aplicacao.usuarios()`, `Aplicacao.equipamentos()` e `Aplicacao.emprestimos()`; a View não faz SQL.

| Tela (classe) | Controller usado |
| --- | --- |
| `TelaInicio` (histórico à direita) | `EmprestimoController.historico()` |
| `TelaEquipamentos` | `EquipamentoController` (`listar`, `alterarStatus`) |
| `TelaCadastros` (`CadastroPessoas`, `CadastroEquipamentos`) | `UsuarioController`, `EquipamentoController` |
| `TelaConsultar` | `EmprestimoController.abertos()`, `UsuarioController.listar()` |
| `TelaDevolucao` | `EmprestimoController.devolver(...)` e `devolucoes()` |
| `TelaRelatorios` | `EmprestimoController.devolucoes()` |
| `TelaEmprestimos` | `EmprestimoController.emprestar(...)` |

O histórico da página inicial é lido do banco a cada 2 segundos por um `SwingWorker` (a tela não trava se o MySQL
demorar) e só é redesenhado quando algo mudou; assim também mostra o que outros computadores fizeram.

## Onde cada regra do Lucas é aplicada

| Regra / limite | Controller | Banco ou DAO (rede de proteção) |
| --- | --- | --- |
| 1. Pessoa ativa para emprestar | `EmprestimoController.emprestar` | `EmprestimoDAO.inserir` (`SELECT ... FOR UPDATE`) |
| 2. Equipamento `DISPONIVEL` | `EmprestimoController.emprestar` | `UPDATE` condicional do `EmprestimoDAO` |
| 3. Um empréstimo aberto por equipamento | `EmprestimoController.emprestar` | índice `UNIQUE` da V2 |
| 4. Emprestar: `EMPRESTADO` e `ABERTO` | `EmprestimoDAO.inserir` (transação) | CHECKs da V1 |
| 5. Devolver: `DEVOLVIDO`, data e `DISPONIVEL` | `EmprestimoDAO.registrarDevolucao` (transação) | CHECKs da V2 |
| 6. Histórico impede exclusão | `UsuarioController.excluir`, `EquipamentoController.excluir` | `ON DELETE RESTRICT` |
| Valores permitidos (tipo, estado, status) | `Regras` | CHECKs da V1 e V3 |
| Tamanhos (nome 100, e-mail 120, patrimônio 50, categoria 50, observação 255) | `Regras` | colunas `VARCHAR` |
| E-mail e patrimônio únicos (sem diferenciar maiúsculas) | `UsuarioController`, `EquipamentoController` | `UNIQUE` |
| Data prevista não anterior à retirada | `EmprestimoController.emprestar` | `CHECK` da V2 |
| `EMPRESTADO` não muda à mão | `EquipamentoController.alterarStatus` | — |
| `ATRASADO` é calculado | `Regras.situacao` / `Emprestimo.isAtrasado()` | não é gravado |

Erros do banco (`SQLException`) são traduzidos para mensagens em português por `TradutorErros` e chegam à tela como
`RegraNegocioException`.

## O que o README não define (decisões da interface)

- Não há limite de equipamentos por pessoa nem prazos fixos: a devolução prevista é uma data digitada, que vale até o
  fim do dia e não pode ser anterior a hoje.
- Inativar uma pessoa é permitido; ela ainda pode devolver, mas não faz novos empréstimos (a tela avisa).
- Equipamento devolvido `DANIFICADO`: a regra 5 o mantém `DISPONIVEL`; a tela pergunta se deve ficar `INDISPONIVEL`.
- O estado na devolução exige a tabela `devolucao` (migration V3, aditiva).
