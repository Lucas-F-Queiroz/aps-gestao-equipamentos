# APS — Gestão de Empréstimos de Equipamentos de TI

Sistema desktop em Java 17, Swing + FlatLaf, JDBC e MySQL 8.4, com arquitetura **MVC + DAO**.

## Executar após baixar do GitHub

**Requisitos no computador:** Java 17, Maven e Docker Desktop/Engine em funcionamento. Não é necessário criar tabelas, fornecer senhas pessoais ou configurar caminhos absolutos.

- Windows: execute `iniciar.bat` na pasta do repositório.
- Linux/macOS: no terminal da pasta, execute `sh iniciar.sh`.

Os scripts iniciam o MySQL local em container, aguardam o banco ficar pronto e executam a aplicação via Maven. O Flyway cria automaticamente as tabelas na primeira inicialização.

O banco de desenvolvimento usa **127.0.0.1:3307** para evitar conflito com um MySQL instalado na porta padrão. As credenciais do `compose.yaml` servem **apenas para ambiente local**, nunca para um servidor público. Os dados persistem em um volume Docker.

Para usar um MySQL próprio sem Docker, crie `src/main/resources/application.properties` com base no arquivo `.example`, crie o banco usando `database/create_database.sql` e rode `mvn compile exec:java`. Alternativamente, configure `APS_DB_URL`, `APS_DB_USER` e `APS_DB_PASSWORD` como variáveis de ambiente. Prioridade de configuração: **ambiente → arquivo opcional → padrão do Docker**.

> **Banco novo:** esta branch consolida o esquema em somente `V1__estrutura_inicial.sql`. Não se aplica sobre um banco antigo com Flyway V1/V2: recrie **um banco de desenvolvimento descartável**. Para apagar apenas o banco local deste projeto: `docker compose down -v` (comando destrutivo).

## Arquitetura

```text
Swing View -> Controller (validacao) -> DAO (JDBC) -> MySQL
                   |                       |
                   +---- Model ------------+
```

O `Aplicacao.main` prepara o banco via `DatabaseMigrator` antes de iniciar o Swing. A View não executa SQL. Os Controllers coordenam operações e apresentam mensagens de erro; os DAOs possuem o SQL e as transações. O `ConnectionFactory` abre conexões configuradas por `DatabaseConfig`.

## Modelo de dados definitivo

- `usuario`: id, nome, email único, tipo (`ALUNO`, `PROFESSOR`, `FUNCIONARIO`), ativo.
- `equipamento`: id, patrimônio único, nome, categoria, estado de conservação, status.
- `emprestimo`: id, usuário, equipamento, retirada, prazo, devolução, status.
- `flyway_schema_history`: tabela técnica criada pelo Flyway, não entidade do sistema.

Um usuário pode ter vários empréstimos, um equipamento pode ter vários empréstimos históricos, mas **apenas um empréstimo ABERTO por vez**. A coluna virtual `equipamento_aberto_id` e a restrição `UNIQUE` fazem essa garantia no MySQL.

`ATRASADO` é calculado a partir do prazo, não salvo como status. Os status persistidos do empréstimo são `ABERTO` e `DEVOLVIDO`.

### Regras

1. Um usuário deve existir e estar ativo para tomar emprestado.
2. O equipamento deve estar `DISPONIVEL` e não ter empréstimo aberto.
3. O empréstimo grava `ABERTO` e altera o equipamento para `EMPRESTADO` **na mesma transação**.
4. A devolução grava `DEVOLVIDO`, data de devolução e equipamento `DISPONIVEL` **na mesma transação**.
5. `ON DELETE RESTRICT` preserva histórico de empréstimos.
6. Editar nome, patrimônio, categoria ou estado do equipamento **não altera seu status**; a mudança manual de disponibilidade é protegida por um UPDATE condicional.

## Interface reaproveitada e simplificada

A navegação e os componentes visuais foram reaproveitados da branch `feature/aps-v6-final`. Mantivemos cadastro de pessoas e equipamentos, inventário, consulta, empréstimo, devolução e relatório CSV, adaptados ao modelo original.

**Fora de escopo:** matrícula, área/profissão livre, marca, modelo, série, departamento, SO e quarta tabela de devolução. O relatório é baseado em `emprestimo` e não registra observações de devolução.

## Conformidade com o enunciado

A [matriz de conformidade](docs/CONFORMIDADE_APS_2026_2.md) relaciona cada requisito oficial
com as classes e funcionalidades implementadas. A documentação acadêmica e a
apresentação oral precisam ser verificadas separadamente do build Java.

## Testar

- `mvn -B -ntp verify` valida compilação, **não** substitui o teste com banco.
- O GitHub Actions inicia um MySQL 8.4 **isolado** e executa as verificações dos três DAOs e do fluxo MVC.
- Tests de integração manual: `UsuarioDAOManualTest`, `EquipamentoDAOManualCheck`, `EmprestimoDAOManualCheck`, `APSFluxoCheck` e `APSOrdenacaoCheck`.
- Um smoke test do Swing verifica a criação das telas em display virtual no CI.

Não rode testes de integração contra bancos de produção ou compartilhados: eles criam e apagam dados temporários.

## Equipe

Lucas — Model, DAO, JDBC, MySQL e integração. Marianna — Swing e FlatLaf. Davi — Controller e validações. Aline — documentação e apresentação.

## Revisão

Esta é uma branch de integração para avaliação antes de qualquer merge na `main`. Alterações futuras de schema devem ocorrer por novas migrations após a definição da versão entregue.
