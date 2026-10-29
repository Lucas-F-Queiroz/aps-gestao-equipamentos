package br.com.aps.view;

import br.com.aps.Aplicacao;
import br.com.aps.controller.RegraNegocioException;
import br.com.aps.controller.Regras;
import br.com.aps.model.Emprestimo;
import br.com.aps.model.EmprestimoDetalhe;
import br.com.aps.model.Equipamento;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextField;

/**
 * Cadastro de equipamentos. Em cima, o formulario; embaixo, a lista com busca, filtros e o menu "⋮"
 * de cada linha (Alterar status, Alterar estado, Editar dados, Ver historico, Excluir).
 * O status (Disponivel ou Indisponivel) muda na janela "Alterar equipamento"; Emprestado so muda
 * pela devolucao (o Controller recusa). Equipamento novo sempre nasce Disponivel.
 */
public class CadastroEquipamentos extends JPanel {
    private static final String[] OPCOES_STATUS = {"Disponível", "Indisponível"};

    private final JTextField patrimonio = new JTextField();
    private final JTextField nome = new JTextField();
    private final JTextField categoria = new JTextField();
    private final JComboBox<String> estado = new JComboBox<>(Regras.rotulos(Regras.ESTADOS));
    private final JTable tabela = Estilo.criarTabela(
        new String[]{"ID", "Patrimônio", "Nome", "Categoria", "Estado", "Status", "Ações"});
    private final List<Equipamento> linhas = new ArrayList<>();
    private final FiltroTabela filtro = new FiltroTabela(tabela);

