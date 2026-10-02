package br.com.aps.model;

/**
 * Dados para exibicao de historico com JOIN.
 * E um DTO de leitura, nao representa uma nova tabela no banco.
 */
public record EmprestimoDetalhe(
        Emprestimo emprestimo,
        String usuarioNome,
        String equipamentoNome,
        String equipamentoPatrimonio
) {
}
