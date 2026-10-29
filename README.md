# APS — Gestão de Empréstimos de Equipamentos de TI

Projeto acadêmico em **Java 17**, **Swing/FlatLaf**, **JDBC** e **MySQL**, com arquitetura **MVC + DAO**.

## Como executar pelo Eclipse

**Você precisa apenas de:** JDK 17, Eclipse com suporte Maven e MySQL instalado e funcionando. O Eclipse importa as dependências pelo `pom.xml`; não é necessário instalar ferramentas de infraestrutura adicionais.

1. Clone ou baixe esta versão do projeto. Durante a revisão, use a branch da PR; após o merge, use a `main`.
2. Inicie o serviço do MySQL no computador. Com DBeaver, MySQL Workbench ou outro cliente, execute **uma vez** o arquivo `database/create_database.sql` para criar o banco `aps_equipamentos`.
3. Em `src/main/resources/`, faça uma cópia de `application.properties.example` e nomeie a cópia como **`application.properties`**. Ajuste `db.user`, `db.password` e a porta de `db.url` conforme a sua instalação do MySQL (a porta padrão é **3306**). **Não envie esse arquivo com senhas para o GitHub.**
4. No Eclipse: **File → Import → Maven → Existing Maven Projects**. Selecione a pasta do projeto, onde está `pom.xml`. Se necessário, use **Maven → Update Project**.
5. Em `src/main/java`, abra **`br.com.aps.Aplicacao.java`** e execute **Run As → Java Application**.

Na primeira execução, o **Flyway** cria as tabelas automaticamente. Você só precisa criar o **banco vazio** no passo 2; não execute a migration manualmente. Nas próximas execuções, o banco e seus dados são preservados.

### Se você já tem um banco de uma versão anterior

As migrations **V1 e V2 originais da main foram preservadas**. O Flyway aplica a
**V3** para acrescentar CPF e celular sem apagar cadastros nem histórico. Pessoas
anteriores continuam com os dois campos vazios, até que sejam preenchidos pela edição.

Se seu banco veio de um protótipo/arquivo compactado que reescreveu a V1 e removeu
a V2, ele tem outra linha de migrations. Não execute `repair` nem apague dados para
esconder o conflito. Use um banco separado para testar e planeje a migração dos dados
desse protótipo antes de apontar a aplicação para ele.

### Erros frequentes

- **Não existe `Aplicacao.java`**: confira se abriu a versão que contém a interface e atualize os arquivos do projeto.
- **Arquivo `application.properties` não encontrado**: copie o `.example` na mesma pasta e atualize o projeto no Eclipse.
- **Erro de conexão ou acesso negado**: verifique o serviço MySQL, porta `3306` e credenciais informadas.
- **Problemas de Java**: configure o projeto para **JDK 17** e atualize as dependências Maven.
- **Erro de validation/checksum do Flyway**: o banco veio de outra linha de migrations; preserve seus dados e confira a seção anterior. Bancos com V1/V2 originais recebem a V3 normalmente.

### CPF e celular de pessoas

Os dois campos estão disponíveis no formulário, na edição, na visualização e no
relatório de cadastros/exportação CSV. Também podem ser encontrados pela busca da tabela.

- **CPF opcional:** aceita com ou sem máscara, valida os dígitos verificadores e
  rejeita sequências repetidas. Quando preenchido, deve ser único entre pessoas.
- **Celular opcional:** número brasileiro com DDD e nove dígitos iniciando em 9,
  por exemplo `(11) 91234-5678`. Pode ser compartilhado por pessoas diferentes.
- São armazenados como texto, somente com dígitos, preservando zeros iniciais.
  Campo vazio vira `NULL`; não há dados pessoais inventados para cadastros antigos.
- Os dados são preservados ao alterar a situação Ativo/Inativo. Para removê-los,
  limpe os campos pela edição e salve.
- Use apenas dados fictícios em testes e não publique arquivos exportados com dados reais.

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

A pagina inicial tem a caixa de boas-vindas, tres atalhos (Cadastros, Emprestimos e Consultas) e o
**Historico ao vivo**: os emprestimos mais recentes, relidos do banco a cada 5 segundos (Timer + SwingWorker, para
a tela nao travar). O botao "Ver todos" abre Consultas > Situacao dos emprestimos.