    public CadastroEquipamentos() {
        super(new BorderLayout(0, 14));
        setOpaque(false);

        patrimonio.putClientProperty("JTextField.placeholderText", "Ex.: PAT-007");
        nome.putClientProperty("JTextField.placeholderText", "Ex.: Notebook Dell Latitude");
        categoria.putClientProperty("JTextField.placeholderText", "Ex.: Notebook");

        // ---------- formulario
        JPanel cadastro = Estilo.cartao();
        cadastro.add(Estilo.subtitulo("Cadastro de equipamentos"), BorderLayout.NORTH);
        JPanel campos = new JPanel(new GridLayout(1, 4, 12, 0)); // 1 linha, 4 colunas
        campos.setOpaque(false);
        campos.add(Estilo.campo("Patrimônio", patrimonio));
        campos.add(Estilo.campo("Nome", nome));
        campos.add(Estilo.campo("Categoria", categoria));
        campos.add(Estilo.campo("Estado de conservação", estado));
        cadastro.add(campos, BorderLayout.CENTER);

        JButton criar = Estilo.botaoPrincipal("Cadastrar");
        JButton salvar = Estilo.botao("Salvar alterações");
        JButton excluirForm = Estilo.botao("Excluir");
        JButton limpar = Estilo.botao("Limpar");
        cadastro.add(Estilo.linhaBotoes(criar, salvar, excluirForm, limpar), BorderLayout.SOUTH);
        add(cadastro, BorderLayout.NORTH);

        // ---------- lista: busca, filtros, botao "Alterar situacao" e menu "⋮"
        JButton alterarSituacao = Estilo.botaoPrincipal("Alterar situação");
        filtro.adicionarFiltro("Categoria", 3).adicionarFiltro("Estado", 4).adicionarFiltro("Status", 5)
            .adicionarBotao(alterarSituacao);
        Estilo.larguras(tabela, 50, 120, 300, 140, 120, 130, 70);
        Estilo.colorirSituacao(tabela, 4);
        Estilo.colorirSituacao(tabela, 5);
        JPopupMenu menu = MenuAcoes.instalar(tabela, 6);
        menu.add(MenuAcoes.item("Alterar status", "trocar", Estilo.BARRA_LATERAL, () -> alterar(0)));
        menu.add(MenuAcoes.item("Alterar estado", "trocar", Estilo.BARRA_LATERAL, () -> alterar(1)));
        menu.add(MenuAcoes.item("Editar dados", "editar", Estilo.BARRA_LATERAL, this::editarDados));
        menu.add(MenuAcoes.item("Ver histórico", "relogio", Estilo.BARRA_LATERAL, this::verHistorico));
        menu.add(MenuAcoes.item("Excluir", "excluir", Estilo.VERMELHO, this::excluir));
        JPanel listagem = Estilo.cartao();
        listagem.add(filtro, BorderLayout.NORTH);
        listagem.add(Estilo.rolagem(tabela), BorderLayout.CENTER);
        listagem.add(new JLabel("Excluir: somente equipamentos nunca emprestados. Com histórico, use Alterar situação → Indisponível."),
            BorderLayout.SOUTH);
        add(listagem, BorderLayout.CENTER);

        // ---------- botoes
        criar.addActionListener(e -> executar(() -> {
            cadastrar(patrimonio.getText(), nome.getText(), categoria.getText(),
                Regras.codigo((String) estado.getSelectedItem()));
            limpar();
        }));
        salvar.addActionListener(e -> executar(() -> {
            Equipamento atual = exigirSelecao();
            atualizar(atual.getId(), patrimonio.getText(), nome.getText(),
                categoria.getText(), Regras.codigo((String) estado.getSelectedItem()));
        }));
        excluirForm.addActionListener(e -> Mensagens.executar(this, this::excluir));
        limpar.addActionListener(e -> limpar());
        alterarSituacao.addActionListener(e -> Mensagens.executar(this, () -> alterar(0)));
        tabela.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) preencher();
        });
        carregar();
    }

    // ------------------------------------------------------------ janelas (modais) do menu "⋮"

    /** Alterar equipamento: duas abas, Status (disponivel/indisponivel) e Estado (conservacao). */
    private void alterar(int abaInicial) {
        Equipamento e = exigirSelecao();
        JComboBox<String> novoStatus = new JComboBox<>(OPCOES_STATUS);
        novoStatus.setSelectedItem(Equipamento.STATUS_DISPONIVEL.equals(e.getStatus()) ? "Indisponível" : "Disponível");
        JComboBox<String> novoEstado = new JComboBox<>(Regras.rotulos(Regras.ESTADOS));
        novoEstado.setSelectedItem(Regras.rotulo(e.getEstado()));

        JTabbedPane abas = new JTabbedPane();
        abas.addTab("Status", montarAba("Status atual", Regras.rotulo(e.getStatus()), "Novo status", novoStatus));
        abas.addTab("Estado", montarAba("Estado atual", Regras.rotulo(e.getEstado()), "Novo estado", novoEstado));
        abas.setSelectedIndex(abaInicial);

        Modal modal = new Modal(this, "Alterar equipamento")
            .info("Patrimônio", e.getPatrimonio()).info("Nome", e.getNome()).componente(abas);
        if (modal.exibir(() -> {
            if (abas.getSelectedIndex() == 0) {
                String status = Regras.codigo((String) novoStatus.getSelectedItem());
                TarefaBanco.executar(modal, () -> Aplicacao.equipamentos().alterarStatus(e.getId(), status));
            } else {
                atualizar(e.getId(), e.getPatrimonio(), e.getNome(), e.getCategoria(),
                    Regras.codigo((String) novoEstado.getSelectedItem()));
            }
        })) {
            carregar();
        }
    }

    /** Uma aba do modal: "Rotulo atual: [selo]" e, embaixo, a lista com o novo valor. */
    private JPanel montarAba(String rotuloAtual, String valorAtual, String rotuloNovo, JComboBox<String> novo) {
        JPanel atual = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        atual.setOpaque(false);
        JLabel texto = new JLabel(rotuloAtual + ":");
        texto.setForeground(Estilo.TEXTO_SUAVE);
        atual.add(texto);
        atual.add(Estilo.selo(valorAtual));

        JPanel aba = new JPanel(new BorderLayout(0, 12));
        aba.setOpaque(false);
        aba.setBorder(BorderFactory.createEmptyBorder(14, 0, 4, 0));
        aba.add(atual, BorderLayout.NORTH);
        aba.add(Estilo.campo(rotuloNovo, novo), BorderLayout.CENTER);
        return aba;
    }

    /** Editar dados: patrimonio, nome, categoria e estado do equipamento escolhido. */
    private void editarDados() {
        Equipamento e = exigirSelecao();
        JTextField campoPatrimonio = new JTextField(e.getPatrimonio());
        JTextField campoNome = new JTextField(e.getNome());
        JTextField campoCategoria = new JTextField(e.getCategoria());
        JComboBox<String> campoEstado = new JComboBox<>(Regras.rotulos(Regras.ESTADOS));
        campoEstado.setSelectedItem(Regras.rotulo(e.getEstado()));

        Modal modal = new Modal(this, "Editar equipamento")
            .campo("Patrimônio", campoPatrimonio).campo("Nome", campoNome)
            .campo("Categoria", campoCategoria).campo("Estado de conservação", campoEstado);
        if (modal.exibir(() -> atualizar(e.getId(), campoPatrimonio.getText(),
                campoNome.getText(), campoCategoria.getText(),
                Regras.codigo((String) campoEstado.getSelectedItem())))) {
            carregar();
        }
    }

    /** Ver historico: todos os emprestimos deste equipamento (somente leitura). */
    private void verHistorico() {
        Equipamento e = exigirSelecao();
        JTable historico = Estilo.criarTabela(
            new String[]{"Pessoa", "Retirada", "Devolução prevista", "Devolvido em", "Situação"});
        for (EmprestimoDetalhe d : TarefaBanco.consultar(this, Aplicacao.emprestimos()::listarDetalhado)) {
            Emprestimo m = d.emprestimo();
            if (m.getEquipamentoId() == e.getId()) {
                Estilo.modelo(historico).addRow(new Object[]{d.usuarioNome(), Estilo.dataHora(m.getDataRetirada()),
                    Estilo.dia(m.getDataPrevista()), Estilo.dia(m.getDataDevolucao()), Regras.situacao(m)});
            }
        }
        Estilo.colorirSituacao(historico, 4);

        Modal modal = new Modal(this, "Histórico do equipamento")
            .info("Patrimônio", e.getPatrimonio()).info("Nome", e.getNome());
        if (historico.getRowCount() == 0) {
            modal.info("Empréstimos", "este equipamento ainda não foi emprestado");
        } else {
            JScrollPane rolagem = Estilo.rolagem(historico);
            rolagem.setPreferredSize(new Dimension(640, 200));
            modal.componente(rolagem);
        }
        modal.exibirSomenteLeitura();
    }

    private void excluir() {
        Equipamento e = exigirSelecao();
        if (Mensagens.confirmar(this, "Excluir " + e.getNome() + "?")) {
            TarefaBanco.executar(this, () -> Aplicacao.equipamentos().excluir(e.getId()));
            limpar();
            carregar();
        }
    }

    // ------------------------------------------------------------ apoio

    private void cadastrar(String patrimonio, String nome, String categoria, String estado) {
        TarefaBanco.executar(this, () -> Aplicacao.equipamentos().cadastrar(patrimonio, nome, categoria, estado));
    }

    private void atualizar(int id, String patrimonio, String nome, String categoria, String estado) {
        TarefaBanco.executar(this, () -> Aplicacao.equipamentos().atualizar(id, patrimonio, nome, categoria, estado));
    }

    private Equipamento exigirSelecao() {
        Equipamento atual = selecionado();
        if (atual == null) throw new RegraNegocioException("Selecione um equipamento.");
        return atual;
    }

    /** Equipamento da linha selecionada (linhaSelecionada ja considera ordenacao e filtro). */
    private Equipamento selecionado() {
        int i = Estilo.linhaSelecionada(tabela);
        return i >= 0 && i < linhas.size() ? linhas.get(i) : null;
    }

    private void preencher() {
        Equipamento e = selecionado();
        if (e == null) return;
        patrimonio.setText(e.getPatrimonio());
        nome.setText(e.getNome());
        categoria.setText(e.getCategoria());
        estado.setSelectedItem(Regras.rotulo(e.getEstado()));
    }

    private void limpar() {
        tabela.clearSelection();
        patrimonio.setText("");
        nome.setText("");
        categoria.setText("");
        estado.setSelectedIndex(0);
    }

    private void carregar() {
        Equipamento anterior = selecionado(); // para manter a mesma linha escolhida depois de recarregar
        List<Equipamento> atualizadas = TarefaBanco.consultar(this, Aplicacao.equipamentos()::listar);
        linhas.clear();
        linhas.addAll(atualizadas);
        Estilo.modelo(tabela).setRowCount(0);
        for (Equipamento e : linhas) {
            Estilo.modelo(tabela).addRow(new Object[]{e.getId(), e.getPatrimonio(), e.getNome(),
                e.getCategoria(), Regras.rotulo(e.getEstado()), Regras.rotulo(e.getStatus()), ""});
        }
        filtro.atualizar();
        if (anterior != null) {
            for (int i = 0; i < linhas.size(); i++) {
                int visual = tabela.convertRowIndexToView(i);
                if (linhas.get(i).getId() == anterior.getId() && visual >= 0) {
                    tabela.setRowSelectionInterval(visual, visual);
                }
            }
        }
    }

    private void executar(Runnable acao) {
        try { acao.run(); carregar(); Mensagens.info(this, "Operação concluída."); }
        catch (RuntimeException erro) { Mensagens.erro(this, erro); }
    }
}
