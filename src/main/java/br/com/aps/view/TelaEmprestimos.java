package br.com.aps.view;

import br.com.aps.Aplicacao;
import br.com.aps.controller.Regras;
import br.com.aps.model.EmprestimoDetalhe;
import br.com.aps.model.Equipamento;
import br.com.aps.model.Usuario;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.JTextField;

/**
 * Emprestimos: registra uma retirada seguindo as regras de negocio:
 *   1. a pessoa precisa estar ativa;
 *   2. o equipamento precisa estar DISPONIVEL;
 *   3. um equipamento nao pode ter dois emprestimos abertos;
 *   4. ao emprestar, o equipamento passa para EMPRESTADO e o emprestimo fica ABERTO.
 * A devolucao prevista nao pode ser anterior a retirada. As regras estao em EmprestimoController.
 */
public class TelaEmprestimos extends JPanel {
    private static final DateTimeFormatter DATA = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private final JComboBox<String> comboEquipamento = new JComboBox<>();
    private final JComboBox<String> comboPessoa = new JComboBox<>();
    private final JTextField campoPrevista = new JTextField();
    private final JTable tabela = Estilo.criarTabela(new String[] {
        "ID", "Pessoa", "Equipamento", "Retirada", "Devolução prevista", "Devolvido em", "Situação"});
    private final FiltroTabela filtro = new FiltroTabela(tabela);
    private final ArrayList<Equipamento> equipamentosDaLista = new ArrayList<>();
    private final ArrayList<Usuario> pessoasDaLista = new ArrayList<>();

    public TelaEmprestimos() {
        super(new BorderLayout(0, 18));
        setOpaque(false);

        campoPrevista.setText(LocalDate.now().plusDays(7).format(DATA));
        campoPrevista.putClientProperty("JTextField.placeholderText", "dd/mm/aaaa");

        JPanel cartaoForm = Estilo.cartao();
        cartaoForm.add(Estilo.subtitulo("Novo empréstimo"), BorderLayout.NORTH);

        JButton registrar = Estilo.botaoPrincipal("Registrar empréstimo");
        JPanel campos = new JPanel(new GridLayout(1, 4, 16, 0));
        campos.setOpaque(false);
        campos.add(Estilo.campo("Equipamento (somente disponíveis)", comboEquipamento));
        campos.add(Estilo.campo("Pessoa (somente ativas)", comboPessoa));
        campos.add(Estilo.campo("Devolução prevista (dd/mm/aaaa)", new CalendarioData(campoPrevista)));
        campos.add(Estilo.campo(" ", Estilo.linhaBotoes(registrar))); // " " alinha o botao com os campos

        JLabel dica = new JLabel("A retirada é agora. A data prevista vale até o fim do dia e não pode ser anterior a hoje.");
        dica.setForeground(Estilo.TEXTO_SUAVE);

        JPanel miolo = new JPanel(new BorderLayout(0, 14));
        miolo.setOpaque(false);
        miolo.add(campos, BorderLayout.NORTH);
        miolo.add(dica, BorderLayout.CENTER);
        cartaoForm.add(miolo, BorderLayout.CENTER);
        add(cartaoForm, BorderLayout.NORTH);

        // Lista de todos os emprestimos, com busca, filtros e periodo da retirada
        filtro.adicionarFiltro("Situação", 6);
        filtro.adicionarFiltro("Pessoa", 1);
        filtro.adicionarPeriodo("Data do empréstimo", 3);
        JPanel cartaoTabela = Estilo.cartao();
        JPanel cabecalhoLista = new JPanel(new BorderLayout(0, 10));
        cabecalhoLista.setOpaque(false);
        cabecalhoLista.add(Estilo.subtitulo("Empréstimos registrados"), BorderLayout.NORTH);
        cabecalhoLista.add(filtro, BorderLayout.CENTER);
        cartaoTabela.add(cabecalhoLista, BorderLayout.NORTH);
        Estilo.colorirSituacao(tabela, 6);
        Estilo.larguras(tabela, 50, 160, 320, 150, 150, 150, 110);
        cartaoTabela.add(Estilo.rolagem(tabela), BorderLayout.CENTER);
        add(cartaoTabela, BorderLayout.CENTER);

        registrar.addActionListener(e -> registrar());
        atualizar();
    }

