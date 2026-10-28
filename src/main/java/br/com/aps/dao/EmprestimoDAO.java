package br.com.aps.dao;

import br.com.aps.model.Emprestimo;
import br.com.aps.model.EmprestimoDetalhe;
import br.com.aps.model.Equipamento;
import br.com.aps.util.ConnectionFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class EmprestimoDAO {

    private static final String COLUNAS =
            "id, usuario_id, equipamento_id, data_retirada, data_prevista, data_devolucao, status";

    /**
     * Registra um emprestimo e marca o equipamento como emprestado
     * na MESMA transacao JDBC.
     *
     * Em caso de erro, nenhuma das duas alteracoes fica gravada.
     */
    public void inserir(Emprestimo emprestimo) throws SQLException {
        if (emprestimo == null
                || emprestimo.getDataRetirada() == null
                || emprestimo.getDataPrevista() == null) {
            throw new IllegalArgumentException("Emprestimo e datas sao obrigatorios.");
        }
        if (!emprestimo.getDataPrevista().isAfter(emprestimo.getDataRetirada())) {
            throw new IllegalArgumentException("Data prevista deve ser posterior a retirada.");
        }

        try (Connection conexao = ConnectionFactory.getConnection()) {
            conexao.setAutoCommit(false);
            try {
                // Bloqueia o usuario durante a operacao e garante que esteja ativo.
                if (!usuarioAtivo(conexao, emprestimo.getUsuarioId())) {
                    throw new SQLException("Usuario inexistente ou inativo.", "45000");
                }

                // UPDATE condicional bloqueia a linha e evita dois emprestimos simultaneos.
                if (!marcarEquipamentoComoEmprestado(conexao, emprestimo.getEquipamentoId())) {
                    throw new SQLException("Equipamento inexistente ou indisponivel.", "45000");
                }

                // Verificacao defensiva para bancos com registros anteriores inconsistentes.
                if (possuiEmprestimoAberto(conexao, emprestimo.getEquipamentoId())) {
                    throw new SQLException("Equipamento ja possui emprestimo aberto.", "45000");
                }

                int id = inserirEmprestimo(conexao, emprestimo);
                conexao.commit();

                // Somente atualiza o objeto apos confirmar no banco.
                emprestimo.setId(id);
                emprestimo.setStatus(Emprestimo.STATUS_ABERTO);
                emprestimo.setDataDevolucao(null);
            } catch (SQLException | RuntimeException erro) {
                desfazer(conexao, erro);
                throw erro;
            }
        }
    }

    /**
     * Retorna false para emprestimo inexistente ou ja devolvido.
     * Se a atualizacao do equipamento falhar, aplica rollback no emprestimo.
     */
    public boolean registrarDevolucao(int emprestimoId, LocalDateTime devolucao)
            throws SQLException {
        return registrarDevolucao(emprestimoId, devolucao, null, null);
    }

    /**
     * Igual a registrarDevolucao(id, data), e tambem registra COMO o equipamento voltou
     * (NOVO, BOM, REGULAR ou DANIFICADO) na MESMA transacao:
     *  - o estado informado passa a ser o estado do equipamento;
     *  - estado e observacao ficam gravados na tabela devolucao (V4), para o relatorio.
     * Com estado null, o comportamento e o da versao antiga (nada do estado e gravado).
     * Continua valendo a regra 5: o equipamento volta para DISPONIVEL.
     */
    public boolean registrarDevolucao(int emprestimoId, LocalDateTime devolucao,
                                      String estado, String observacao) throws SQLException {
        return registrarDevolucao(emprestimoId, devolucao, estado, observacao, null);
    }

    public boolean registrarDevolucao(int emprestimoId, LocalDateTime devolucao,
            String estado, String observacao, String responsavel) throws SQLException {
        if (devolucao == null) {
            throw new IllegalArgumentException("Data de devolucao e obrigatoria.");
        }
        if (estado != null && !estadoValido(estado)) {
            throw new IllegalArgumentException("Estado do equipamento invalido.");
        }

        try (Connection conexao = ConnectionFactory.getConnection()) {
            conexao.setAutoCommit(false);
            try {
                int equipamentoId;
                LocalDateTime retirada;

                String sqlBusca = """
                        SELECT equipamento_id, data_retirada, status
                        FROM emprestimo
                        WHERE id = ?
                        FOR UPDATE
                        """;
                try (PreparedStatement consulta = conexao.prepareStatement(sqlBusca)) {
                    consulta.setInt(1, emprestimoId);

                    try (ResultSet resultado = consulta.executeQuery()) {
                        if (!resultado.next()
                                || !Emprestimo.STATUS_ABERTO.equals(
                                        resultado.getString("status"))) {
                            conexao.rollback();
                            return false;
                        }
                        equipamentoId = resultado.getInt("equipamento_id");
                        retirada = resultado.getTimestamp("data_retirada").toLocalDateTime();
                    }
                }

                if (devolucao.isBefore(retirada)) {
                    throw new IllegalArgumentException(
                            "Devolucao nao pode ser anterior a retirada.");
                }

                String sqlDevolucao = """
                        UPDATE emprestimo
                        SET status = ?, data_devolucao = ?
                        WHERE id = ? AND status = ?
                        """;
                try (PreparedStatement atualizacao = conexao.prepareStatement(sqlDevolucao)) {
                    atualizacao.setString(1, Emprestimo.STATUS_DEVOLVIDO);
                    atualizacao.setTimestamp(2, Timestamp.valueOf(devolucao));
                    atualizacao.setInt(3, emprestimoId);
                    atualizacao.setString(4, Emprestimo.STATUS_ABERTO);

                    if (atualizacao.executeUpdate() != 1) {
                        throw new SQLException("Nao foi possivel registrar a devolucao.", "45000");
                    }
                }

                String sqlEquipamento = estado == null
                        ? "UPDATE equipamento SET status = ? WHERE id = ? AND status = ?"
                        : "UPDATE equipamento SET status = ?, estado = ? WHERE id = ? AND status = ?";
                try (PreparedStatement atualizacao = conexao.prepareStatement(sqlEquipamento)) {
                    int posicao = 1;
                    atualizacao.setString(posicao++, Equipamento.STATUS_DISPONIVEL);
                    if (estado != null) {
                        atualizacao.setString(posicao++, estado);
                    }
                    atualizacao.setInt(posicao++, equipamentoId);
                    atualizacao.setString(posicao, Equipamento.STATUS_EMPRESTADO);

                    if (atualizacao.executeUpdate() != 1) {
                        throw new SQLException("Status do equipamento inconsistente.", "45000");
                    }
                }

                if (estado != null) {
                    gravarEstadoDaDevolucao(conexao, emprestimoId, estado, observacao, responsavel);
                }

                conexao.commit();
                return true;
            } catch (SQLException | RuntimeException erro) {
                desfazer(conexao, erro);
                throw erro;
            }
        }
    }

    public boolean registrarDevolucao(int emprestimoId) throws SQLException {
        return registrarDevolucao(emprestimoId, LocalDateTime.now());
    }

    public Emprestimo buscarPorId(int id) throws SQLException {
        String sql = "SELECT " + COLUNAS + " FROM emprestimo WHERE id = ?";

        try (Connection conexao = ConnectionFactory.getConnection();
             PreparedStatement consulta = conexao.prepareStatement(sql)) {

            consulta.setInt(1, id);
            try (ResultSet resultado = consulta.executeQuery()) {
                if (resultado.next()) {
                    return mapearEmprestimo(resultado);
                }
            }
        }
        return null;
    }

    public List<Emprestimo> listar() throws SQLException {
        String sql = "SELECT " + COLUNAS
                + " FROM emprestimo ORDER BY data_retirada DESC, id DESC";
        return consultarTodos(sql);
    }

    public List<Emprestimo> listarAbertos() throws SQLException {
        String sql = "SELECT " + COLUNAS
                + " FROM emprestimo WHERE status = ? ORDER BY data_prevista, id";
        return consultarFiltrados(sql, Emprestimo.STATUS_ABERTO);
    }

    // ATRASADO e calculado, nunca armazenado como status.
    public List<Emprestimo> listarAtrasados() throws SQLException {
        String sql = "SELECT " + COLUNAS + """
                 FROM emprestimo
                 WHERE status = ? AND data_prevista < NOW()
                 ORDER BY data_prevista ASC, id ASC
                """;
        return consultarFiltrados(sql, Emprestimo.STATUS_ABERTO);
    }

    public List<Emprestimo> listarPorUsuario(int usuarioId) throws SQLException {
        String sql = "SELECT " + COLUNAS + """
                 FROM emprestimo WHERE usuario_id = ?
                 ORDER BY data_retirada DESC, id DESC
                """;
        return consultarFiltrados(sql, usuarioId);
    }

    public List<Emprestimo> listarPorEquipamento(int equipamentoId) throws SQLException {
        String sql = "SELECT " + COLUNAS + """
                 FROM emprestimo WHERE equipamento_id = ?
                 ORDER BY data_retirada DESC, id DESC
                """;
        return consultarFiltrados(sql, equipamentoId);
    }

    /**
     * Consulta com JOIN para apresentar nomes e patrimonio nas tabelas da View.
     * EmprestimoDetalhe e um DTO, nao uma tabela adicional.
     */
    public List<EmprestimoDetalhe> listarDetalhado() throws SQLException {
        String sql = """
                SELECT e.id, e.usuario_id, e.equipamento_id,
                       e.data_retirada, e.data_prevista, e.data_devolucao, e.status,
                       u.nome AS usuario_nome,
                       eq.nome AS equipamento_nome,
                       eq.patrimonio AS equipamento_patrimonio
                FROM emprestimo e
                INNER JOIN usuario u ON e.usuario_id = u.id
                INNER JOIN equipamento eq ON e.equipamento_id = eq.id
                ORDER BY e.data_retirada DESC, e.id DESC
                """;

        List<EmprestimoDetalhe> historico = new ArrayList<>();
        try (Connection conexao = ConnectionFactory.getConnection();
             PreparedStatement consulta = conexao.prepareStatement(sql);
             ResultSet resultado = consulta.executeQuery()) {

            while (resultado.next()) {
                historico.add(new EmprestimoDetalhe(
                        mapearEmprestimo(resultado),
                        resultado.getString("usuario_nome"),
                        resultado.getString("equipamento_nome"),
                        resultado.getString("equipamento_patrimonio")
                ));
            }
        }
        return historico;
    }

    private boolean estadoValido(String estado) {
        return Equipamento.ESTADO_NOVO.equals(estado)
                || Equipamento.ESTADO_BOM.equals(estado)
                || Equipamento.ESTADO_REGULAR.equals(estado)
                || Equipamento.ESTADO_DANIFICADO.equals(estado);
    }

    private void gravarEstadoDaDevolucao(Connection conexao, int emprestimoId,
                                         String estado, String observacao, String responsavel) throws SQLException {
        String sql = "INSERT INTO devolucao (emprestimo_id, estado, observacao, registrado_por) VALUES (?, ?, ?, ?)";
        try (PreparedStatement insercao = conexao.prepareStatement(sql)) {
            insercao.setInt(1, emprestimoId);
            insercao.setString(2, estado);
            if (observacao == null || observacao.isBlank()) {
                insercao.setNull(3, java.sql.Types.VARCHAR);
            } else {
                insercao.setString(3, observacao.trim());
            }
            insercao.setString(4, responsavel);
            insercao.executeUpdate();
        }
    }

    private boolean usuarioAtivo(Connection conexao, int usuarioId) throws SQLException {
        String sql = "SELECT ativo FROM usuario WHERE id = ? FOR UPDATE";
        try (PreparedStatement consulta = conexao.prepareStatement(sql)) {
            consulta.setInt(1, usuarioId);
            try (ResultSet resultado = consulta.executeQuery()) {
                return resultado.next() && resultado.getBoolean("ativo");
            }
        }
    }

    private boolean marcarEquipamentoComoEmprestado(Connection conexao, int equipamentoId)
            throws SQLException {
        String sql = "UPDATE equipamento SET status = ? WHERE id = ? AND status = ?";
        try (PreparedStatement atualizacao = conexao.prepareStatement(sql)) {
            atualizacao.setString(1, Equipamento.STATUS_EMPRESTADO);
            atualizacao.setInt(2, equipamentoId);
            atualizacao.setString(3, Equipamento.STATUS_DISPONIVEL);
            return atualizacao.executeUpdate() == 1;
        }
    }

    private boolean possuiEmprestimoAberto(Connection conexao, int equipamentoId)
            throws SQLException {
        String sql = """
                SELECT id FROM emprestimo
                WHERE equipamento_id = ? AND status = ?
                LIMIT 1
                """;
        try (PreparedStatement consulta = conexao.prepareStatement(sql)) {
            consulta.setInt(1, equipamentoId);
            consulta.setString(2, Emprestimo.STATUS_ABERTO);
            try (ResultSet resultado = consulta.executeQuery()) {
                return resultado.next();
            }
        }
    }

    private int inserirEmprestimo(Connection conexao, Emprestimo emprestimo)
            throws SQLException {
        String sql = """
                INSERT INTO emprestimo
                    (usuario_id, equipamento_id, data_retirada, data_prevista, status)
                VALUES (?, ?, ?, ?, ?)
                """;

        try (PreparedStatement insercao =
                conexao.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            insercao.setInt(1, emprestimo.getUsuarioId());
            insercao.setInt(2, emprestimo.getEquipamentoId());
            insercao.setTimestamp(3, Timestamp.valueOf(emprestimo.getDataRetirada()));
            insercao.setTimestamp(4, Timestamp.valueOf(emprestimo.getDataPrevista()));
            insercao.setString(5, Emprestimo.STATUS_ABERTO);

            if (insercao.executeUpdate() != 1) {
                throw new SQLException("Falha ao inserir emprestimo.");
            }
            try (ResultSet chaves = insercao.getGeneratedKeys()) {
                if (chaves.next()) {
                    return chaves.getInt(1);
                }
            }
        }
        throw new SQLException("MySQL nao retornou o ID do emprestimo.");
    }

    private List<Emprestimo> consultarTodos(String sql) throws SQLException {
        List<Emprestimo> emprestimos = new ArrayList<>();
        try (Connection conexao = ConnectionFactory.getConnection();
             PreparedStatement consulta = conexao.prepareStatement(sql);
             ResultSet resultado = consulta.executeQuery()) {
            while (resultado.next()) {
                emprestimos.add(mapearEmprestimo(resultado));
            }
        }
        return emprestimos;
    }

    private List<Emprestimo> consultarFiltrados(String sql, String valor)
            throws SQLException {
        List<Emprestimo> emprestimos = new ArrayList<>();
        try (Connection conexao = ConnectionFactory.getConnection();
             PreparedStatement consulta = conexao.prepareStatement(sql)) {
            consulta.setString(1, valor);
            try (ResultSet resultado = consulta.executeQuery()) {
                while (resultado.next()) {
                    emprestimos.add(mapearEmprestimo(resultado));
                }
            }
        }
        return emprestimos;
    }

    private List<Emprestimo> consultarFiltrados(String sql, int valor)
            throws SQLException {
        List<Emprestimo> emprestimos = new ArrayList<>();
        try (Connection conexao = ConnectionFactory.getConnection();
             PreparedStatement consulta = conexao.prepareStatement(sql)) {
            consulta.setInt(1, valor);
            try (ResultSet resultado = consulta.executeQuery()) {
                while (resultado.next()) {
                    emprestimos.add(mapearEmprestimo(resultado));
                }
            }
        }
        return emprestimos;
    }

    private Emprestimo mapearEmprestimo(ResultSet resultado) throws SQLException {
        Timestamp devolucao = resultado.getTimestamp("data_devolucao");
        return new Emprestimo(
                resultado.getInt("id"),
                resultado.getInt("usuario_id"),
                resultado.getInt("equipamento_id"),
                resultado.getTimestamp("data_retirada").toLocalDateTime(),
                resultado.getTimestamp("data_prevista").toLocalDateTime(),
                devolucao == null ? null : devolucao.toLocalDateTime(),
                resultado.getString("status")
        );
    }

    private void desfazer(Connection conexao, Exception erro) {
        try {
            conexao.rollback();
        } catch (SQLException falhaRollback) {
            erro.addSuppressed(falhaRollback);
        }
    }
}
