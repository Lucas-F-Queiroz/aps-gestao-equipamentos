package br.com.aps.integracao;

import br.com.aps.controller.RegraNegocioException;
import br.com.aps.controller.UsuarioController;
import br.com.aps.dao.UsuarioDAO;
import br.com.aps.model.Usuario;
import br.com.aps.util.DatabaseMigrator;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/** CRUD e unicidade com dados sinteticos; executar apenas em banco isolado. */
public final class APSPessoasCheck {
    public static void main(String[] args) throws Exception {
        DatabaseMigrator.migrate();
        UsuarioController controller = new UsuarioController();
        UsuarioDAO dao = new UsuarioDAO();
        String tag = UUID.randomUUID().toString();
        List<Integer> ids = new ArrayList<>();
        try {
            Usuario pessoa = controller.cadastrar("Pessoa CPF " + tag, tag + "@example.invalid",
                    Usuario.TIPO_ALUNO, "012.345.678-90", "(11) 91234-5678");
            ids.add(pessoa.getId());
            Usuario lida = dao.buscarPorId(pessoa.getId());
            exigir("01234567890".equals(lida.getCpf()), "Perdeu CPF ou zero inicial");
            exigir("11912345678".equals(lida.getCelular()), "Nao gravou celular normalizado");
            exigir(dao.buscarPorNome(tag).stream().anyMatch(u -> "01234567890".equals(u.getCpf())),
                    "Pesquisa nao mapeou CPF");
            exigir(controller.listar().stream().anyMatch(u -> "11912345678".equals(u.getCelular())),
                    "Listagem nao mapeou celular");

            try {
                Usuario repetida = controller.cadastrar("Duplicada", "dup." + tag + "@example.invalid",
                        Usuario.TIPO_PROFESSOR, "01234567890", "");
                ids.add(repetida.getId());
                throw new AssertionError("CPF duplicado foi aceito");
            } catch (RegraNegocioException esperado) {
                exigir(esperado.getMessage().contains("CPF"), "Duplicidade sem mensagem do CPF");
            }

            controller.atualizar(pessoa.getId(), pessoa.getNome(), pessoa.getEmail(), pessoa.getTipo(),
                    false, lida.getCpf(), lida.getCelular());
            lida = dao.buscarPorId(pessoa.getId());
            exigir(!lida.isAtivo() && "01234567890".equals(lida.getCpf())
                    && "11912345678".equals(lida.getCelular()), "Troca de situacao perdeu contato");
            controller.atualizar(pessoa.getId(), "Nome editado", pessoa.getEmail(), pessoa.getTipo(),
                    true, "529.982.247-25", "(21) 98765-4321");
            lida = dao.buscarPorId(pessoa.getId());
            exigir("52998224725".equals(lida.getCpf()) && "21987654321".equals(lida.getCelular()),
                    "Edicao nao persistiu contatos");
            controller.atualizar(pessoa.getId(), lida.getNome(), lida.getEmail(), lida.getTipo(),
                    true, "", "");
            lida = dao.buscarPorId(pessoa.getId());
            exigir(lida.getCpf() == null && lida.getCelular() == null, "Campos vazios devem virar NULL");

            for (int i = 0; i < 2; i++) {
                Usuario semCpf = controller.cadastrar("Sem CPF", "vazio" + i + tag + "@example.invalid",
                        Usuario.TIPO_FUNCIONARIO, "", "");
                ids.add(semCpf.getId());
            }
            controller.excluir(pessoa.getId());
            ids.remove(Integer.valueOf(pessoa.getId()));
            exigir(dao.buscarPorId(pessoa.getId()) == null, "Pessoa sem historico nao foi excluida");
            System.out.println("APSPessoasCheck: cadastro, consulta, edicao, contato e CPF unico OK");
        } finally {
            for (int id : ids) dao.excluir(id);
        }
    }

    private static void exigir(boolean ok, String mensagem) {
        if (!ok) throw new AssertionError(mensagem);
    }
}
