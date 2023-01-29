package com.cursoandroid.uber.model;

import com.cursoandroid.uber.service.DatabaseService;

public class Requisicao implements DatabaseService.ModelId {
    private String id;
    private String status;
    private Usuario passageiro;
    private Usuario motorista;
    private Destino destino;
    private long inicioRequisicao;//1672253454000
    private long finalRequisicao;

    public static final String STATUS_AGUARDADNDO = "AGUARDANDO";
    public static final String STATUS_A_CAMINHO = "A_CAMINHO";
    public static final String STATUS_VIAGEM = "VIAGEM";
    public static final String STATUS_NO_DESTINO = "NO_DESTINO";
    public static final String STATUS_ENCERRADO = "ENCERRADO";
    public static final String STATUS_CANCELADO_USUARIO = "CANCELADO_USUARIO";
    public static final String STATUS_CANCELADO_MOTORISTA = "CANCELADO_MOTORISTA";

    public Requisicao() {
    }

    public Requisicao(String id, String status, Usuario passageiro, Usuario motorista, Destino destino, long inicioRequisicao) {
        this.id = id;
        this.status = status;
        this.passageiro = passageiro;
        this.motorista = motorista;
        this.destino = destino;
        this.inicioRequisicao = inicioRequisicao;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Usuario getPassageiro() {
        return passageiro;
    }

    public void setPassageiro(Usuario passageiro) {
        this.passageiro = passageiro;
    }

    public Usuario getMotorista() {
        return motorista;
    }

    public void setMotorista(Usuario motorista) {
        this.motorista = motorista;
    }

    public Destino getDestino() {
        return destino;
    }

    public void setDestino(Destino destino) {
        this.destino = destino;
    }

    public long getInicioRequisicao() {
        return inicioRequisicao;
    }

    public void setInicioRequisicao(long inicioRequisicao) {
        this.inicioRequisicao = inicioRequisicao;
    }
}


