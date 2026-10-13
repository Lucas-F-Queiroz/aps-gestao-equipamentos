package br.com.aps.util;

public class DatabaseConfigManualCheck {

    public static void main(String[] args) {
        DatabaseConfig config = new DatabaseConfig("jdbc:mysql://localhost/test", "teste", " senha ");
        verificar(" senha ".equals(config.password()), "A senha deve preservar espacos.");
        verificar(new DatabaseConfig(config.url(), config.user(), "").password().isEmpty(),
                "Senha vazia deve ser permitida.");
        invalido(null, "teste", "senha");
        invalido(" ", "teste", "senha");
        invalido(config.url(), null, "senha");
        invalido(config.url(), " ", "senha");
        invalido(config.url(), "teste", null);
        System.out.println("DatabaseConfig: verificacoes concluidas com sucesso.");
    }

    private static void invalido(String url, String user, String password) {
        try {
            new DatabaseConfig(url, user, password);
        } catch (IllegalStateException esperado) {
            return;
        }
        throw new AssertionError("Configuracao incompleta deveria ser rejeitada.");
    }

    private static void verificar(boolean condicao, String mensagem) {
        if (!condicao) {
            throw new AssertionError(mensagem);
        }
    }
}
