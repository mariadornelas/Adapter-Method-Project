package com.faculdade.sensor;

import com.faculdade.sensor.legado.TransdutorModbus;

import java.util.Objects;

public class AdaptadorTransdutorModbus implements SensorMedicao {

    private static final int MAXIMO_16_BITS = 65535;

    private final TransdutorModbus dispositivo;
    private final int registrador;
    private final double faixaMaximaBar;

    public AdaptadorTransdutorModbus(TransdutorModbus dispositivo, int registrador, double faixaMaximaBar) {
        this.dispositivo = Objects.requireNonNull(dispositivo, "dispositivo não pode ser nulo");
        if (registrador < 0) {
            throw new IllegalArgumentException("registrador não pode ser negativo");
        }
        if (!Double.isFinite(faixaMaximaBar) || faixaMaximaBar <= 0) {
            throw new IllegalArgumentException("a faixa máxima deve ser positiva");
        }
        this.registrador = registrador;
        this.faixaMaximaBar = faixaMaximaBar;
    }

    @Override
    public String id() {
        return "MODBUS-" + dispositivo.enderecoEscravo() + "-R" + registrador;
    }

    @Override
    public Medicao medir() {
        int bruto;
        try {
            bruto = dispositivo.lerRegistrador(registrador);
        } catch (IndexOutOfBoundsException e) {
            throw new LeituraIndisponivelException("registrador " + registrador
                    + " inexistente no escravo " + dispositivo.enderecoEscravo(), e);
        }
        if (bruto < 0 || bruto > MAXIMO_16_BITS) {
            throw new LeituraIndisponivelException("valor " + bruto + " fora dos 16 bits em " + id());
        }
        return new Medicao(id(), bruto * faixaMaximaBar / MAXIMO_16_BITS, "bar");
    }
}
