# Matriz de conformidade — APS 2026/2

**Fonte:** `APS 2026_2_CC_SI_4.pdf`, documento oficial da UNIP,
"Desenvolvimento de Sistema Com Interface Gráfica Para Manipulação de Banco de Dados",
3º e 4º semestre, 2026/2, páginas 3 a 11.

**Versão:** interface do RAR integrada com CPF/celular e migrations V1/V2 preservadas, seguidas da V3.
Este documento distingue requisitos do **software** daqueles do **trabalho acadêmico**.
Nenhum teste de código comprova a entrega do relatório/slide.

| Requisito oficial | Implementação / evidência | Situação |
| --- | --- | --- |
| Java com POO | `model/Usuario`, `Equipamento`, `Emprestimo` | Implementado |
| MySQL relacional | `V1__estrutura_inicial.sql` e JDBC | Implementado e testado em MySQL 8.4 |
| Pelo menos 2 tabelas entidade + tabela associativa | `usuario`, `equipamento`, `emprestimo` | Implementado |
| Classes Java equivalentes às entidades | 3 classes em `model` | Implementado |
| Relacionamento e integridade PK/FK | `emprestimo.usuario_id`, `equipamento_id`, FKs e `ON DELETE RESTRICT` | Implementado |
| Listar registros | Cadastros e abas de Empréstimos/Consultas | Implementado |
| Ordenar por mais de um critério | Inventário: nome/patrimônio, patrimônio/ID, categoria/nome; `EquipamentoDAO` | Implementado; teste `APSOrdenacaoCheck` |
| Filtrar/buscar | Inventário e Consultar, com campo de busca e filtro de situação | Implementado |
| Adicionar registros pela interface | Cadastro de pessoas, equipamentos e empréstimos | Implementado |
| Modificar registros pela interface | Cadastro de pessoas e equipamentos | Implementado |
| CPF/celular (pedido do grupo, não tratado como requisito oficial) | Campos opcionais no cadastro, edição e relatório; validação e CPF único | Testes `APSPessoaValidacaoCheck`, `APSPessoasCheck`, `APSContatoViewCheck` |
| Excluir registros preservando FKs | Cadastros; banco restringe exclusão com histórico | Implementado |
| Arquitetura MVC e DAO | `view -> controller -> dao -> MySQL`, Models separados | Implementado |
| Validação e tratamento de erros | `Regras`, `RegraNegocioException`, `TradutorErros`, restrições SQL | Implementado |
| Interface gráfica intuitiva | Swing/FlatLaf; smoke test cria as telas | Implementado; **usabilidade visual manual ainda precisa ser confirmada** |
| Pesquisa, planejamento, prototipagem e testes | README, arquitetura, migration, CI | **Evidência de prototipagem não localizada no código** |
| Relatório escrito conforme Guia UNIP/ABNT | Exige capa, resumo, sumário automático, introdução, revisão teórica, metodologia, desenvolvimento, conclusões, referências e fichas | **Não verificado aqui: material escrito pode estar fora do repositório** |
| Diagramas/modelagens, resultados, análise crítica, código completo no trabalho | Obrigatórios na seção Desenvolvimento do relatório | **Não verificado no repositório** |
| Apresentação oral com slides, participação individual | Exigência do documento oficial | **Não verificado no repositório** |
| Entrega no sistema acadêmico | Procedimento externo do grupo | **Não verificado** |

## Decisões de escopo preservadas

- Não adicionar campos de matrícula, área/profissão livre, marca, modelo, série,
  sistema operacional ou departamento.
- Não criar tabela `devolucao` apenas para relatório;
  datas e status de empréstimo ficam em `emprestimo`.
- `ATRASADO` é calculado, não gravado na tabela.
- Permitir vários empréstimos por pessoa, mas apenas um empréstimo aberto
  por equipamento.
- Manter JDBC e transações no DAO, não no Controller ou na View.
- Preservar as migrations V1/V2 da main e acrescentar V3; atualizar bancos dessa
  linha sem recriar cadastros. Bancos dos protótipos com V1 reescrita exigem migração específica.

## Revisões técnicas e testes

Os DAOs usam `PreparedStatement`, `try-with-resources`, SQL com placeholders
e transações `commit/rollback` para empréstimos e devoluções.
A coluna virtual UNIQUE impede dois empréstimos abertos por equipamento.

`EquipamentoDAO.atualizar` não altera o status operacional durante edição.
A alteração manual de disponibilidade é um UPDATE condicional.

O CI executa compilação Java 17, MySQL 8.4, verificações DAO,
`APSFluxoCheck`, `APSOrdenacaoCheck`, smoke test da interface e
execução com configuração JDBC temporária em MySQL isolado no GitHub Actions.

## Pontos que exigem confirmação humana

**Relacionamentos confirmados pelo professor:** são exigidos relacionamentos
no banco de dados; não há exigência de referências diretas a objetos Java.
As chaves estrangeiras de `emprestimo` atendem a esse requisito.

1. **Trabalho escrito:** conferir DER, UML, protótipos/prints, metodologia,
   análise crítica, relatório ABNT, referências, fichas e slides.
2. **Teste manual:** verificar navegação, diálogos e exportação CSV no Windows;
   o smoke test automatizado não avalia aparência nem facilidade de uso.

**Estado do projeto:** requisitos verificáveis de implementação atendidos;
**entrega acadêmica integral não certificada** enquanto os itens externos
ao código não forem apresentados.
