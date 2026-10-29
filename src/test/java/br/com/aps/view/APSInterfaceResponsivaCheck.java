package br.com.aps.view;

import br.com.aps.controller.RegraNegocioException;
import java.awt.Window;
import java.util.concurrent.atomic.AtomicInteger;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

/** A tarefa espera um evento Swing: so termina se a EDT continuar processando eventos. */
public final class APSInterfaceResponsivaCheck {
    public static void main(String[] args) throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            JPanel pai = new JPanel();
            AtomicInteger eventos = new AtomicInteger();
            int resultado = TarefaBanco.consultar(pai, () -> {
                if (SwingUtilities.isEventDispatchThread()) throw new AssertionError("Operacao executada na EDT");
                try {
                    SwingUtilities.invokeAndWait(eventos::incrementAndGet);
                } catch (Exception erro) {
                    throw new AssertionError(erro);
                }
                return 42;
            });
            if (resultado != 42 || eventos.get() != 1) throw new AssertionError("Interface bloqueou ou perdeu resultado");

            RegraNegocioException esperado = new RegraNegocioException("Falha de teste");
            try {
                TarefaBanco.executar(pai, () -> { throw esperado; });
                throw new AssertionError("Falha da operacao foi ignorada");
            } catch (RegraNegocioException recebido) {
                if (recebido != esperado) throw new AssertionError("Erro original nao foi preservado");
            }
            for (Window janela : Window.getWindows()) {
                if (janela.isDisplayable()) throw new AssertionError("Janela de espera ficou ativa: " + janela);
            }
        });
        System.out.println("APSInterfaceResponsivaCheck: eventos, resultado, erro e fechamento da espera passaram.");
        System.exit(0);
    }
}
