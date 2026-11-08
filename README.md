# APS | Gestão de Empréstimos de Produtos e Equipamentos

Sistema desktop desenvolvido como Atividade Prática Supervisionada (APS) de Ciência da Computação. A interface TI Fácil usa Java Swing e FlatLaf. O sistema persiste os cadastros e as movimentações em MySQL via JDBC.

## Funcionalidades

- **Pessoas** — cadastro com nome, e-mail, área/profissão, matrícula, CPF e celular. Matrícula, CPF e celular são opcionais. O CPF é único quando informado.
- **Equipamentos** — cadastro por patrimônio, com categoria, estado, modelo, marca, número de série, sistema operacional e departamento.
- **Inventário** — consulta de disponibilidade, conservação, departamento, pessoa com empréstimo aberto e devolução prevista.
- **Empréstimos e devoluções** — registro de retirada e prazo. Na devolução, o operador informa o estado do equipamento e quem registrou a operação, com observação opcional.
- **Consultas e relatórios** — filtros por situação, histórico de devoluções e exportação CSV individual ou geral. Cadastros com histórico são preservados para manter os vínculos com os empréstimos.

## Como executar

Instale um JDK 17 ou superior, Maven e MySQL 8.4. O Eclipse é opcional.

1. Clone o repositório e entre na pasta do projeto.

   ```sh
   git clone https://github.com/Lucas-F-Queiroz/aps-gestao-equipamentos.git
   cd aps-gestao-equipamentos
   ```

2. Para uma instalação nova, crie o banco com [database/create_database.sql](database/create_database.sql). Se já usa o projeto, aponte a configuração para o banco existente.
3. Em `src/main/resources`, copie [application.properties.example](src/main/resources/application.properties.example) para `application.properties` e preencha `db.url`, `db.user` e `db.password`. Mantenha o arquivo com credenciais na instalação local.
4. No Eclipse, importe em **File > Import > Maven > Existing Maven Projects**. Execute **Maven > Update Project** e rode `br.com.aps.Aplicacao` como **Java Application**. A configuração `Executar_Interface` também está disponível.

Para executar pelo terminal, na raiz do projeto:

```sh
mvn -B -ntp -DskipTests package dependency:copy-dependencies -DoutputDirectory=target/dependency
java -cp "target/classes;target/dependency/*" br.com.aps.Aplicacao
```

No Linux e no macOS, use `:` no lugar de `;` no classpath.

## Banco de dados

O Flyway aplica as migrações pendentes na inicialização. A versão integrada identifica qual V3 já foi aplicada, tanto no banco da antiga `main` quanto no da V6. Os dois caminhos chegam ao mesmo esquema e preservam os cadastros existentes.

A devolução guarda o estado, a observação e o nome informado pelo operador. O CSV é gerado sob demanda a partir dos dados no MySQL.

## Validação

`mvn clean verify` compila o projeto. O GitHub Actions executa também as verificações dos DAOs, dos Controllers e das telas em MySQL 8.4. A integração da V6 foi testada com um banco vazio e com bancos isolados iniciados em cada versão da migração V3.

## Equipe

- **Lucas Figueiredo Queiroz** — arquitetura, modelagem do banco, persistência, integração e revisão técnica.
- **Davi Pacheco da Costa** — backend, Controllers e integração com a interface.
- **Marianna Brazão Silva** — desenvolvimento da interface gráfica.
- **Aline Becker Costa** — trabalho escrito, diagramas e revisão da APS.
