package br.com.aps.view;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.text.Normalizer;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.ResolverStyle;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.TreeSet;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.RowFilter;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;

/**
 * Barra de busca e filtros para uma JTable. Todas as tabelas do sistema usam esta mesma classe.
 *
 *   - Pesquisar: procura o texto em TODAS as colunas (sem diferenciar maiusculas e acentos;
 *     varias palavras: todas precisam aparecer na mesma linha).
 *   - Filtro de coluna: lista suspensa com os valores que existem na coluna (ex.: Categoria).
 *   - Periodo: data inicial e final (dd/mm/aaaa) sobre uma coluna de data.
 *   - Os dados nao sao alterados: so as linhas mostradas (RowFilter do Swing).
 *
 * Como usar na tela:
 *   FiltroTabela filtro = new FiltroTabela(tabela);
 *   filtro.adicionarFiltro("Categoria", 3);
 *   filtro.adicionarPeriodo("Data do empréstimo", 4);
 *   ... depois de CARREGAR a tabela com dados novos: filtro.atualizar();
 */
public final class FiltroTabela extends JPanel {
    private static final String TODOS = "Todos";
    private static final DateTimeFormatter DATA =
        DateTimeFormatter.ofPattern("dd/MM/uuuu").withResolverStyle(ResolverStyle.STRICT);

    /** Um filtro por lista suspensa, ligado a uma coluna da tabela. */
    private static final class FiltroDeColuna {
        final int coluna;
        final JComboBox<String> lista = new JComboBox<>();

        FiltroDeColuna(int coluna) {
            this.coluna = coluna;
        }
    }

    /** Um periodo (De / Ate), ligado a uma coluna de data da tabela. */
    private static final class Periodo {
        final int coluna;
        final JTextField de = new JTextField();
        final JTextField ate = new JTextField();

        Periodo(int coluna) {
            this.coluna = coluna;
        }
    }

    private final JTable tabela;
    private final TableRowSorter<TableModel> ordenador;
    private final JTextField busca = new JTextField();
    private final JLabel contador = new JLabel(" ");
    private final JPanel painelFiltros = new JPanel(new GridLayout(1, 0, 12, 0));
    private final JPanel painelExtras = new JPanel(new GridLayout(1, 0, 12, 0));
    private final JPanel painelBotoes = new JPanel(new GridLayout(1, 0, 10, 0)); // "Limpar filtros" + botoes da tela
    private final List<FiltroDeColuna> filtros = new ArrayList<>();
    private final List<Periodo> periodos = new ArrayList<>();
    private boolean atualizando; // evita refazer o filtro varias vezes enquanto as listas sao recarregadas

    @SuppressWarnings("unchecked")
    public FiltroTabela(JTable tabela) {
        super(new BorderLayout(0, 10));
        this.tabela = tabela;
        this.ordenador = (TableRowSorter<TableModel>) tabela.getRowSorter();
        setOpaque(false);

        busca.putClientProperty("JTextField.placeholderText", "Pesquisar em todos os campos da tabela");
        busca.putClientProperty("JTextField.leadingIcon", new IconeAcao("busca", Estilo.TEXTO_SUAVE));
        busca.putClientProperty("JTextField.showClearButton", true);
        aoMudar(busca, this::aplicar);

        painelFiltros.setOpaque(false);
        painelExtras.setOpaque(false);

        // Linha de cima: campo de pesquisa (ocupa o espaco que sobrar) + listas de filtro
        JPanel linhaDeCima = new JPanel(new BorderLayout(12, 0));
        linhaDeCima.setOpaque(false);
        linhaDeCima.add(Estilo.campo("Pesquisar", busca), BorderLayout.CENTER);
        linhaDeCima.add(painelFiltros, BorderLayout.EAST);

        // Linha de baixo: periodos e campos extras (esquerda) + contador e botao Limpar (direita)
        JButton limpar = Estilo.botao("Limpar filtros");
        limpar.addActionListener(e -> limparFiltros());
        contador.setForeground(Estilo.TEXTO_SUAVE);
        painelBotoes.setOpaque(false);
        painelBotoes.add(limpar);
        JPanel direita = new JPanel(new BorderLayout(14, 0));
        direita.setOpaque(false);
        direita.add(Estilo.campo(" ", contador), BorderLayout.WEST);
        direita.add(Estilo.campo(" ", painelBotoes), BorderLayout.EAST);
        JPanel linhaDeBaixo = new JPanel(new BorderLayout(12, 0));
        linhaDeBaixo.setOpaque(false);
        linhaDeBaixo.add(painelExtras, BorderLayout.WEST);
        linhaDeBaixo.add(direita, BorderLayout.EAST);

        add(linhaDeCima, BorderLayout.NORTH);
        add(linhaDeBaixo, BorderLayout.CENTER);
        aplicar();
    }

