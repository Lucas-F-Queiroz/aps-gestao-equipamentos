package br.com.aps;

import br.com.aps.controller.EmprestimoController;
import br.com.aps.controller.EquipamentoController;
import br.com.aps.controller.UsuarioController;
import br.com.aps.util.DatabaseMigrator;
import br.com.aps.view.Estilo;
import br.com.aps.view.JanelaPrincipal;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

/**
 * Classe principal do sistema. Liga o visual (FlatLaf), aplica as migrations do Flyway ANTES de
 * abrir a interface (conforme o README) e guarda os Controllers que todas as telas compartilham.
 */
public final class Aplicacao {
    private static UsuarioController usuarios;
    private static EquipamentoController equipamentos;
    private static EmprestimoController emprestimos;

    private Aplicacao() {
    }

    public static synchronized UsuarioController usuarios() {
        if (usuarios == null) {
            usuarios = new UsuarioController();
        }
        return usuarios;
    }

    public static synchronized EquipamentoController equipamentos() {
        if (equipamentos == null) {
            equipamentos = new EquipamentoController();
        }
        return equipamentos;
    }

    public static synchronized EmprestimoController emprestimos() {
        if (emprestimos == null) {
            emprestimos = new EmprestimoController();
        }
        return emprestimos;
    }

    public static void main(String[] args) {
        Estilo.instalar(); // 1) escolhe o visual (FlatLaf) ANTES de criar qualquer janela

        SwingUtilities.invokeLater(() -> {
            try {
                DatabaseMigrator.migrate(); // 2) cria/atualiza as tabelas do banco
            } catch (RuntimeException erro) {
                erro.printStackTrace();
                JOptionPane.showMessageDialog(
                    null, mensagemDeFalha(erro), "Não foi possível iniciar", JOptionPane.ERROR_MESSAGE);
                System.exit(1);
                return;
            }
            new JanelaPrincipal().setVisible(true); // 3) abre o sistema
        });
    }

    /** Explica em portugues os problemas mais comuns de conexao. */
    private static String mensagemDeFalha(RuntimeException erro) {
        String texto = erro.getMessage() == null ? "" : erro.getMessage();
        if (erro instanceof IllegalStateException && !texto.isEmpty()) {
            return texto; // ex.: application.properties ausente ou incompleto
        }
        String ajuda;
        if (texto.contains("Access denied")) {
            ajuda = "O MySQL recusou o usuário ou a senha.\n"
                + "Confira db.user e db.password no arquivo src/main/resources/application.properties.";
        } else if (texto.contains("Unknown database")) {
            ajuda = "O banco aps_equipamentos ainda não existe.\n"
                + "Execute o arquivo database/create_database.sql no MySQL Workbench.";
        } else if (texto.contains("Communications link failure") || texto.contains("Connection refused")) {
            ajuda = "Não foi possível falar com o MySQL.\n"
                + "Confira se ele está ligado (Serviços do Windows > MySQL80) e a porta no db.url.";
        } else if (texto.contains("Validate failed")) {
            ajuda = "O histórico de migrações não corresponde aos arquivos do projeto.\n"
                + "Confira as migrações V1 a V5 e faça Project > Clean no Eclipse.\n\n" + texto;
        } else {
            ajuda = texto;
        }
        return "Não foi possível preparar o banco de dados.\n\n" + ajuda;
    }
}
