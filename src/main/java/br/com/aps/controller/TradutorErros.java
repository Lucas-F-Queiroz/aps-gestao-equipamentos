package br.com.aps.controller;

import java.sql.SQLException;
import java.util.Locale;

/** Transforma os erros do banco (SQLException dos DAOs/MySQL) em mensagens claras para a tela. */
public final class TradutorErros {

    private TradutorErros() {
    }

    public static RegraNegocioException traduzir(SQLException erro) {
        String original = erro.getMessage() == null ? "" : erro.getMessage();
        String texto = original.toLowerCase(Locale.ROOT);
        String estado = erro.getSQLState() == null ? "" : erro.getSQLState();

        // Regras lancadas pelo EmprestimoDAO (SQLState 45000), com a mensagem pronta.
        if ("45000".equals(estado)) {
            return new RegraNegocioException(mensagemDoDao(original), erro);
        }

        switch (erro.getErrorCode()) {
            case 1062: // valor repetido (UNIQUE)
                return new RegraNegocioException(mensagemDeRepetido(texto), erro);
            case 1451: // chave estrangeira ON DELETE RESTRICT
                return new RegraNegocioException(
                    "Não é possível excluir: existe histórico de empréstimos, mesmo após a devolução. "
                    + "Preserve o histórico: marque o equipamento como Indisponível ou a pessoa como Inativa.", erro);
            case 3819: // CHECK violado
                return new RegraNegocioException(
                    "Os dados informados violam uma regra do banco de dados.", erro);
            case 1406: // texto maior que a coluna
                return new RegraNegocioException(
                    "Algum texto é maior que o permitido para o campo.", erro);
            default:
                break;
        }

        if (estado.startsWith("08")) {
            return new RegraNegocioException(
                "Não foi possível conectar ao MySQL. Verifique se o banco está iniciado e a configuração de conexão.",
                erro);
        }
        return new RegraNegocioException("Erro ao acessar o banco de dados.", erro);
    }

    // O MySQL diz qual indice foi violado; assim a mensagem aponta o campo certo.
    private static String mensagemDeRepetido(String texto) {
        if (texto.contains("uq_usuario_cpf")) return "Já existe uma pessoa com esse CPF.";
        if (texto.contains("uq_emprestimo_equipamento_aberto")) {
            return "Este equipamento já possui um empréstimo aberto.";
        }
        if (texto.contains("matricula")) return "Já existe uma pessoa com essa matrícula.";
        if (texto.contains("numero_serie")) return "Já existe um equipamento com esse número de série.";
        if (texto.contains("email")) {
            return "Já existe uma pessoa com esse e-mail.";
        }
        if (texto.contains("patrimonio")) {
            return "Já existe um equipamento com esse patrimônio.";
        }
        return "Já existe um registro com esse valor.";
    }

    // Mensagens dos DAOs (sem acento) -> texto para a tela.
    private static String mensagemDoDao(String original) {
        switch (original) {
            case "Usuario inexistente ou inativo.":
                return "A pessoa precisa existir e estar ativa para realizar um empréstimo.";
            case "Equipamento inexistente ou indisponivel.":
                return "O equipamento precisa estar com status Disponível.";
            case "Equipamento ja possui emprestimo aberto.":
                return "Este equipamento já possui um empréstimo aberto.";
            case "Status do equipamento inconsistente.":
                return "O status do equipamento não confere com o empréstimo. "
                    + "Procure o responsável pelo banco antes de devolver.";
            case "Nao foi possivel registrar a devolucao.":
                return "Não foi possível registrar a devolução.";
            default:
                return original;
        }
    }
}
