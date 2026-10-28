package br.com.aps;

import br.com.aps.controller.EmprestimoController;
import br.com.aps.controller.EquipamentoController;
import br.com.aps.controller.UsuarioController;
import br.com.aps.util.DatabaseMigrator;
import br.com.aps.view.Estilo;
import br.com.aps.view.JanelaPrincipal;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

/** Ponto de entrada: prepara o banco ANTES de abrir a interface Swing. */
public final class Aplicacao {
    private static final UsuarioController USUARIOS = new UsuarioController();
    private static final EquipamentoController EQUIPAMENTOS = new EquipamentoController();
    private static final EmprestimoController EMPRESTIMOS = new EmprestimoController();

    private Aplicacao() { }
    public static UsuarioController usuarios() { return USUARIOS; }
    public static EquipamentoController equipamentos() { return EQUIPAMENTOS; }
    public static EmprestimoController emprestimos() { return EMPRESTIMOS; }

    public static void main(String[] args) {
        try {
            DatabaseMigrator.migrate();
        } catch (RuntimeException erro) {
            erro.printStackTrace();
            SwingUtilities.invokeLater(() -> JOptionPane.showMessageDialog(null,
                "Não foi possível conectar ao MySQL ou preparar o banco.\n"
                + "Use iniciar.bat / iniciar.sh ou confira a configuração JDBC.\n\n"
                + erro.getMessage(), "Erro ao iniciar", JOptionPane.ERROR_MESSAGE));
            return;
        }
        SwingUtilities.invokeLater(() -> {
            Estilo.instalar();
            new JanelaPrincipal().setVisible(true);
        });
    }
}
