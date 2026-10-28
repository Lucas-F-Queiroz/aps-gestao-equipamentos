package br.com.aps.view;

import java.awt.BorderLayout;
import javax.swing.JPanel;

/** Alterna formularios de equipamento (indice 0) e pessoa (indice 1). */
public class TelaCadastros extends JPanel {
    private final JPanel area = new JPanel(new BorderLayout());

    public TelaCadastros() {
        super(new BorderLayout(0, 0));
        setOpaque(false);
        JPanel navegacao = new JPanel(new BorderLayout());
        navegacao.setOpaque(false);
        navegacao.add(new SeletorCadastro(this::mostrar), BorderLayout.WEST);
        add(navegacao, BorderLayout.NORTH);
        area.setOpaque(false);
        add(area, BorderLayout.CENTER);
        mostrar(0);
    }

    private void mostrar(int numero) {
        area.removeAll();
        area.add(numero == 0 ? new CadastroEquipamentos() : new CadastroPessoas(), BorderLayout.CENTER);
        area.revalidate();
        area.repaint();
    }
}