    // ------------------------------------------------------------ montagem da barra

    /** Lista suspensa com os valores da coluna. Ex.: adicionarFiltro("Categoria", 3). */
    public FiltroTabela adicionarFiltro(String rotulo, int coluna) {
        FiltroDeColuna filtro = new FiltroDeColuna(coluna);
        filtro.lista.addItem(TODOS);
        filtro.lista.addActionListener(e -> aplicar());
        filtros.add(filtro);
        painelFiltros.add(Estilo.campo(rotulo, filtro.lista));
        return this;
    }

    /** Dois campos de data (De / Ate) com calendario, filtrando uma coluna de data. */
    public FiltroTabela adicionarPeriodo(String rotulo, int coluna) {
        Periodo periodo = new Periodo(coluna);
        periodo.de.putClientProperty("JTextField.placeholderText", "dd/mm/aaaa");
        periodo.ate.putClientProperty("JTextField.placeholderText", "dd/mm/aaaa");
        aoMudar(periodo.de, this::aplicar);
        aoMudar(periodo.ate, this::aplicar);
        periodos.add(periodo);
        painelExtras.add(Estilo.campo(rotulo + " - de", new CalendarioData(periodo.de)));
        painelExtras.add(Estilo.campo("até", new CalendarioData(periodo.ate)));
        return this;
    }

    /** Coloca qualquer componente (ex.: uma lista "Ordenar por") na linha de baixo da barra. */
    public FiltroTabela adicionarCampo(String rotulo, JComponent componente) {
        painelExtras.add(Estilo.campo(rotulo, componente));
        return this;
    }

    /** Coloca um botao da tela (ex.: "Alterar situacao") ao lado de "Limpar filtros". */
    public FiltroTabela adicionarBotao(JButton botao) {
        painelBotoes.add(botao);
        return this;
    }

    // ------------------------------------------------------------ uso

    /**
     * Chame depois de carregar a tabela com dados novos: refaz as listas de filtro com os
     * valores que existem agora (mantendo a escolha, se ela ainda existir) e reaplica o filtro.
     */
    public void atualizar() {
        atualizando = true;
        try {
            for (FiltroDeColuna filtro : filtros) {
                Object escolhido = filtro.lista.getSelectedItem();
                TreeSet<String> valores = new TreeSet<>(String.CASE_INSENSITIVE_ORDER);
                TableModel modelo = tabela.getModel();
                for (int linha = 0; linha < modelo.getRowCount(); linha++) {
                    String valor = String.valueOf(modelo.getValueAt(linha, filtro.coluna));
                    if (!valor.isBlank() && !valor.equals("-")) {
                        valores.add(valor);
                    }
                }
                filtro.lista.removeAllItems();
                filtro.lista.addItem(TODOS);
                for (String valor : valores) {
                    filtro.lista.addItem(valor);
                }
                filtro.lista.setSelectedItem(escolhido != null && valores.contains(escolhido.toString())
                    ? escolhido : TODOS);
            }
        } finally {
            atualizando = false;
        }
        aplicar();
    }

    /** Volta tudo ao normal: sem texto, sem filtros e sem periodos. */
    public void limparFiltros() {
        atualizando = true;
        busca.setText("");
        for (FiltroDeColuna filtro : filtros) {
            filtro.lista.setSelectedItem(TODOS);
        }
        for (Periodo periodo : periodos) {
            periodo.de.setText("");
            periodo.ate.setText("");
        }
        atualizando = false;
        aplicar();
    }

    // Atalhos usados tambem pelos testes automaticos.

    public void pesquisar(String texto) {
        busca.setText(texto);
    }

    public void escolher(int coluna, String valor) {
        for (FiltroDeColuna filtro : filtros) {
            if (filtro.coluna == coluna) {
                filtro.lista.setSelectedItem(valor);
            }
        }
    }

    public void definirPeriodo(int coluna, String de, String ate) {
        for (Periodo periodo : periodos) {
            if (periodo.coluna == coluna) {
                periodo.de.setText(de);
                periodo.ate.setText(ate);
            }
        }
    }

    // ------------------------------------------------------------ o filtro em si

