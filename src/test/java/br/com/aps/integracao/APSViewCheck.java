package br.com.aps.integracao;

import br.com.aps.view.*;
import br.com.aps.util.DatabaseMigrator;
import java.awt.Component;
import java.awt.Container;
import javax.swing.*;

/** Sem reflexao nem dados criados; verifica se todas as telas e abas podem ser abertas. */
public class APSViewCheck {
    public static void main(String[] args) throws Exception {
        DatabaseMigrator.migrate();
        SwingUtilities.invokeAndWait(() -> {
            Estilo.instalar();
            // Confere as tres areas de navegacao e o retorno ao inicio.
            JanelaPrincipal janela = new JanelaPrincipal();
            for (int area = 1; area <= 3; area++) {
                janela.mostrar(area);
                abrirTodasAsAbas(janela); // cada aba e criada ao ser aberta: erro de tela aparece aqui
            }
            janela.mostrar(0);
            janela.dispose();

            JPanel[] telas = {
                new TelaInicio(new JanelaPrincipal(1)),
                new TelaEquipamentos(), new TelaCadastros(),
                new TelaConsultar(), new TelaDevolucao(),
                new TelaRelatorios(), new TelaEmprestimos()
            };
            for (JPanel tela : telas) {
                if (tela.getComponentCount() == 0) {
                    throw new AssertionError("Tela vazia: " + tela.getClass().getSimpleName());
                }
            }
            System.out.println("APSViewCheck: telas Swing criadas com sucesso.");
        });
        System.exit(0);
    }

    /** Seleciona, uma a uma, as abas (JTabbedPane) que estiverem dentro do componente. */
    private static void abrirTodasAsAbas(Container componente) {
        for (Component filho : componente.getComponents()) {
            if (filho instanceof JTabbedPane abas) {
                for (int i = 0; i < abas.getTabCount(); i++) {
                    abas.setSelectedIndex(i);
                }
            }
            if (filho instanceof Container interno) {
                abrirTodasAsAbas(interno);
            }
        }
    }
}
