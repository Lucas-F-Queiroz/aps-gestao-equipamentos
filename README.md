# APS — Gestão de Empréstimos de Equipamentos de TI

Projeto acadêmico em **Java 17**, **Swing/FlatLaf**, **JDBC** e **MySQL**, com arquitetura **MVC + DAO**.

## Como executar pelo Eclipse

**Você precisa apenas de:** JDK 17, Eclipse com suporte Maven e MySQL instalado e funcionando. O Eclipse importa as dependências pelo `pom.xml`; não é necessário instalar ferramentas de infraestrutura adicionais.

1. No GitHub, selecione a branch `refactor/aps-mvc-enxuta` antes de clonar ou baixar o ZIP. **A `main` ainda é a versão antiga, sem interface.**
2. Inicie o serviço do MySQL no computador. Com DBeaver, MySQL Workbench ou outro cliente, execute **uma vez** o arquivo `database/create_database.sql` para criar o banco `aps_equipamentos`.
3. Em `src/main/resources/`, faça uma cópia de `application.properties.example` e nomeie a cópia como **`application.properties`**. Ajuste `db.user`, `db.password` e a porta de `db.url` conforme a sua instalação do MySQL (a porta padrão é **3306**). **Não envie esse arquivo com senhas para o GitHub.**
4. No Eclipse: **File → Import → Maven → Existing Maven Projects**. Selecione a pasta do projeto, onde está `pom.xml`. Se necessário, use **Maven → Update Project**.
5. Em `src/main/java`, abra **`br.com.aps.Aplicacao.java`** e execute **Run As → Java Application**.

Na primeira execução, o **Flyway** cria as tabelas automaticamente. Você só precisa criar o **banco vazio** no passo 2; não execute a migration manualmente. Nas próximas execuções, o banco e seus dados são preservados.

### Se você já tem um banco de uma versão anterior

A branch possui uma **única migration** `V1__estrutura_inicial.sql` com o esquema final. Ela **não é compatível** com um banco que já tenha recebido o V1/V2 antigo pelo Flyway. Use um **banco vazio separado**, alterando o nome em `db.url`, ou recrie apenas seu banco de testes, caso não tenha dados importantes. **Não apague bancos compartilhados nem bancos com dados que queira preservar.**

### Erros frequentes

- **Não existe `Aplicacao.java`**: você abriu a `main`, e não `refactor/aps-mvc-enxuta`.
- **Arquivo `application.properties` não encontrado**: copie o `.example` na mesma pasta e atualize o projeto no Eclipse.
- **Erro de conexão ou acesso negado**: verifique o serviço MySQL, porta `3306` e credenciais informadas.
- **Problemas de Java**: configure o projeto para **JDK 17** e atualize as dependências Maven.
- **Erro de validation/checksum do Flyway**: você está usando um banco já migrado com outra versão do esquema; use um banco vazio, sem executar `repair` para esconder a divergência.

## Arquitetura

```text
View (Swing) → Controller → DAO (JDBC/SQL) → MySQL
                   ↑            ↑
                   └── Model ───┘
```

- **Model**: as classes `Usuario`, `Equipamento` e `Emprestimo`.
- **View**: campos, tabelas, botões e mensagens, sem comandos SQL.
- **Controller**: validações e coordenação das operações.
- **DAO**: consultas SQL por JDBC, inserção, atualização, exclusão e transações.
- **MySQL**: tabelas e integridade dos dados. `Aplicacao.java` executa o Flyway antes de abrir a interface.

## Navegacao

A pagina inicial possui apenas tres acessos:

- **Cadastros:** registrar e editar pessoas ou equipamentos.
- **Emprestimos:** abas para emprestar e registrar devolucao.
- **Consultas:** abas para inventario, busca de emprestimos e relatorio CSV.

A tela inicial nao faz consultas periodicas ao banco. As abas apenas organizam
as telas Swing ja existentes, sem adicionar entidades, tabelas ou novas regras.

## Banco e regras principais

O banco possui **três tabelas**: `usuario`, `equipamento` e a tabela de relação `emprestimo`. Os vínculos são mantidos pelas chaves estrangeiras `emprestimo.usuario_id` e `emprestimo.equipamento_id`, conforme a orientação do professor.

- Uma pessoa precisa estar **ativa** para realizar um empréstimo.
- Um equipamento precisa estar **DISPONIVEL**; só pode ter **um empréstimo ABERTO** por vez.
- Empréstimo e devolução atualizam o equipamento **na mesma transação**.
- `ATRASADO` é calculado pela data; não é um status salvo no banco.
- O MySQL impede excluir usuários ou equipamentos com empréstimos históricos (`ON DELETE RESTRICT`).
- A interface permite cadastrar, consultar, filtrar, ordenar, alterar e excluir registros, respeitando essas regras.

## Testes e requisitos da APS

A [matriz de conformidade](docs/CONFORMIDADE_APS_2026_2.md) relaciona os requisitos da UNIP às funcionalidades do sistema e indica o que ainda precisa ser conferido no relatório acadêmico.

O repositório também possui verificações em `src/test/java`. Elas **não são necessárias para iniciar o programa**: servem para verificar o funcionamento dos DAOs, Controllers, ordenações e janelas Swing. Execute testes de integração somente em um banco de desenvolvimento.

## Equipe

- Lucas — Model, DAO, JDBC, MySQL e integração.
- Marianna — interface Swing e FlatLaf.
- Davi — Controllers e validações.
- Aline — documentação e apresentação.

A branch continua em revisão pelo PR #7. A `main` não foi modificada.
