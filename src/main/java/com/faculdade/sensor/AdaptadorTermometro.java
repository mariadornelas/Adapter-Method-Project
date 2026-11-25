package com.faculdade.sensor;

import com.faculdade.sensor.legado.TermometroLegadoFahrenheit;

import java.util.Objects;

public class AdaptadorTermometro implements SensorMedicao {

    private final TermometroLegadoFahrenheit termometro;

    public AdaptadorTermometro(TermometroLegadoFahrenheit termometro) {
        this.termometro = Objects.requireNonNull(termometro, "termômetro não pode ser nulo");
    }

    @Override
    public String id() {
        return termometro.serial();
    }

    @Override
    public Medicao medir() {
        double fahrenheit = termometro.lerFahrenheit();
        if (!Double.isFinite(fahrenheit)) {
            throw new LeituraIndisponivelException("termômetro " + id() + " sem leitura válida");
        }
        return new Medicao(id(), Conversoes.fahrenheitParaCelsius(fahrenheit), "°C");
    }
}