- **Cadastros:** abas para equipamentos e pessoas. Cada linha tem o menu **⋮** (coluna Acoes):
  - Equipamentos: Alterar status, Alterar estado, Editar dados, Ver historico e Excluir. O status
    (Disponivel ou Indisponivel) muda na janela "Alterar equipamento"; Emprestado so muda pela devolucao.
  - Pessoas: Visualizar, Editar, Alterar situacao e Excluir. "Funcao" e o tipo da pessoa (Aluno, Professor ou Funcionario).
  - O botao "Alterar situacao", ao lado de "Limpar filtros", abre a mesma janela para a linha selecionada.
- **Emprestimos:** abas para emprestar e registrar devolucao.
- **Consultas:** abas para inventario, situacao dos emprestimos e relatorios.
  - Relatorios: escolha o tipo (Cadastros, Emprestimos, Equipamentos ou Geral), use a busca e os filtros e exporte em
    CSV exatamente o que esta na tela. O tipo Geral e a consulta geral em numeros.

As janelas (`Modal`) e o menu das linhas (`MenuAcoes`) sao classes pequenas e reutilizaveis. Nada disso adiciona
entidades, tabelas ou regras: as telas continuam chamando os Controllers de sempre.

### Busca, filtros e ordenacao das tabelas

Todas as tabelas usam a mesma barra (`FiltroTabela`) e o mesmo ordenador (`ComparadorCelulas`):

- **Pesquisar** procura o texto em todas as colunas, sem diferenciar maiusculas e acentos; com varias palavras,
  todas precisam aparecer na linha.
- **Filtros por lista** (categoria, estado, status, pessoa, modelo, situacao...) e **periodo** (data inicial e final
  do emprestimo) quando a tabela tem datas. "Limpar filtros" volta ao normal.
- **Ordenar:** clicar no cabecalho da coluna ordena (a setinha indica a direcao) e clicar de novo inverte.
  Numeros, datas e textos sao comparados cada um do seu jeito. No inventario ha tambem "Ordenar por" e "Ordem".
- Filtrar e ordenar nao alteram dados. As telas convertem a linha clicada para a posicao real na lista
  (`Estilo.linhaSelecionada`), por isso editar ou devolver sempre atinge o registro que o usuario escolheu.

## Banco e regras principais

O banco possui **três tabelas**: `usuario`, `equipamento` e a tabela de relação `emprestimo`. Os vínculos são mantidos pelas chaves estrangeiras `emprestimo.usuario_id` e `emprestimo.equipamento_id`, conforme a orientação do professor.

- Uma pessoa precisa estar **ativa** para realizar um empréstimo.
- Um equipamento precisa estar **DISPONIVEL**; só pode ter **um empréstimo ABERTO** por vez.
- Empréstimo e devolução atualizam o equipamento **na mesma transação**.
- `ATRASADO` é calculado pela data; não é um status salvo no banco.
- O MySQL impede excluir usuários ou equipamentos com empréstimos históricos (`ON DELETE RESTRICT`).
- Um equipamento nunca emprestado pode ser excluído. Se já teve empréstimo, mesmo
  devolvido, mantenha o cadastro e use **Alterar situação > Indisponível** para
  retirá-lo de circulação. Pessoas com histórico podem ficar Inativas. Abrir ou
  exportar um relatório não cria o vínculo que impede exclusão.
- A interface permite cadastrar, consultar, filtrar, ordenar, alterar e excluir registros, respeitando essas regras.

## Testes e requisitos da APS

A [matriz de conformidade](docs/CONFORMIDADE_APS_2026_2.md) relaciona os requisitos da UNIP às funcionalidades do sistema e indica o que ainda precisa ser conferido no relatório acadêmico.

O repositório também possui verificações em `src/test/java`. Elas **não são necessárias para iniciar o programa**: servem para verificar o funcionamento dos DAOs, Controllers, ordenações e janelas Swing. Execute testes de integração somente em um banco de desenvolvimento.

## Equipe

- Lucas — Model, DAO, JDBC, MySQL e integração.
- Marianna — interface Swing e FlatLaf.
- Davi — Controllers e validações.
- Aline — documentação e apresentação.

### Verificações adicionais

- `APSPessoaValidacaoCheck`: valida CPF/celular sem banco.
- `APSPessoasCheck`: gravação, leitura, edição, campos opcionais e CPF único em banco isolado.
- `APSMigracaoCheck`: primeiro teste do CI em banco vazio; prepara V1/V2, cria um
  cadastro antigo e confirma que a V3 o preserva. Não executar em banco de trabalho.
- `APSContatoViewCheck`: seleção do cadastro com/sem contato e inclusão dos campos no CSV.

O CI também executa os testes DAO, o fluxo transacional, ordenação, filtros e criação
das telas Swing. Migrations e testes de integração devem ser validados em bancos de teste.
