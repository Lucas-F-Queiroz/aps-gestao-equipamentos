package br.com.aps.model;

public class Usuario {

    public static final String TIPO_ALUNO = "ALUNO";
    public static final String TIPO_PROFESSOR = "PROFESSOR";
    public static final String TIPO_FUNCIONARIO = "FUNCIONARIO";

    private int id;
    private String nome;
    private String email;
    private String tipo;
    private boolean ativo;

    public Usuario() {
    }

    public Usuario(int id, String nome, String email, String tipo, boolean ativo) {
        this.id = id;
        this.nome = nome;
        this.email = email;
        this.tipo = tipo;
        this.ativo = ativo;
    }

    public Usuario(String nome, String email, String tipo) {
        this(0, nome, email, tipo, true);
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getTipo() {
        return tipo;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }

    public boolean isAtivo() {
        return ativo;
    }

    public void setAtivo(boolean ativo) {
        this.ativo = ativo;
    }
}
