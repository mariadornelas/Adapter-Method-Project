package com.faculdade.sensor.legado;

import java.util.Objects;

/**
 * ADAPTEE 1: driver de terceiros, que NÃO pode ser alterado. Entrega °F e usa NaN para
 * "sem leitura" (sensor desconectado ou ainda sem medir). Os métodos de simulação existem
 * só para a demonstração e os testes.
 */
public class TermometroLegadoFahrenheit {

    private final String serial;
    private double ultimaLeituraF = Double.NaN;

    public TermometroLegadoFahrenheit(String serial) {
        this.serial = Objects.requireNonNull(serial, "serial não pode ser nulo");
    }

    public String serial() {
        return serial;
    }

    public double lerFahrenheit() {
        return ultimaLeituraF;
    }

    /** Simulação: o hardware real atualizaria isto sozinho. */
    public void simular(double fahrenheit) {
        this.ultimaLeituraF = fahrenheit;
    }
}
