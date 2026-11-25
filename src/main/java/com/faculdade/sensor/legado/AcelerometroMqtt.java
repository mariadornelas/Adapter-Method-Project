package com.faculdade.sensor.legado;

/**
 * ADAPTEE 3: acelerômetro de terceiros que publica texto no formato
 * {@code "vib=3.20;unid=mm/s"}. Não oferece número nenhum, só a mensagem crua.
 */
public class AcelerometroMqtt {

    private String ultimoPayload = "";

    public String ultimoPayload() {
        return ultimoPayload;
    }

    /** Simulação: na vida real, o cliente MQTT do fabricante chamaria isto. */
    public void receber(String payload) {
        this.ultimoPayload = payload;
    }
}
