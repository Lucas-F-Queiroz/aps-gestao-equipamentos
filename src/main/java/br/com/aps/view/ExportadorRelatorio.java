package br.com.aps.view;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import javax.swing.table.TableModel;

/** Exporta exatamente as linhas filtradas exibidas no relatorio. */
public final class ExportadorRelatorio {
    private ExportadorRelatorio() {}
    public static void salvar(TableModel tabela, Path destino) throws IOException {
        try (var out = Files.newBufferedWriter(destino, StandardCharsets.UTF_8)) {
            out.write("\uFEFF");
            for (int linha = -1; linha < tabela.getRowCount(); linha++) {
                for (int coluna = 0; coluna < tabela.getColumnCount(); coluna++) {
                    if (coluna > 0) out.write(";");
                    Object valor = linha < 0 ? tabela.getColumnName(coluna) : tabela.getValueAt(linha, coluna);
                    String texto = valor == null ? "" : valor.toString();
                    // Evita interpretar texto cadastrado como formula ao abrir em uma planilha.
                    if (texto.stripLeading().matches("(?s)[=+@-].*")) texto = "'" + texto;
                    out.write("\"" + texto.replace("\"", "\"\"") + "\"");
                }
                out.write("\r\n");
            }
        }
    }
}
