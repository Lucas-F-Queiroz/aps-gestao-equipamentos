# APS — interface atualizada

## Referências aplicadas

1. Botão Download Estilo FlatLaf: ícone vetorial azul, contorno suave e estados visualmente distintos. Downloads individuais na coluna Ações e exportação geral em CSV.
2. Cadastro atualizado: abas Equipamentos/Pessoas no alto, Equipamentos aberto inicialmente, formulários compactos, pesquisa por texto e categoria e tabela ampliada.
3. Calendário Empréstimo: botão junto à devolução prevista, seleção de mês e ano, navegação entre meses, dias reais e botão Hoje. Mantida a digitação dd/mm/aaaa; datas anteriores a hoje não são selecionáveis no calendário.
4. Consulta atualizada: pesquisa e filtro Todos/Em uso/Atrasado/Devolvido, tabela com datas e cores de situação, contador de resultados.
5. Equipamentos atualizado: agora é um inventário operacional, diferente do cadastro. Exibe totais, disponibilidade, conservação, departamento, pessoa responsável pelo empréstimo aberto e devolução prevista. Inclusão e alteração continuam na área Cadastros.
6. Devolução é o único lugar da interface que encerra empréstimos. Exige estado, responsável pelo registro e aceita observação opcional.
7. Relatórios é somente consulta e download em CSV; não modifica empréstimos nem equipamentos.

As telas internas têm Voltar para a interface principal. O cabeçalho mantém TI Fácil. Os exemplos de pessoas e equipamentos das imagens não são inseridos no banco.

## Instalação no Eclipse

1. Extraia o ZIP em uma pasta nova. Feche o projeto antigo no Eclipse para não executar sua versão anterior.
2. Importe a pasta aps-gestao-equipamentos em File > Import > Maven > Existing Maven Projects. Se houver conflito de nome, use um workspace novo.
3. Em src/main/resources, copie application.properties.example para application.properties. Preencha db.url, db.user e db.password com a conexão do seu MySQL. Você também pode copiar o application.properties da instalação anterior.
4. Execute Maven > Update Project e Project > Clean.
5. Execute br.com.aps.Aplicacao, ou a configuração Executar_Interface.

Use o banco existente. Somente numa instalação nova, crie o banco usando database/create_database.sql. Não execute os scripts antigos de criação de tabelas por cima de um banco já utilizado.

## Banco e relatórios

O Flyway aplica as migrações pendentes ao abrir o aplicativo. V1 a V4 foram preservadas em relação ao pacote corrigido anterior; V3 é a versão original com checksum 1634580469. V4 cria o registro do estado/descrição da devolução. V5 adiciona registrado_por em devolucao, sem apagar registros. Não edite o histórico Flyway ou execute repair para esconder diferenças.

O campo Responsável pelo registro deve ser preenchido pelo operador na tela Devolução. A identidade fixa `Davi | Administrador` foi removida, pois o projeto ainda não possui autenticação. Registros antigos permanecem sem autor e são exibidos como Não informado.

Os dados do relatório ficam no MySQL; o arquivo CSV é gerado quando o usuário solicita o download. O sistema não guarda arquivos PDF ou CSV dentro do banco. Não há um segundo fluxo de devolução em Relatórios, evitando finalizar o mesmo processo em telas diferentes.

## Validação

Fontes compiladas com --release 17. Testes executados em MySQL 8.0 isolado: atualização de banco antigo, migrações até V5, regras de cadastro/empréstimo/devolução, responsável persistido, exportação CSV, filtros e seleção correta de linhas e calendário com ano bissexto. Telas renderizadas para inspeção visual.

O banco e a instalação do usuário não foram modificados nem testados diretamente. Os programas de teste que escrevem dados devem ser executados somente com uma conexão de desenvolvimento isolada.
