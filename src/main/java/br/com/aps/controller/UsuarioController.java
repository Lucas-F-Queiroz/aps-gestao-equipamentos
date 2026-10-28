package br.com.aps.controller;

import br.com.aps.dao.EmprestimoDAO;
import br.com.aps.dao.UsuarioDAO;
import br.com.aps.model.Emprestimo;
import br.com.aps.model.Usuario;

import java.sql.SQLException;
import java.util.List;

/**
 * Regras e validacoes de pessoas (tabela usuario do Lucas):
 * nome ate 100, e-mail valido ate 120 e unico, área/profissão livre até 100 caracteres e matrícula opcional única, ativo ou inativo.
 * Quem tem historico de emprestimos nao e excluido (chave estrangeira RESTRICT): use Inativo.
 */
public class UsuarioController {
    private final UsuarioDAO usuarioDAO;
    private final EmprestimoDAO emprestimoDAO;

    public UsuarioController() {
        this(new UsuarioDAO(), new EmprestimoDAO());
    }

    public UsuarioController(UsuarioDAO usuarioDAO, EmprestimoDAO emprestimoDAO) {
        this.usuarioDAO = usuarioDAO;
        this.emprestimoDAO = emprestimoDAO;
    }

    public List<Usuario> listar() {
        try {
            return usuarioDAO.listar();
        } catch (SQLException erro) {
            throw TradutorErros.traduzir(erro);
        }
    }

    /** Pessoa nova nasce ativa. */
    public Usuario cadastrar(String nome, String email, String tipo) {
        return cadastrar(nome, email, tipo, null);
    }

    public Usuario cadastrar(String nome, String email, String tipo, String matricula) {
        String nomeValido = Regras.exigirTexto("nome", nome, Regras.NOME_USUARIO_MAX);
        String emailValido = Regras.exigirEmail(email);
        String tipoValido = Regras.exigirTexto("área/profissão", tipo, 100);
        verificarEmailRepetido(emailValido, 0);

        Usuario usuario = new Usuario(nomeValido, emailValido, tipoValido);
        usuario.setMatricula(Regras.textoOpcional("matrícula", matricula, 50));
        try {
            usuarioDAO.inserir(usuario);
        } catch (SQLException erro) {
            throw TradutorErros.traduzir(erro);
        }
        return usuario;
    }

    /**
     * Altera uma pessoa. Inativar e permitido: a regra do Lucas so exige pessoa ativa para
     * EMPRESTAR, entao quem esta com um equipamento ainda pode devolve-lo.
     */
    public void atualizar(int id, String nome, String email, String tipo, boolean ativo) {
        try {
            Usuario atual = usuarioDAO.buscarPorId(id);
            if (atual == null) throw new RegraNegocioException("Pessoa não encontrada.");
            atualizar(id, nome, email, tipo, ativo, atual.getMatricula());
        } catch (SQLException erro) { throw TradutorErros.traduzir(erro); }
    }

    public void atualizar(int id, String nome, String email, String tipo, boolean ativo, String matricula) {
        String nomeValido = Regras.exigirTexto("nome", nome, Regras.NOME_USUARIO_MAX);
        String emailValido = Regras.exigirEmail(email);
        String tipoValido = Regras.exigirTexto("área/profissão", tipo, 100);
        verificarEmailRepetido(emailValido, id);

        Usuario usuario = new Usuario(id, nomeValido, emailValido, tipoValido, ativo);
        usuario.setMatricula(Regras.textoOpcional("matrícula", matricula, 50));
        try {
            if (!usuarioDAO.atualizar(usuario)) {
                throw new RegraNegocioException("Pessoa não encontrada.");
            }
        } catch (SQLException erro) {
            throw TradutorErros.traduzir(erro);
        }
    }

    /** Verdadeiro se a pessoa ainda esta com algum equipamento emprestado (emprestimo ABERTO). */
    public boolean possuiEmprestimoAberto(int usuarioId) {
        try {
            for (Emprestimo emprestimo : emprestimoDAO.listarPorUsuario(usuarioId)) {
                if (Emprestimo.STATUS_ABERTO.equals(emprestimo.getStatus())) {
                    return true;
                }
            }
            return false;
        } catch (SQLException erro) {
            throw TradutorErros.traduzir(erro);
        }
    }

    public void excluir(int id) {
        try {
            if (!emprestimoDAO.listarPorUsuario(id).isEmpty()) {
                throw new RegraNegocioException(
                    "Esta pessoa possui histórico de empréstimos e não pode ser excluída.\n"
                        + "Use o status Inativo.");
            }
            if (!usuarioDAO.excluir(id)) {
                throw new RegraNegocioException("Pessoa não encontrada.");
            }
        } catch (SQLException erro) {
            throw TradutorErros.traduzir(erro);
        }
    }

    // O banco tambem recusa e-mail repetido (UNIQUE); aqui a mensagem sai antes e ignora maiusculas.
    private void verificarEmailRepetido(String email, int idDaPessoaAtual) {
        try {
            for (Usuario existente : usuarioDAO.listar()) {
                if (existente.getId() != idDaPessoaAtual && existente.getEmail().equalsIgnoreCase(email)) {
                    throw new RegraNegocioException("Já existe uma pessoa com esse e-mail.");
                }
            }
        } catch (SQLException erro) {
            throw TradutorErros.traduzir(erro);
        }
    }
}