    /** Monta o filtro com o que esta escolhido na tela e entrega ao ordenador da tabela. */
    private void aplicar() {
        if (atualizando) {
            return;
        }

        String texto = normalizar(busca.getText()).trim();
        final String[] palavras = texto.isEmpty() ? new String[0] : texto.split("\\s+");

        final int[] colunasDasListas = new int[filtros.size()];
        final String[] valoresDasListas = new String[filtros.size()];
        for (int i = 0; i < filtros.size(); i++) {
            colunasDasListas[i] = filtros.get(i).coluna;
            Object escolhido = filtros.get(i).lista.getSelectedItem();
            valoresDasListas[i] = escolhido == null || TODOS.equals(escolhido) ? null : escolhido.toString();
        }

        final int[] colunasDosPeriodos = new int[periodos.size()];
        final LocalDate[] inicios = new LocalDate[periodos.size()];
        final LocalDate[] fins = new LocalDate[periodos.size()];
        for (int i = 0; i < periodos.size(); i++) {
            Periodo periodo = periodos.get(i);
            colunasDosPeriodos[i] = periodo.coluna;
            inicios[i] = lerData(periodo.de);
            fins[i] = lerData(periodo.ate);
        }

        ordenador.setRowFilter(new RowFilter<TableModel, Integer>() {
            @Override
            public boolean include(Entry<? extends TableModel, ? extends Integer> linha) {
                // 1) listas de filtro: o valor da coluna precisa ser igual ao escolhido
                for (int i = 0; i < colunasDasListas.length; i++) {
                    if (valoresDasListas[i] != null
                            && !valoresDasListas[i].equals(linha.getStringValue(colunasDasListas[i]))) {
                        return false;
                    }
                }
                // 2) periodos: a data da coluna precisa estar entre o inicio e o fim
                for (int i = 0; i < colunasDosPeriodos.length; i++) {
                    if (inicios[i] == null && fins[i] == null) {
                        continue;
                    }
                    LocalDate dia = diaDaCelula(linha.getStringValue(colunasDosPeriodos[i]));
                    if (dia == null || (inicios[i] != null && dia.isBefore(inicios[i]))
                            || (fins[i] != null && dia.isAfter(fins[i]))) {
                        return false;
                    }
                }
                // 3) texto: cada palavra precisa aparecer em alguma coluna da linha
                if (palavras.length > 0) {
                    StringBuilder linhaInteira = new StringBuilder();
                    for (int coluna = 0; coluna < linha.getValueCount(); coluna++) {
                        linhaInteira.append(normalizar(linha.getStringValue(coluna))).append(' ');
                    }
                    for (String palavra : palavras) {
                        if (!linhaInteira.toString().contains(palavra)) {
                            return false;
                        }
                    }
                }
                return true;
            }
        });

        contador.setText(tabela.getRowCount() + " de " + tabela.getModel().getRowCount() + " registro(s)");
    }

    /** Le a data de um campo; devolve null se estiver vazio ou invalido (e marca o campo em vermelho). */
    private static LocalDate lerData(JTextField campo) {
        String texto = campo.getText().trim();
        if (texto.isEmpty()) {
            campo.putClientProperty("JComponent.outline", null);
            return null;
        }
        try {
            LocalDate data = LocalDate.parse(texto, DATA);
            campo.putClientProperty("JComponent.outline", null);
            return data;
        } catch (DateTimeParseException erro) {
            campo.putClientProperty("JComponent.outline", "error");
            return null;
        }
    }

    /** "27/10/2026 14:30" -> 27/10/2026. Devolve null se a celula nao comeca com uma data. */
    private static LocalDate diaDaCelula(String celula) {
        if (celula == null || celula.length() < 10) {
            return null;
        }
        try {
            return LocalDate.parse(celula.substring(0, 10), DATA);
        } catch (DateTimeParseException erro) {
            return null;
        }
    }

    /** Minusculas e sem acento: "Notebook" e "NOTEBOOK" e "Nótebook" ficam iguais na busca. */
    public static String normalizar(String texto) {
        if (texto == null) {
            return "";
        }
        return Normalizer.normalize(texto, Normalizer.Form.NFD)
            .replaceAll("\\p{M}", "").toLowerCase(Locale.ROOT);
    }

    /** Executa a acao toda vez que o texto do campo muda (digitou, apagou ou colou). */
    private static void aoMudar(JTextField campo, Runnable acao) {
        campo.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent evento) {
                acao.run();
            }

            @Override
            public void removeUpdate(DocumentEvent evento) {
                acao.run();
            }

            @Override
            public void changedUpdate(DocumentEvent evento) {
                acao.run();
            }
        });
    }
}
