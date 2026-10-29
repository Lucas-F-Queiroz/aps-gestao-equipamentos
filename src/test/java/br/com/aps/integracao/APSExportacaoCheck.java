package br.com.aps.integracao;

import br.com.aps.view.ExportadorRelatorio;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.atomic.AtomicInteger;
import javax.swing.JTable;
import javax.swing.SwingUtilities;

/** Confirma que cancelar uma substituicao nunca altera o arquivo anterior. */
public final class APSExportacaoCheck {
    public static void main(String[] args) throws Exception {
        Path pasta = Files.createTempDirectory("aps-exportacao-");
        Path arquivo = pasta.resolve("relatorio.csv");
        try {
            SwingUtilities.invokeAndWait(() -> {
                try {
                    JTable tabela = new JTable(new Object[][]{{"Ana; \"Teste\"", "=1+1"}},
                            new String[]{"Nome", "Observação"});
                    AtomicInteger confirmacoes = new AtomicInteger();
                    exigir(ExportadorRelatorio.salvar(tabela, arquivo, () -> {
                        confirmacoes.incrementAndGet();
                        return false;
                    }), "Arquivo novo deve ser salvo");
                    exigir(confirmacoes.get() == 0, "Arquivo novo nao deve pedir substituicao");
                    exigir(Files.readString(arquivo).contains("\"Ana; \"\"Teste\"\"\";\"'=1+1\""),
                            "CSV perdeu escape ou protecao contra formula");

                    Files.writeString(arquivo, "RELATORIO ANTERIOR");
                    exigir(!ExportadorRelatorio.salvar(tabela, arquivo, () -> {
                        confirmacoes.incrementAndGet();
                        return false;
                    }), "Cancelar deve interromper a exportacao");
                    exigir(confirmacoes.get() == 1 && "RELATORIO ANTERIOR".equals(Files.readString(arquivo)),
                            "Cancelamento alterou o arquivo");
                    try {
                        ExportadorRelatorio.salvar(tabela, arquivo);
                        throw new AssertionError("Gravacao sem autorizacao substituiu arquivo existente");
                    } catch (FileAlreadyExistsException esperado) {
                        exigir("RELATORIO ANTERIOR".equals(Files.readString(arquivo)),
                                "Tentativa sem autorizacao alterou o arquivo");
                    }
                    exigir(ExportadorRelatorio.salvar(tabela, arquivo, () -> true), "Confirmacao deve permitir salvar");
                    exigir(!Files.readString(arquivo).contains("RELATORIO ANTERIOR"), "Arquivo confirmado nao foi substituido");
                } catch (java.io.IOException erro) {
                    throw new AssertionError(erro);
                }
            });
        } finally {
            Files.deleteIfExists(arquivo);
            Files.delete(pasta);
        }
        System.out.println("APSExportacaoCheck: criacao, cancelamento e substituicao confirmada passaram.");
    }

    private static void exigir(boolean condicao, String mensagem) {
        if (!condicao) throw new AssertionError(mensagem);
    }
}
