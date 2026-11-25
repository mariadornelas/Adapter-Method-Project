package com.faculdade.sensor;

import com.faculdade.sensor.legado.TermometroLegadoFahrenheit;

public class AdaptadorTermometroPorHeranca extends TermometroLegadoFahrenheit implements SensorMedicao {

    public AdaptadorTermometroPorHeranca(String serial) {
        super(serial);
    }

    @Override
    public String id() {
        return serial();
    }

    @Override
    public Medicao medir() {
        double fahrenheit = lerFahrenheit();
        if (!Double.isFinite(fahrenheit)) {
            throw new LeituraIndisponivelException("termômetro " + id() + " sem leitura válida");
        }
        return new Medicao(id(), Conversoes.fahrenheitParaCelsius(fahrenheit), "°C");
    }
}
