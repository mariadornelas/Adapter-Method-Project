package com.faculdade.sensor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class PainelMonitoramento {

    public record Resultado(String sensorId, Medicao medicao, Nivel nivel) {
    }

    private record Entrada(SensorMedicao sensor, double limiteAlerta, double limiteCritico) {
    }

    private final Map<String, Entrada> entradas = new LinkedHashMap<>();
    private final Map<String, String> falhas = new LinkedHashMap<>();

    public void registrar(SensorMedicao sensor, double limiteAlerta, double limiteCritico) {
        Objects.requireNonNull(sensor, "sensor não pode ser nulo");
        if (!Double.isFinite(limiteAlerta) || !Double.isFinite(limiteCritico) || !(limiteAlerta < limiteCritico)) {
            throw new IllegalArgumentException("o limite de alerta deve ser menor que o crítico");
        }
        if (entradas.containsKey(sensor.id())) {
            throw new IllegalArgumentException("já existe um sensor com o id " + sensor.id());
        }
        entradas.put(sensor.id(), new Entrada(sensor, limiteAlerta, limiteCritico));
    }

    public List<Resultado> atualizar() {
        falhas.clear();
        List<Resultado> resultados = new ArrayList<>();
        for (Map.Entry<String, Entrada> e : entradas.entrySet()) {
            Entrada entrada = e.getValue();
            try {
                Medicao m = entrada.sensor().medir();
                resultados.add(new Resultado(e.getKey(), m, classificar(m.valor(), entrada)));
            } catch (LeituraIndisponivelException ex) {
                falhas.put(e.getKey(), ex.getMessage());
            }
        }
        return resultados;
    }

    public Map<String, String> falhas() {
        return Collections.unmodifiableMap(new LinkedHashMap<>(falhas));
    }

    private static Nivel classificar(double valor, Entrada entrada) {
        if (valor >= entrada.limiteCritico()) return Nivel.CRITICO;
        if (valor >= entrada.limiteAlerta()) return Nivel.ALERTA;
        return Nivel.NORMAL;
    }
}
