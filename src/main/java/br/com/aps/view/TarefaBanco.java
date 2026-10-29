package br.com.aps.view;

import java.awt.Component;
import java.awt.Dialog;
import java.util.concurrent.ExecutionException;
import java.util.function.Supplier;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;

/** Mantem eventos e pintura ativos, sem permitir outra operacao durante uma gravacao. */
final class TarefaBanco {
    private TarefaBanco() { }

    static void executar(Component pai, Runnable operacao) {
        consultar(pai, () -> { operacao.run(); return null; });
    }

    // A operacao recebe somente dados capturados na EDT, nunca componentes Swing.
    static <T> T consultar(Component pai, Supplier<T> operacao) {
        if (!SwingUtilities.isEventDispatchThread()) return operacao.get();
        java.awt.Window dona = java.awt.KeyboardFocusManager.getCurrentKeyboardFocusManager().getActiveWindow();
        if (dona == null || !dona.isShowing()) dona = SwingUtilities.getWindowAncestor(pai);
        JDialog espera = new JDialog(dona,
                "Aguarde", Dialog.ModalityType.APPLICATION_MODAL);
        espera.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
        JPanel conteudo = new JPanel(new java.awt.BorderLayout(12, 12));
        conteudo.setBorder(javax.swing.BorderFactory.createEmptyBorder(20, 24, 20, 24));
        conteudo.add(new JLabel("Aguarde, processando…"), java.awt.BorderLayout.NORTH);
        JProgressBar progresso = new JProgressBar();
        progresso.setIndeterminate(true);
        conteudo.add(progresso);
        espera.setContentPane(conteudo);
        espera.pack();
        espera.setLocationRelativeTo(pai);
        SwingWorker<T, Void> tarefa = new SwingWorker<>() {
            @Override protected T doInBackground() { return operacao.get(); }
            @Override protected void done() { espera.dispose(); }
        };
        tarefa.execute();
        try {
            espera.setVisible(true);
            return tarefa.get();
        } catch (InterruptedException erro) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Operação interrompida.", erro);
        } catch (ExecutionException erro) {
            if (erro.getCause() instanceof RuntimeException causa) throw causa;
            if (erro.getCause() instanceof Error causa) throw causa;
            throw new IllegalStateException("Falha na operação do banco.", erro.getCause());
        } finally {
            espera.dispose();
        }
    }
}
