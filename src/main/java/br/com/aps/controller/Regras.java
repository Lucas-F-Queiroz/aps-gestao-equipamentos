package br.com.aps.controller;

import br.com.aps.model.Emprestimo;
import br.com.aps.model.Equipamento;
import br.com.aps.model.Usuario;

import java.text.Normalizer;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.ResolverStyle;

/**
 * As regras e limites definidos pelo Lucas (README e banco de dados), num lugar so.
 * Os Controllers usam esta classe para validar o que vem da tela.
 *
 * Valores permitidos (o banco recusa qualquer outro):
 *   Usuario.tipo ALUNO, PROFESSOR ou FUNCIONARIO
 *   Equipamento.estado   NOVO, BOM, REGULAR, DANIFICADO
 *   Equipamento.status   DISPONIVEL, EMPRESTADO, INDISPONIVEL
 *   Emprestimo.status    ABERTO, DEVOLVIDO   (ATRASADO nao e gravado: e calculado pela data)
 *
 * Tamanhos maximos (colunas VARCHAR do banco):
 *   usuario.nome 100 | usuario.email 120 (unico) | equipamento.patrimonio 50 (unico)
 *   equipamento.nome 100 | equipamento.categoria 50 | devolucao.observacao 255
 */
public final class Regras {
    public static final String[] TIPOS_USUARIO = {
        Usuario.TIPO_ALUNO, Usuario.TIPO_PROFESSOR, Usuario.TIPO_FUNCIONARIO};
    public static final String[] ESTADOS = {
        Equipamento.ESTADO_NOVO, Equipamento.ESTADO_BOM,
        Equipamento.ESTADO_REGULAR, Equipamento.ESTADO_DANIFICADO};

    public static final int NOME_USUARIO_MAX = 100;
    public static final int EMAIL_MAX = 120;
    public static final int PATRIMONIO_MAX = 50;
    public static final int NOME_EQUIPAMENTO_MAX = 100;
    public static final int CATEGORIA_MAX = 50;
    public static final int OBSERVACAO_MAX = 255;

    // STRICT: recusa datas que nao existem (31/02/2026). Sem isso o Java "corrige" para 28/02.
    private static final DateTimeFormatter DATA =
        DateTimeFormatter.ofPattern("dd/MM/uuuu").withResolverStyle(ResolverStyle.STRICT);

    private Regras() {
    }

    // ------------------------------------------------------------ texto da tela x valor do banco

    /** Valor do banco -> texto bonito da tela. Ex.: "DISPONIVEL" vira "Disponível". */
    public static String rotulo(String codigo) {
        if (codigo == null || codigo.isBlank()) return "Não informado";
        switch (codigo) {
            case "DISPONIVEL":
                return "Disponível";
            case "INDISPONIVEL":
                return "Indisponível";
            case "FUNCIONARIO":
                return "Funcionário";
            default:
                return codigo.substring(0, 1) + codigo.substring(1).toLowerCase(); // "BOM" -> "Bom"
        }
    }

    public static String[] rotulos(String[] codigos) {
        String[] resultado = new String[codigos.length];
        for (int i = 0; i < codigos.length; i++) {
            resultado[i] = rotulo(codigos[i]);
        }
        return resultado;
    }

    /** Texto da tela -> valor do banco. Ex.: "Funcionário" vira "FUNCIONARIO". */
    public static String codigo(String rotulo) {
        String semAcento = Normalizer.normalize(rotulo, Normalizer.Form.NFD).replaceAll("\\p{M}", "");
        return semAcento.toUpperCase();
    }

    /** Situacao mostrada na tela: Aberto, Atrasado (calculado pela data) ou Devolvido. */
    public static String situacao(Emprestimo emprestimo) {
        if (emprestimo.isAtrasado()) {
            return "Atrasado";
        }
        return rotulo(emprestimo.getStatus());
    }

    // ------------------------------------------------------------ validacoes (lancam RegraNegocioException)

    /** Confere obrigatorio e tamanho. Devolve o texto sem espacos nas pontas. */
    public static String exigirTexto(String campo, String valor, int maximo) {
        String texto = valor == null ? "" : valor.trim();
        if (texto.isEmpty()) {
            throw new RegraNegocioException("Informe o campo " + campo + ".");
        }
        if (texto.length() > maximo) {
            throw new RegraNegocioException(
                "O campo " + campo + " aceita no máximo " + maximo + " caracteres.");
        }
        return texto;
    }

    public static String textoOpcional(String campo, String valor, int maximo) {
        if (valor == null || valor.isBlank()) return null;
        return exigirTexto(campo, valor, maximo);
    }

    public static String exigirEmail(String valor) {
        String email = exigirTexto("e-mail", valor, EMAIL_MAX);
        if (email.contains(" ") || !email.matches("[^@]+@[^@]+\\.[^@]+")) {
            throw new RegraNegocioException("Informe um e-mail válido.");
        }
        return email;
    }

    /** Confere se o valor e um dos permitidos; devolve o codigo do banco. */
    public static String exigirUm(String campo, String valor, String[] permitidos) {
        String codigo = valor == null ? "" : codigo(valor.trim());
        for (String permitido : permitidos) {
            if (permitido.equals(codigo)) {
                return permitido;
            }
        }
        throw new RegraNegocioException("Valor inválido para " + campo + ".");
    }

    /** Le "dd/MM/aaaa". Lanca RegraNegocioException se o texto nao for uma data valida. */
    public static LocalDate lerData(String texto, String campo) {
        try {
            return LocalDate.parse(texto == null ? "" : texto.trim(), DATA);
        } catch (DateTimeParseException erro) {
            throw new RegraNegocioException("Informe " + campo + " no formato dd/mm/aaaa.");
        }
    }
}
