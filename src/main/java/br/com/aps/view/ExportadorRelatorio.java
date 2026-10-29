package br.com.aps.view;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.function.BooleanSupplier;

import javax.swing.JTable;

/** Salva em CSV exatamente o que a tabela mostra agora (com filtro e ordem aplicados). */
public final class ExportadorRelatorio {
    private ExportadorRelatorio() {
    }

    public static void salvar(JTable tabela, Path destino) throws IOException {
        gravar(tabela, destino, false);
    }

    public static boolean salvar(JTable tabela, Path destino, BooleanSupplier confirmarSubstituicao) throws IOException {
        boolean existe = Files.exists(destino);
        if (existe && !confirmarSubstituicao.getAsBoolean()) return false;
        gravar(tabela, destino, existe);
        return true;
    }

    private static void gravar(JTable tabela, Path destino, boolean substituir) throws IOException {
        try (var saida = Files.newBufferedWriter(destino, StandardCharsets.UTF_8,
                StandardOpenOption.WRITE,
                substituir ? StandardOpenOption.TRUNCATE_EXISTING : StandardOpenOption.CREATE_NEW)) {
            saida.write("\uFEFF"); // marca para o Excel entender os acentos
            for (int linha = -1; linha < tabela.getRowCount(); linha++) {
                for (int coluna = 0; coluna < tabela.getColumnCount(); coluna++) {
                    if (coluna > 0) saida.write(";");
                    Object valor = linha < 0 ? tabela.getColumnName(coluna) : tabela.getValueAt(linha, coluna);
                    String texto = valor == null ? "" : valor.toString();
                    // Evita que um texto cadastrado seja lido como formula ao abrir em planilha.
                    if (texto.stripLeading().matches("(?s)[=+@-].*")) texto = "'" + texto;
                    saida.write("\"" + texto.replace("\"", "\"\"") + "\"");
                }
                saida.write("\r\n");
            }
        }
    }
}
