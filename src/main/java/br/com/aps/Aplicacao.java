package br.com.aps;

import br.com.aps.controller.EmprestimoController;
import br.com.aps.controller.EquipamentoController;
import br.com.aps.controller.UsuarioController;
import br.com.aps.util.DatabaseMigrator;
import br.com.aps.view.Estilo;
import br.com.aps.view.JanelaPrincipal;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

/** Inicializa as tabelas com Flyway e abre a interface Swing. */
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
                "Nao foi possivel conectar ao MySQL ou preparar o banco.\n"
                + "Confira se o MySQL esta iniciado e se application.properties esta configurado.\n\n"
                + erro.getMessage(), "Erro ao iniciar", JOptionPane.ERROR_MESSAGE));
            return;
        }
        SwingUtilities.invokeLater(() -> {
            Estilo.instalar();
            new JanelaPrincipal().setVisible(true);
        });
    }
}
