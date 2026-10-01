# APS — Gestão de Empréstimos de Equipamentos

Projeto acadêmico da APS 2026/2 desenvolvido em Java.

## Tema

Sistema de gestão de empréstimos de equipamentos de TI.

## Tecnologias

- Java 17
- Maven
- Swing + FlatLaf
- JDBC
- MySQL
- Flyway
- Arquitetura MVC + DAO
- DBeaver como cliente gráfico recomendado para administrar o banco

## Arquitetura

```text
View -> Controller -> DAO -> MySQL
          ^
          |
        Model
```

Os objetos de `Model` representam as entidades do domínio e são utilizados pelas demais camadas.

## Entidades iniciais

- Usuario
- Equipamento
- Emprestimo

## Equipe

- Lucas — Model, DAO, JDBC, MySQL e integração
- Marianna — View, Swing e FlatLaf
- Davi — Controller, regras e validações
- Aline — documentação, DER, UML e apoio à apresentação

## Como rodar o projeto pela primeira vez

1. Instale Java 17, Maven e MySQL.
2. Use o DBeaver ou outro cliente MySQL para conectar ao servidor local.
3. Execute o arquivo:

```text
database/create_database.sql
```

4. Configure as credenciais do banco localmente, se necessário:

```text
APS_DB_URL
APS_DB_USER
APS_DB_PASSWORD
```

5. Execute a aplicação Java.
6. Na inicialização, o Flyway aplicará automaticamente as migrations pendentes.
7. Depois disso, o sistema acessará o banco normalmente por JDBC e DAO.

## Banco de dados

O projeto usa MySQL. O banco precisa existir antes da primeira execução do Flyway.

O script de bootstrap está em:

```text
database/create_database.sql
```

Depois disso, a estrutura das tabelas é versionada pelo Flyway em:

```text
src/main/resources/db/migration/
```

A migration inicial é:

```text
V1__estrutura_inicial.sql
```

Não edite uma migration que já tenha sido aplicada em um banco compartilhado. Mudanças futuras devem ser novas migrations, por exemplo:

```text
V2__adiciona_observacao_equipamento.sql
V3__cria_indice_exemplo.sql
```

## Configuração local

Credenciais não devem ser versionadas no Git.

A conexão JDBC e o Flyway leem estas variáveis de ambiente quando existirem:

```text
APS_DB_URL
APS_DB_USER
APS_DB_PASSWORD
```

Valores padrão usados no desenvolvimento local:

```text
URL: jdbc:mysql://localhost:3306/aps_equipamentos
Usuário: root
Senha: vazia
```

Se o seu MySQL usar outra senha, configure `APS_DB_PASSWORD` localmente em vez de colocar a senha no repositório.

## Flyway

A classe:

```text
br.com.aps.util.DatabaseMigrator
```

é responsável por executar as migrations. Quando a classe principal da aplicação for criada, ela deverá chamar:

```java
DatabaseMigrator.migrate();
```

antes de abrir a interface gráfica.

## Fluxo de desenvolvimento

Cada tarefa deve ser feita em uma branch própria e enviada por Pull Request antes do merge na `main`.
