package com.faculdade.sensor;

import java.util.Objects;

public record Medicao(String sensorId, double valor, String unidade) {

    public Medicao {
        Objects.requireNonNull(sensorId, "sensorId não pode ser nulo");
        Objects.requireNonNull(unidade, "unidade não pode ser nula");
        if (!Double.isFinite(valor)) {
            throw new IllegalArgumentException("valor deve ser finito: " + valor);
        }
    }
}
