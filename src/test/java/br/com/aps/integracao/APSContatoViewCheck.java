package br.com.aps.integracao;

import br.com.aps.controller.UsuarioController;
import br.com.aps.dao.UsuarioDAO;
import br.com.aps.model.Usuario;
import br.com.aps.util.DatabaseMigrator;
import br.com.aps.view.CadastroPessoas;
import br.com.aps.view.ExportadorRelatorio;
import br.com.aps.view.TelaRelatorios;
import java.awt.Component;
import java.awt.Container;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

/** Conferencia funcional das tabelas Swing com dados sinteticos, sem reflexao. */
public final class APSContatoViewCheck {
    public static void main(String[] args) throws Exception {
        DatabaseMigrator.migrate();
        UsuarioController controller = new UsuarioController();
        String tag = UUID.randomUUID().toString();
        Usuario pessoa = controller.cadastrar("Contato " + tag, tag + "@example.invalid",
                Usuario.TIPO_ALUNO, "01234567890", "11912345678");
        Usuario legado = null;
        try {
            legado = controller.cadastrar("Sem contato " + tag, "legado." + tag + "@example.invalid",
                    Usuario.TIPO_FUNCIONARIO, "", "");
            final int idLegado = legado.getId();
            SwingUtilities.invokeAndWait(() -> {
                CadastroPessoas cadastro = new CadastroPessoas();
                JTable tabela = tabela(cadastro);
                selecionar(tabela, pessoa.getId());
                List<String> textos = textos(cadastro);
                exigir(textos.contains("01234567890") && textos.contains("11912345678"),
                        "Selecionar cadastro nao preenche CPF e celular");
                selecionar(tabela, idLegado);
                textos = textos(cadastro);
                exigir(!textos.contains("01234567890") && !textos.contains("11912345678"),
                        "Selecionar cadastro sem contato deixou valores da outra pessoa");

                JTable relatorio = tabela(new TelaRelatorios());
                selecionar(relatorio, pessoa.getId());
                int row = relatorio.getSelectedRow();
                exigir("01234567890".equals(relatorio.getValueAt(row, relatorio.getColumn("CPF").getModelIndex()))
                        && "11912345678".equals(relatorio.getValueAt(row, relatorio.getColumn("Celular").getModelIndex())),
                        "Relatorio nao mostrou contatos");
                Path csv = null;
                try {
                    csv = Files.createTempFile("aps-contatos-ficticios", ".csv");
                    ExportadorRelatorio.salvar(relatorio, csv, () -> true);
                    String conteudo = Files.readString(csv);
                    exigir(conteudo.contains("CPF") && conteudo.contains("Celular")
                            && conteudo.contains("01234567890") && conteudo.contains("11912345678"),
                            "Exportacao nao incluiu os contatos");
                } catch (java.io.IOException e) {
                    throw new AssertionError(e);
                } finally {
                    if (csv != null) {
                        try { Files.deleteIfExists(csv); }
                        catch (java.io.IOException e) { throw new AssertionError(e); }
                    }
                }
            });
            System.out.println("APSContatoViewCheck: selecao, campos opcionais e relatorio CSV OK");
        } finally {
            new UsuarioDAO().excluir(pessoa.getId());
            if (legado != null) new UsuarioDAO().excluir(legado.getId());
        }
    }

    private static JTable tabela(Container pai) {
        for (Component filho : pai.getComponents()) {
            if (filho instanceof JTable t) {
                for (int col = 0; col < t.getColumnCount(); col++) {
                    if ("CPF".equals(t.getColumnName(col))) return t;
                }
            }
            if (filho instanceof Container c) {
                JTable encontrada = tabela(c);
                if (encontrada != null) return encontrada;
            }
        }
        return null;
    }

    private static List<String> textos(Container pai) {
        List<String> resultado = new ArrayList<>();
        for (Component filho : pai.getComponents()) {
            if (filho instanceof JTextField t) resultado.add(t.getText());
            if (filho instanceof Container c) resultado.addAll(textos(c));
        }
        return resultado;
    }

    private static void selecionar(JTable tabela, int id) {
        exigir(tabela != null, "Tabela com CPF nao encontrada");
        for (int row = 0; row < tabela.getRowCount(); row++) {
            if (Integer.valueOf(id).equals(tabela.getValueAt(row, 0))) {
                tabela.setRowSelectionInterval(row, row);
                return;
            }
        }
        throw new AssertionError("Pessoa nao encontrada na tabela");
    }

    private static void exigir(boolean ok, String mensagem) {
        if (!ok) throw new AssertionError(mensagem);
    }
}
