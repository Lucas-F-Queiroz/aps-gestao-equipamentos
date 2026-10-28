package br.com.aps.integracao;

import br.com.aps.view.*;
import br.com.aps.util.DatabaseMigrator;
import javax.swing.*;

/** Sem reflexao nem dados criados; verifica se todas as telas podem ser abertas. */
public class APSViewCheck {
    public static void main(String[] args) throws Exception {
        DatabaseMigrator.migrate();
        SwingUtilities.invokeAndWait(() -> {
            Estilo.instalar();
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
}
