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
- Arquitetura MVC + DAO

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

## Banco de dados

O script inicial está em:

```text
database/schema.sql
```

Credenciais do banco não devem ser versionadas. A conexão JDBC pode usar as variáveis de ambiente:

```text
APS_DB_URL
APS_DB_USER
APS_DB_PASSWORD
```

## Fluxo de desenvolvimento

Cada tarefa deve ser feita em uma branch própria e enviada por Pull Request antes do merge na `main`.
