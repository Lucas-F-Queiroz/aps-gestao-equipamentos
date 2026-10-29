package br.com.aps.view;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.Font;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

/**
 * Janela "modal": aparece por cima da tela e so volta ao sistema quando for fechada.
 * Monte com titulo, campos e botoes:
 *
 *   Modal modal = new Modal(this, "Editar pessoa");
 *   modal.campo("Nome", campoNome);
 *   boolean salvou = modal.exibir(() -> controller.atualizar(...));   // botoes Cancelar e Salvar
 *
 * Se a acao do Salvar lancar erro (regra de negocio), o aviso aparece e a janela continua aberta.
 */
final class Modal extends JDialog {
    private final JPanel conteudo = new JPanel();
    private final JPanel botoes = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
    private boolean salvou;

    Modal(Component pai, String titulo) {
        super(SwingUtilities.getWindowAncestor(pai), titulo, ModalityType.APPLICATION_MODAL);

        JLabel cabecalho = new JLabel(titulo);
        cabecalho.setFont(cabecalho.getFont().deriveFont(Font.BOLD, 18f));
        conteudo.setLayout(new BoxLayout(conteudo, BoxLayout.Y_AXIS)); // um componente embaixo do outro
        conteudo.setOpaque(false);
        botoes.setOpaque(false);

        JPanel raiz = new JPanel(new BorderLayout(0, 16));
        raiz.setBackground(Color.WHITE);
        raiz.setBorder(BorderFactory.createEmptyBorder(20, 24, 18, 24));
        raiz.add(cabecalho, BorderLayout.NORTH);
        raiz.add(conteudo, BorderLayout.CENTER);
        raiz.add(botoes, BorderLayout.SOUTH);
        setContentPane(raiz);
        setResizable(false);
    }

    /** Campo de formulario (rotulo em cima, componente embaixo). */
    Modal campo(String rotulo, JComponent componente) {
        return componente(Estilo.campo(rotulo, componente));
    }

    /** Linha de informacao somente leitura: "Patrimonio: PAT-005". */
    Modal info(String rotulo, String valor) {
        return componente(new JLabel("<html><span style='color:#64748B'>" + rotulo + ":</span>&nbsp; <b>"
            + valor.replace("&", "&amp;").replace("<", "&lt;") + "</b></html>"));
    }

    /** Linha com um selo colorido: "Status atual: [Indisponivel]". */
    Modal selo(String rotulo, String situacao) {
        JPanel linha = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        linha.setOpaque(false);
        JLabel texto = new JLabel(rotulo + ":");
        texto.setForeground(Estilo.TEXTO_SUAVE);
        linha.add(texto);
        linha.add(Estilo.selo(situacao));
        return componente(linha);
    }

    /** Qualquer componente (ex.: abas ou uma tabela). */
    Modal componente(JComponent componente) {
        componente.setAlignmentX(LEFT_ALIGNMENT);
        conteudo.add(componente);
        conteudo.add(Box.createVerticalStrut(10));
        return this;
    }

    /** Mostra com Cancelar e Salvar. Devolve true se salvou com sucesso. */
    boolean exibir(Runnable aoSalvar) {
        JButton cancelar = Estilo.botao("Cancelar");
        JButton salvar = Estilo.botaoPrincipal("Salvar");
        cancelar.addActionListener(evento -> dispose());
        salvar.addActionListener(evento -> {
            try {
                aoSalvar.run();
                salvou = true;
                dispose();
            } catch (RuntimeException erro) {
                Mensagens.erro(this, erro);
            }
        });
        botoes.add(cancelar);
        botoes.add(salvar);
        abrir();
        return salvou;
    }

    /** Mostra so com o botao Fechar (para consultar informacoes). */
    void exibirSomenteLeitura() {
        JButton fechar = Estilo.botao("Fechar");
        fechar.addActionListener(evento -> dispose());
        botoes.add(fechar);
        abrir();
    }

    private void abrir() {
        pack();
        setSize(Math.max(getWidth(), 480), getHeight());
        setLocationRelativeTo(getOwner());
        setVisible(true); // modal: o codigo so continua depois que a janela fechar
    }
}
