package br.com.aps.dao;

import br.com.aps.model.DevolucaoDetalhe;
import br.com.aps.util.ConnectionFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

/**
 * Consulta o estado em que os equipamentos foram devolvidos (tabela devolucao, migration V4).
 * A gravacao e feita por EmprestimoDAO.registrarDevolucao(id, data, estado, observacao),
 * na mesma transacao da devolucao.
 */
public class DevolucaoDAO {

    /** Devolucoes registradas com estado, da mais recente para a mais antiga. */
    public List<DevolucaoDetalhe> listarDetalhado() throws SQLException {
        String sql = """
                SELECT e.id AS emprestimo_id, e.data_devolucao, d.estado, d.observacao, d.registrado_por,
                       u.nome AS usuario_nome,
                       eq.nome AS equipamento_nome,
                       eq.patrimonio AS equipamento_patrimonio
                FROM emprestimo e
                LEFT JOIN devolucao d ON d.emprestimo_id = e.id
                INNER JOIN usuario u ON e.usuario_id = u.id
                INNER JOIN equipamento eq ON e.equipamento_id = eq.id
                WHERE e.status = 'DEVOLVIDO'
                ORDER BY e.data_devolucao DESC, e.id DESC
                """;

        List<DevolucaoDetalhe> devolucoes = new ArrayList<>();
        try (Connection conexao = ConnectionFactory.getConnection();
             PreparedStatement consulta = conexao.prepareStatement(sql);
             ResultSet resultado = consulta.executeQuery()) {

            while (resultado.next()) {
                Timestamp data = resultado.getTimestamp("data_devolucao");
                devolucoes.add(new DevolucaoDetalhe(
                        resultado.getInt("emprestimo_id"),
                        data == null ? null : data.toLocalDateTime(),
                        resultado.getString("usuario_nome"),
                        resultado.getString("equipamento_nome"),
                        resultado.getString("equipamento_patrimonio"),
                        resultado.getString("estado"),
                        resultado.getString("observacao"),
                        resultado.getString("registrado_por")
                ));
            }
        }
        return devolucoes;
    }
}
