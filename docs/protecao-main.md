# Protecao da main

Lucas (`@Lucas-F-Queiroz`, ID GitHub `200534446`) decide quais alteracoes entram na `main`.
Colaboradores devem trabalhar em outras branches e abrir Pull Requests.

## Estado atual: protecao ativa no GitHub

Em 26/10/2026 a primeira consulta encontrou o repositorio **privado**, em uma
conta **GitHub Free**. A API recusou o acesso a rulesets/protecao com HTTP 403:

> Upgrade to GitHub Pro or make this repository public to enable this feature.

Lucas tornou o repositorio publico durante a configuracao. Depois disso, a regra
foi criada com sucesso e sua aplicacao a `refs/heads/main` foi verificada pela API:

- ID: `25897210`.
- Estado: `active`.
- [Regra ativa no GitHub](https://github.com/Lucas-F-Queiroz/aps-gestao-equipamentos/rules/25897210).
- Unica excecao: usuario `Lucas-F-Queiroz`, somente por Pull Request.

A restricao de atualizacoes impede os demais colaboradores de alterar ou mergear
na `main`, mesmo antes da integracao deste documento. A exigencia de revisao por
CODEOWNER usa o arquivo `.github/CODEOWNERS` da branch-base; esse arquivo precisa
estar integrado na `main` para que Lucas seja solicitado como responsavel por todos
os arquivos. As permissoes de colaboracao da equipe foram preservadas.

No GitHub Free, rulesets ficam disponiveis em repositorios publicos. Para usar
em repositorio privado, e necessario um plano compativel, como GitHub Pro.

## O que a regra faz depois de ativada

- Aplica-se somente a `refs/heads/main`.
- Exige Pull Request e revisao do responsavel definido em `.github/CODEOWNERS`.
- Define Lucas como responsavel por todos os arquivos, inclusive o proprio `CODEOWNERS`.
- Descarta aprovacoes anteriores quando chegam novos commits e exige resolver as conversas.
- Restringe atualizacoes da branch a Lucas: os colegas nao conseguem concluir o merge,
  mesmo que outra pessoa tenha aprovado a PR.
- Bloqueia push direto, force-push e exclusao da `main`.
- Permite excecao **somente ao usuario Lucas e somente via Pull Request**.
  Nenhum colaborador, equipe, GitHub App ou papel generico de administrador esta na lista.

O GitHub nao permite aprovar a propria PR. Para suas proprias mudancas, Lucas pode
usar a excecao de merge pela PR. Essa excecao tambem permite que Lucas decida
concluir outras PRs sem satisfazer a revisao exigida; continua sendo uma decisao
exclusiva dele, registrada no GitHub. A excecao nao permite push direto.

Como proprietario, Lucas ainda pode editar ou desativar a regra nas configuracoes.

## Reproduzir a configuracao

Esta regra ja foi criada no repositorio e nao deve ser duplicada. Integrar a PR
desta configuracao para que `.github/CODEOWNERS` tambem esteja na `main`.
O JSON fica versionado para auditoria e reproducao; a protecao depende da regra
ativa nas configuracoes do GitHub, nao apenas do arquivo no Git.

Na raiz do projeto, com GitHub CLI autenticado como Lucas e com permissao de
administracao do repositorio, o comando de criacao e:

```powershell
gh api --method POST repos/Lucas-F-Queiroz/aps-gestao-equipamentos/rulesets --input .github/rulesets/main.json
```

O comando nao muda a visibilidade nem as permissoes da equipe. A resposta deve
conter `enforcement: active` e o ID da regra. Se retornar HTTP 403, a protecao
nao foi ativada; conferir plano, visibilidade e permissoes do token.

Para verificar:

```powershell
gh api repos/Lucas-F-Queiroz/aps-gestao-equipamentos/rulesets
gh api repos/Lucas-F-Queiroz/aps-gestao-equipamentos/rules/branches/main
```

Conferir tambem **Settings > Rules > Rulesets**: regra ativa na `main` e apenas
Lucas na lista de excecoes, em modo **For pull requests only**. Para conferir
os detalhes pela API, usar o ID retornado:

```powershell
gh api repos/Lucas-F-Queiroz/aps-gestao-equipamentos/rulesets/25897210
```

O POST cria uma regra nova. Se ela ja existir, verificar os detalhes em vez de
repetir o comando e gerar regras duplicadas. Uma atualizacao usa `--method PUT`
no endpoint `rulesets/ID_DA_REGRA`, com o mesmo `--input`.

## Quando a entrega estiver definitivamente aprovada

Para impedir tambem novas alteracoes por Lucas e congelar a entrega inteira,
o proprietario pode **arquivar o repositorio** depois da aprovacao final.
O arquivamento deixa o repositorio somente para leitura e pode ser desfeito
pelo proprietario. Isso e uma etapa separada: nao arquivar enquanto o grupo
ainda estiver trabalhando.

Mesmo publico, o repositorio nao concede escrita a visitantes: pessoas sem
permissao podem propor mudancas por forks e PRs, mas nao altera-lo diretamente.
A protecao trata principalmente os acessos de escrita ja concedidos a equipe.

## Referencias oficiais

- [Disponibilidade e funcionamento dos rulesets](https://docs.github.com/en/repositories/configuring-branches-and-merges-in-your-repository/managing-rulesets/about-rulesets)
- [Excecoes somente por Pull Request](https://docs.github.com/en/repositories/configuring-branches-and-merges-in-your-repository/managing-rulesets/creating-rulesets-for-a-repository)
- [Regras disponiveis](https://docs.github.com/en/repositories/configuring-branches-and-merges-in-your-repository/managing-rulesets/available-rules-for-rulesets)
- [CODEOWNERS e aprovacao](https://docs.github.com/en/repositories/managing-your-repositorys-settings-and-features/customizing-your-repository/about-code-owners)
- [API de rulesets](https://docs.github.com/en/rest/repos/rules)
