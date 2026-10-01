package br.com.aps.model;

import java.time.LocalDateTime;

public class Emprestimo {

    public static final String STATUS_ABERTO = "ABERTO";
    public static final String STATUS_DEVOLVIDO = "DEVOLVIDO";

    private int id;
    private int usuarioId;
    private int equipamentoId;
    private LocalDateTime dataRetirada;
    private LocalDateTime dataPrevista;
    private LocalDateTime dataDevolucao;
    private String status;

    public Emprestimo() {
    }

    public Emprestimo(int id, int usuarioId, int equipamentoId,
                      LocalDateTime dataRetirada, LocalDateTime dataPrevista,
                      LocalDateTime dataDevolucao, String status) {
        this.id = id;
        this.usuarioId = usuarioId;
        this.equipamentoId = equipamentoId;
        this.dataRetirada = dataRetirada;
        this.dataPrevista = dataPrevista;
        this.dataDevolucao = dataDevolucao;
        this.status = status;
    }

    public Emprestimo(int usuarioId, int equipamentoId,
                      LocalDateTime dataRetirada, LocalDateTime dataPrevista) {
        this(0, usuarioId, equipamentoId, dataRetirada, dataPrevista, null, STATUS_ABERTO);
    }

    public boolean isAtrasado() {
        return STATUS_ABERTO.equals(status)
                && dataPrevista != null
                && LocalDateTime.now().isAfter(dataPrevista);
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getUsuarioId() {
        return usuarioId;
    }

    public void setUsuarioId(int usuarioId) {
        this.usuarioId = usuarioId;
    }

    public int getEquipamentoId() {
        return equipamentoId;
    }

    public void setEquipamentoId(int equipamentoId) {
        this.equipamentoId = equipamentoId;
    }

    public LocalDateTime getDataRetirada() {
        return dataRetirada;
    }

    public void setDataRetirada(LocalDateTime dataRetirada) {
        this.dataRetirada = dataRetirada;
    }

    public LocalDateTime getDataPrevista() {
        return dataPrevista;
    }

    public void setDataPrevista(LocalDateTime dataPrevista) {
        this.dataPrevista = dataPrevista;
    }

    public LocalDateTime getDataDevolucao() {
        return dataDevolucao;
    }

    public void setDataDevolucao(LocalDateTime dataDevolucao) {
        this.dataDevolucao = dataDevolucao;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
