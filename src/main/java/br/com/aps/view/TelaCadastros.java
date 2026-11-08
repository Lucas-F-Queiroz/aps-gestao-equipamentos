package br.com.aps.view;

import java.awt.BorderLayout;
import javax.swing.JPanel;

/** Cadastro com seletor deslizante entre pessoas e equipamentos. */
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
        mostrar(1);
    }

    private void mostrar(int numero) {
        area.removeAll();
        area.add(numero == 0 ? new CadastroPessoas() : new CadastroEquipamentos(), BorderLayout.CENTER);
        area.revalidate();
        area.repaint();
    }
}
