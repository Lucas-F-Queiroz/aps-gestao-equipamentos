package br.com.aps.integracao;

import br.com.aps.controller.RegraNegocioException;
import br.com.aps.controller.Regras;

/** Regras de CPF e celular; nao acessa o banco. Dados sinteticos de teste. */
public final class APSPessoaValidacaoCheck {
    public static void main(String[] args) {
        exigir(Regras.cpf(null) == null && Regras.cpf(" ") == null, "CPF opcional");
        exigir("01234567890".equals(Regras.cpf("012.345.678-90")), "CPF com zero inicial e mascara");
        exigir("52998224725".equals(Regras.cpf("52998224725")), "CPF sem mascara");
        rejeitar(() -> Regras.cpf("111.111.111-11"));
        rejeitar(() -> Regras.cpf("529.982.247-24"));
        rejeitar(() -> Regras.cpf("00000000000"));
        rejeitar(() -> Regras.cpf("abc52998224725"));
        rejeitar(() -> Regras.cpf("123"));
        exigir(Regras.celular(null) == null && Regras.celular("") == null, "Celular opcional");
        exigir("11912345678".equals(Regras.celular("(11) 91234-5678")), "DDD e mascara");
        exigir("21987654321".equals(Regras.celular("21987654321")), "Celular sem mascara");
        rejeitar(() -> Regras.celular("11 3123-4567"));
        rejeitar(() -> Regras.celular("00 91234-5678"));
        rejeitar(() -> Regras.celular("abc11912345678"));
        rejeitar(() -> Regras.celular("912345678"));
        System.out.println("APSPessoaValidacaoCheck: CPF e celular OK");
    }

    private static void rejeitar(Runnable acao) {
        try { acao.run(); }
        catch (RegraNegocioException esperado) { return; }
        throw new AssertionError("Valor invalido foi aceito");
    }

    private static void exigir(boolean ok, String mensagem) {
        if (!ok) throw new AssertionError(mensagem);
    }
}
