package br.com.aps.controller;

/**
 * Erro esperado do sistema (regra de negocio ou dado invalido). A mensagem ja vem pronta para
 * ser mostrada ao usuario. E "unchecked" para as telas poderem tratar com um try/catch simples.
 */
public class RegraNegocioException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public RegraNegocioException(String mensagem) {
        super(mensagem);
    }

    public RegraNegocioException(String mensagem, Throwable causa) {
        super(mensagem, causa);
    }
}
