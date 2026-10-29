package br.com.aps.controller;

import br.com.aps.dao.UsuarioDAO;
import br.com.aps.model.Usuario;
import java.sql.SQLException;
import java.util.List;

/** Regras de cadastro de pessoas; nenhuma consulta SQL deve ficar na View. */
public class UsuarioController {
    private final UsuarioDAO dao;

    public UsuarioController() { this(new UsuarioDAO()); }
    public UsuarioController(UsuarioDAO dao) { this.dao = dao; }

    public List<Usuario> listar() {
        try { return dao.listar(); }
        catch (SQLException e) { throw TradutorErros.traduzir(e); }
    }

    public Usuario cadastrar(String nome, String email, String tipo, String cpf, String celular) {
        Usuario usuario = new Usuario(
            Regras.exigirTexto("nome", nome, Regras.NOME_USUARIO_MAX),
            Regras.exigirEmail(email),
            Regras.exigirUm("tipo", tipo, Regras.TIPOS_USUARIO));
        usuario.setCpf(Regras.cpf(cpf));
        usuario.setCelular(Regras.celular(celular));
        try { dao.inserir(usuario); return usuario; }
        catch (SQLException e) { throw TradutorErros.traduzir(e); }
    }

    public void atualizar(int id, String nome, String email, String tipo, boolean ativo,
                          String cpf, String celular) {
        Usuario usuario = new Usuario(id,
            Regras.exigirTexto("nome", nome, Regras.NOME_USUARIO_MAX),
            Regras.exigirEmail(email),
            Regras.exigirUm("tipo", tipo, Regras.TIPOS_USUARIO), ativo);
        usuario.setCpf(Regras.cpf(cpf));
        usuario.setCelular(Regras.celular(celular));
        try {
            if (!dao.atualizar(usuario)) throw new RegraNegocioException("Pessoa não encontrada.");
        } catch (SQLException e) { throw TradutorErros.traduzir(e); }
    }

    public void excluir(int id) {
        try {
            if (!dao.excluir(id)) throw new RegraNegocioException("Pessoa não encontrada.");
        } catch (SQLException e) { throw TradutorErros.traduzir(e); }
    }
}
