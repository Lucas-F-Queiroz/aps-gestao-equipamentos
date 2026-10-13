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

## Valores do domínio

```text
Usuario.tipo
- ALUNO
- PROFESSOR
- FUNCIONARIO

Equipamento.estado
- NOVO
- BOM
- REGULAR
- DANIFICADO

Equipamento.status
- DISPONIVEL
- EMPRESTADO
- INDISPONIVEL

Emprestimo.status
- ABERTO
- DEVOLVIDO
```

`ATRASADO` não é armazenado como status. Um empréstimo é considerado atrasado quando permanece `ABERTO` e a data atual ultrapassa `data_prevista`.

## Regras de negócio iniciais

1. O usuário precisa estar ativo para realizar um empréstimo.
2. O equipamento precisa estar com status `DISPONIVEL`.
3. Um equipamento não pode possuir dois empréstimos abertos ao mesmo tempo.
4. Ao realizar um empréstimo, o equipamento passa para `EMPRESTADO` e o empréstimo fica `ABERTO`.
5. Ao devolver, o empréstimo passa para `DEVOLVIDO`, `data_devolucao` é preenchida e o equipamento volta para `DISPONIVEL`.
6. Usuários e equipamentos com histórico de empréstimos não devem ser excluídos fisicamente; as chaves estrangeiras usam `ON DELETE RESTRICT`.

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

4. Copie o modelo e configure as credenciais do seu MySQL local:

```text
src/main/resources/application.properties.example
    -> src/main/resources/application.properties
```

5. No Eclipse, importe o projeto por **File > Import > Maven > Existing Maven Projects**.
6. Atualize o projeto com **F5** e execute um dos testes manuais listados abaixo por **Run As > Java Application**.
7. O teste aplicará as migrations pendentes pelo Flyway antes de acessar os DAOs.

A versão atual ainda não possui interface gráfica nem classe principal da aplicação.

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

A conexão JDBC e o Flyway leem o mesmo arquivo `src/main/resources/application.properties`,
carregado pelo classpath. Esse arquivo é ignorado pelo Git; apenas o modelo
`application.properties.example` deve ser versionado.

```text
db.url=jdbc:mysql://127.0.0.1:3306/aps_equipamentos?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=America/Sao_Paulo
db.user=aps_local
db.password=SUA_SENHA_LOCAL
```

Preencha o usuário e a senha da sua instalação local. As três propriedades são
obrigatórias; `db.password=` é permitido se o banco usar senha vazia.
As variáveis `APS_DB_*` não são utilizadas.

No Eclipse, atualize o projeto com **F5** após criar ou editar o arquivo.
Não compartilhe o `application.properties` nem JARs que contenham suas credenciais.

## Testes locais

Para conferir a compilação, execute na raiz do projeto:

```text
mvn verify
```

Os testes atuais são programas Java manuais; esse comando não executa as verificações
de configuração nem acessa o MySQL. No Eclipse, execute o método `main` destas classes:

| Classe em `src/test/java` | Requisito | Resultado esperado |
| --- | --- | --- |
| `br.com.aps.util.DatabaseConfigManualCheck` | Java 17, sem banco | Configuração validada |
| `br.com.aps.dao.UsuarioDAOManualTest` | MySQL e `application.properties` | Todas as verificações passaram |
| `br.com.aps.dao.EquipamentoDAOManualCheck` | MySQL e `application.properties` | Verificações concluídas com sucesso |
| `br.com.aps.dao.EmprestimoDAOManualCheck` | MySQL e `application.properties` | Verificações concluídas com sucesso |

Os testes DAO criam e removem seus registros temporários. Execute-os em um banco de
desenvolvimento. No GitHub Actions, uma instância MySQL isolada e um arquivo de
configuração temporário permitem executar os três DAOs sem utilizar credenciais pessoais.

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


## UsuarioDAO

Arquivo: `src/main/java/br/com/aps/dao/UsuarioDAO.java`.

| Método | Retorno | Comportamento |
| --- | --- | --- |
| `inserir(Usuario)` | `void` | Insere e preenche `usuario.id` com o ID gerado |
| `listar()` | `List<Usuario>` | Lista em ordem de nome, depois ID |
| `buscarPorId(int)` | `Usuario` | Retorna o registro ou `null` se não existir |
| `buscarPorNome(String)` | `List<Usuario>` | Pesquisa parcial pelo nome |
| `atualizar(Usuario)` | `boolean` | `false` se não encontrou o ID |
| `excluir(int)` | `boolean` | `false` se não encontrou o ID |

Todos os métodos podem lançar `SQLException`. A exclusão física de usuário
com histórico de empréstimos deve ser bloqueada pelas chaves estrangeiras do MySQL.
O Controller deverá traduzir esse erro para uma mensagem compreensível.

### Teste manual com MySQL

Depois de criar o banco e configurar a conexão, execute o método `main` da classe:

```text
src/test/java/br/com/aps/dao/UsuarioDAOManualTest.java
```

Ela aplica migrations, insere um usuário temporário, valida pesquisas e listagem,
atualiza, exclui e confere o resultado. Exige MySQL local disponível e não é
executada automaticamente pelo build Maven.