    /** Recarrega as listas de escolha (so o que a regra permite) e a tabela. */
    private void atualizar() {
        try {
            // guarda o que a pessoa ja tinha escolhido, para nao perder a selecao ao recarregar
            int equipamentoEscolhido = comboEquipamento.getSelectedIndex() < 0 ? -1
                : equipamentosDaLista.get(comboEquipamento.getSelectedIndex()).getId();
            int pessoaEscolhida = comboPessoa.getSelectedIndex() < 0 ? -1
                : pessoasDaLista.get(comboPessoa.getSelectedIndex()).getId();

            var equipamentos = TarefaBanco.consultar(this, Aplicacao.equipamentos()::listarDisponiveis);
            var pessoas = TarefaBanco.consultar(this, Aplicacao.usuarios()::listar);
            var emprestimos = TarefaBanco.consultar(this, Aplicacao.emprestimos()::listarDetalhado);
            comboEquipamento.removeAllItems();
            equipamentosDaLista.clear();
            for (Equipamento e : equipamentos) { // regra 2
                equipamentosDaLista.add(e);
                comboEquipamento.addItem(e.getNome() + " (" + e.getPatrimonio() + ")");
                if (e.getId() == equipamentoEscolhido) {
                    comboEquipamento.setSelectedIndex(equipamentosDaLista.size() - 1);
                }
            }
            comboPessoa.removeAllItems();
            pessoasDaLista.clear();
            for (Usuario u : pessoas) {
                if (u.isAtivo()) { // regra 1
                    pessoasDaLista.add(u);
                    comboPessoa.addItem(u.getNome());
                    if (u.getId() == pessoaEscolhida) {
                        comboPessoa.setSelectedIndex(pessoasDaLista.size() - 1);
                    }
                }
            }
            Estilo.modelo(tabela).setRowCount(0);
            for (EmprestimoDetalhe d : emprestimos) { // do mais novo ao mais antigo
                Estilo.modelo(tabela).addRow(new Object[] {d.emprestimo().getId(), d.usuarioNome(),
                    d.equipamentoNome() + " (" + d.equipamentoPatrimonio() + ")",
                    Estilo.dataHora(d.emprestimo().getDataRetirada()), Estilo.dia(d.emprestimo().getDataPrevista()),
                    Estilo.dataHora(d.emprestimo().getDataDevolucao()), Regras.situacao(d.emprestimo())});
            }
            filtro.atualizar();
        } catch (RuntimeException erro) {
            Mensagens.erro(this, erro);
        }
    }

    private void registrar() {
        int posicaoEquipamento = comboEquipamento.getSelectedIndex();
        int posicaoPessoa = comboPessoa.getSelectedIndex();
        if (posicaoEquipamento < 0 || posicaoPessoa < 0) {
            Mensagens.aviso(this, "Escolha um equipamento disponível e uma pessoa ativa.");
            return;
        }
        try {
            LocalDate prevista = Regras.lerData(campoPrevista.getText(), "a devolução prevista");
            int pessoaId = pessoasDaLista.get(posicaoPessoa).getId();
            int equipamentoId = equipamentosDaLista.get(posicaoEquipamento).getId();
            TarefaBanco.executar(this, () -> Aplicacao.emprestimos().emprestar(pessoaId, equipamentoId, prevista));
            campoPrevista.setText(LocalDate.now().plusDays(7).format(DATA));
            atualizar();
            Mensagens.info(this, "Empréstimo registrado. O equipamento agora está Emprestado.");
        } catch (RuntimeException erro) {
            Mensagens.erro(this, erro);
            atualizar(); // outra tela ou computador pode ter mudado os dados: recarrega as listas
        }
    }
}
