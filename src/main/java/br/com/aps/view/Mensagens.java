package br.com.aps.view;

import br.com.aps.controller.RegraNegocioException;

import java.awt.Component;

import javax.swing.JOptionPane;

/** Atalhos para as caixas de dialogo usadas pelas telas. */
final class Mensagens {

    private Mensagens() {
    }

    static void aviso(Component pai, String mensagem) {
        JOptionPane.showMessageDialog(pai, mensagem, "Atenção", JOptionPane.WARNING_MESSAGE);
    }

    static void info(Component pai, String mensagem) {
        JOptionPane.showMessageDialog(pai, mensagem, "TI Fácil", JOptionPane.INFORMATION_MESSAGE);
    }

    static boolean confirmar(Component pai, String mensagem) {
        return JOptionPane.showConfirmDialog(
            pai, mensagem, "Confirmar", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION;
    }

    /** Falha prevista vira aviso; detalhes inesperados ficam no log, nao na tela. */
    static void erro(Component pai, RuntimeException erro) {
        if (erro instanceof RegraNegocioException) {
            aviso(pai, erro.getMessage());
        } else {
            erro.printStackTrace();
            aviso(pai, "Ocorreu um erro inesperado. Verifique os registros da aplicação.");
        }
    }
}
