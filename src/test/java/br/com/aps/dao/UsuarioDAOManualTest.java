package br.com.aps.dao;

import br.com.aps.model.Usuario;
import br.com.aps.util.DatabaseMigrator;

import java.sql.SQLException;
import java.util.List;
import java.util.UUID;

/**
 * Teste de integracao manual com MySQL local.
 *
 * Preparacao: criar o banco com database/create_database.sql
 * e configurar APS_DB_URL, APS_DB_USER e APS_DB_PASSWORD quando necessario.
 *
 * Execute esta classe pela IDE usando o metodo main.
 * O registro temporario criado pelo teste e removido ao final.
 */
public class UsuarioDAOManualTest {

    public static void main(String[] args) throws SQLException {
        DatabaseMigrator.migrate();

        UsuarioDAO dao = new UsuarioDAO();
        String emailTemporario = "teste.usuario." + UUID.randomUUID() + "@example.invalid";
        Usuario usuario = new Usuario("Teste DAO", emailTemporario, Usuario.TIPO_ALUNO);

        try {
            dao.inserir(usuario);
            verificar(usuario.getId() > 0, "INSERT nao retornou o ID.");

            Usuario encontrado = dao.buscarPorId(usuario.getId());
            verificar(encontrado != null, "Busca por ID falhou.");
            verificar(emailTemporario.equals(encontrado.getEmail()), "Email incorreto.");

            List<Usuario> porNome = dao.buscarPorNome("Teste DAO");
            verificar(
                    porNome.stream().anyMatch(u -> u.getId() == usuario.getId()),
                    "Busca por nome nao encontrou o usuario."
            );

            verificar(
                    dao.listar().stream().anyMatch(u -> u.getId() == usuario.getId()),
                    "Listagem nao encontrou o usuario."
            );

            usuario.setNome("Teste DAO Atualizado");
            usuario.setAtivo(false);
            verificar(dao.atualizar(usuario), "UPDATE nao alterou o usuario.");

            Usuario atualizado = dao.buscarPorId(usuario.getId());
            verificar(atualizado != null, "Usuario desapareceu apos UPDATE.");
            verificar("Teste DAO Atualizado".equals(atualizado.getNome()), "Nome nao atualizado.");
            verificar(!atualizado.isAtivo(), "Campo ativo nao atualizado.");

            verificar(dao.excluir(usuario.getId()), "DELETE nao removeu o usuario.");
            verificar(dao.buscarPorId(usuario.getId()) == null, "Usuario ainda existe apos DELETE.");

            System.out.println("Teste manual de UsuarioDAO: todas as verificacoes passaram.");
        } finally {
            // Limpeza em caso de falha; nao interfere com outros registros.
            if (usuario.getId() > 0 && dao.buscarPorId(usuario.getId()) != null) {
                dao.excluir(usuario.getId());
            }
        }
    }

    private static void verificar(boolean condicao, String mensagem) {
        if (!condicao) {
            throw new AssertionError(mensagem);
        }
    }
}
