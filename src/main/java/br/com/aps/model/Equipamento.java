package br.com.aps.model;

public class Equipamento {

    private int id;
    private String patrimonio;
    private String nome;
    private String categoria;
    private String estado;
    private String status;

    public Equipamento() {
    }

    public Equipamento(int id, String patrimonio, String nome, String categoria,
                       String estado, String status) {
        this.id = id;
        this.patrimonio = patrimonio;
        this.nome = nome;
        this.categoria = categoria;
        this.estado = estado;
        this.status = status;
    }

    public Equipamento(String patrimonio, String nome, String categoria,
                       String estado, String status) {
        this(0, patrimonio, nome, categoria, estado, status);
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getPatrimonio() {
        return patrimonio;
    }

    public void setPatrimonio(String patrimonio) {
        this.patrimonio = patrimonio;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getCategoria() {
        return categoria;
    }

    public void setCategoria(String categoria) {
        this.categoria = categoria;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
