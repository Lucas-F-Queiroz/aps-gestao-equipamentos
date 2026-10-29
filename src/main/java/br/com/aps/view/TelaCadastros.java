package br.com.aps.view;

import java.awt.BorderLayout;

import javax.swing.JPanel;

/** Cadastros em duas abas: equipamentos (aba 0) e pessoas (aba 1). */
public class TelaCadastros extends JPanel {
    public TelaCadastros() {
        super(new BorderLayout());
        setOpaque(false);
        add(Estilo.criarAbas(new String[]{"Equipamentos", "Pessoas"},
            indice -> indice == 0 ? new CadastroEquipamentos() : new CadastroPessoas()), BorderLayout.CENTER);
    }
}
