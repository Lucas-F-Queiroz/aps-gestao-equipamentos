package br.com.aps.integracao;

import br.com.aps.view.Estilo;
import br.com.aps.view.ExportadorRelatorio;
import br.com.aps.view.FiltroTabela;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JTable;
import javax.swing.RowSorter;
import javax.swing.SortOrder;

/**
 * Confere a ordenacao pelo cabecalho, a barra de filtros e a exportacao CSV das tabelas.
 * Nao precisa de banco de dados nem de janela: usa so uma tabela Swing em memoria.
 */
public final class APSFiltroCheck {
    private APSFiltroCheck() {
    }

    public static void main(String[] args) {
        JTable tabela = Estilo.criarTabela(new String[] {"ID", "Nome", "Categoria", "Retirada", "Situação"});
        Estilo.modelo(tabela).addRow(new Object[] {2, "Álvaro", "Notebook", "05/10/2026 10:00", "Aberto"});
        Estilo.modelo(tabela).addRow(new Object[] {10, "alice", "Celular", "20/09/2026 09:30", "Devolvido"});
        Estilo.modelo(tabela).addRow(new Object[] {1, "Bruno", "Notebook", "27/10/2026 14:30", "Atrasado"});
        Estilo.modelo(tabela).addRow(new Object[] {"3", "Carla", "Monitor", "-", "Aberto"});

        FiltroTabela filtro = new FiltroTabela(tabela);
        filtro.adicionarFiltro("Categoria", 2);
        filtro.adicionarPeriodo("Retirada", 3);
        filtro.atualizar();

        // ---------- ordenacao (o que acontece ao clicar no cabecalho)
        ordenar(tabela, 0, SortOrder.ASCENDING);
        exigir(coluna(tabela, 0).equals(List.of("1", "2", "3", "10")), "ID deve ordenar como numero (1, 2, 3, 10): " + coluna(tabela, 0));
        ordenar(tabela, 0, SortOrder.DESCENDING);
        exigir(coluna(tabela, 0).equals(List.of("10", "3", "2", "1")), "ID decrescente: " + coluna(tabela, 0));
        ordenar(tabela, 3, SortOrder.ASCENDING);
        exigir(coluna(tabela, 1).equals(List.of("Carla", "alice", "Álvaro", "Bruno")),
            "datas devem ordenar como data (20/09, 05/10, 27/10) e o '-' vir primeiro: " + coluna(tabela, 1));
        ordenar(tabela, 1, SortOrder.ASCENDING);
        exigir(coluna(tabela, 1).equals(List.of("alice", "Álvaro", "Bruno", "Carla")),
            "nomes ignoram maiusculas e acentos: " + coluna(tabela, 1));

        // ---------- pesquisa em todos os campos
        filtro.pesquisar("alvaro");
        exigir(tabela.getRowCount() == 1, "'alvaro' deve achar 'Álvaro' (sem acento): " + tabela.getRowCount());
        filtro.pesquisar("NOTEBOOK");
        exigir(tabela.getRowCount() == 2, "'NOTEBOOK' acha 2 notebooks, em maiusculas tambem: " + tabela.getRowCount());
        filtro.pesquisar("notebook atrasado");
        exigir(tabela.getRowCount() == 1, "varias palavras: todas precisam aparecer na linha: " + tabela.getRowCount());
        filtro.pesquisar("27/10");
        exigir(tabela.getRowCount() == 1, "a pesquisa tambem olha as colunas de data: " + tabela.getRowCount());
        filtro.pesquisar("xyz");
        exigir(tabela.getRowCount() == 0, "texto inexistente nao acha nada");
        filtro.limparFiltros();
        exigir(tabela.getRowCount() == 4, "limpar filtros devolve todas as linhas");

        // ---------- lista de filtro por coluna
        filtro.escolher(2, "Celular");
        exigir(tabela.getRowCount() == 1 && "alice".equals(tabela.getValueAt(0, 1)), "filtro Categoria = Celular");
        filtro.pesquisar("zzz");
        filtro.escolher(2, "Todos");
        filtro.limparFiltros();

        // ---------- periodo
        filtro.definirPeriodo(3, "01/10/2026", "31/10/2026");
        exigir(tabela.getRowCount() == 2, "periodo de outubro: 05/10 e 27/10 (o '-' e o de setembro saem): " + tabela.getRowCount());
        filtro.definirPeriodo(3, "06/10/2026", "");
        exigir(tabela.getRowCount() == 1, "so a data inicial (a partir de 06/10): " + tabela.getRowCount());
        filtro.definirPeriodo(3, "", "05/10/2026");
        exigir(tabela.getRowCount() == 2, "so a data final (ate 05/10): 20/09 e 05/10: " + tabela.getRowCount());
        filtro.definirPeriodo(3, "31/02/2026", "");
        exigir(tabela.getRowCount() == 4, "data impossivel e ignorada (campo fica vermelho): " + tabela.getRowCount());
        filtro.limparFiltros();

        // ---------- linha selecionada = posicao no MODELO (a lista de objetos da tela)
        ordenar(tabela, 0, SortOrder.DESCENDING);
        tabela.setRowSelectionInterval(0, 0); // primeira linha VISIVEL = maior ID (10), que esta na posicao 1 do modelo
        exigir(Estilo.linhaSelecionada(tabela) == 1, "linhaSelecionada converte para o modelo: " + Estilo.linhaSelecionada(tabela));
        filtro.pesquisar("bruno");
        tabela.setRowSelectionInterval(0, 0);
        exigir(Estilo.linhaSelecionada(tabela) == 2, "com filtro ativo tambem converte: " + Estilo.linhaSelecionada(tabela));
        tabela.clearSelection();
        exigir(Estilo.linhaSelecionada(tabela) == -1, "sem selecao devolve -1");

        // ---------- exportar CSV: sai exatamente o que a tabela mostra (filtro e ordem aplicados)
        filtro.limparFiltros();
        ordenar(tabela, 0, SortOrder.ASCENDING);
        filtro.pesquisar("notebook");
        try {
            Path arquivo = Files.createTempFile("aps-relatorio", ".csv");
            ExportadorRelatorio.salvar(tabela, arquivo, () -> true);
            List<String> linhasDoArquivo = Files.readAllLines(arquivo);
            Files.delete(arquivo);
            exigir(linhasDoArquivo.size() == 3, "CSV: cabecalho + 2 notebooks filtrados: " + linhasDoArquivo.size());
            exigir(linhasDoArquivo.get(0).startsWith("\uFEFF\"ID\";\"Nome\""), "CSV: cabecalho com as colunas da tabela");
            exigir(linhasDoArquivo.get(1).startsWith("\"1\";") && linhasDoArquivo.get(2).startsWith("\"2\";"),
                "CSV: respeita a ordem da tela (ID 1 antes do 2)");
        } catch (IOException erro) {
            throw new AssertionError("Falha ao exportar CSV: " + erro.getMessage());
        }

        System.out.println("APSFiltroCheck: ordenacao e filtros das tabelas OK");
    }

    private static void ordenar(JTable tabela, int coluna, SortOrder ordem) {
        tabela.getRowSorter().setSortKeys(List.of(new RowSorter.SortKey(coluna, ordem)));
    }

    /** Valores da coluna na ORDEM EM QUE O USUARIO VE as linhas. */
    private static List<String> coluna(JTable tabela, int coluna) {
        List<String> valores = new ArrayList<>();
        for (int linha = 0; linha < tabela.getRowCount(); linha++) {
            valores.add(String.valueOf(tabela.getValueAt(linha, coluna)));
        }
        return valores;
    }

    private static void exigir(boolean condicao, String mensagem) {
        if (!condicao) {
            throw new AssertionError(mensagem);
        }
    }
}
