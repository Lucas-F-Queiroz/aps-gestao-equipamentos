package br.com.aps.model;

import java.time.LocalDateTime;

/**
 * Como o equipamento estava na devolucao (uma linha da tabela devolucao, com os nomes do JOIN).
 * E um DTO de leitura para o relatorio.
 */
public record DevolucaoDetalhe(
        int emprestimoId,
        LocalDateTime dataDevolucao,
        String usuarioNome,
        String equipamentoNome,
        String equipamentoPatrimonio,
        String estado,
        String observacao,
        String registradoPor
) {
    public DevolucaoDetalhe(int id, LocalDateTime data, String usuario, String equipamento,
            String patrimonio, String estado, String observacao) {
        this(id,data,usuario,equipamento,patrimonio,estado,observacao,null);
    }
}
