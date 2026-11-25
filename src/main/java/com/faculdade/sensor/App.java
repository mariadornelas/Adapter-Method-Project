package com.faculdade.sensor;

import com.faculdade.sensor.legado.AcelerometroMqtt;
import com.faculdade.sensor.legado.TermometroLegadoFahrenheit;
import com.faculdade.sensor.legado.TransdutorModbus;

import java.util.Locale;

public class App {

    public static void main(String[] args) {
        // Equipamentos legados (não podem ser alterados).
        TermometroLegadoFahrenheit termometro = new TermometroLegadoFahrenheit("T-01");
        TransdutorModbus modbus = new TransdutorModbus(5);
        AcelerometroMqtt acelerometro = new AcelerometroMqtt();
        TermometroLegadoFahrenheit termometroMudo = new TermometroLegadoFahrenheit("T-02");

        termometro.simular(185.0);
        modbus.escreverRegistrador(2, 43008);
        acelerometro.receber("vib=3.20;unid=mm/s");

        // O painel só conhece SensorMedicao.
        PainelMonitoramento painel = new PainelMonitoramento();
        painel.registrar(new AdaptadorTermometro(termometro), 80, 90);
        painel.registrar(new AdaptadorTransdutorModbus(modbus, 2, 16.0), 8, 10);
        painel.registrar(new AdaptadorAcelerometroMqtt("V-01", acelerometro), 4.5, 7.1);
        painel.registrar(new AdaptadorTermometro(termometroMudo), 80, 90);

        System.out.println("=== Todos na mesma unidade padrão, pelo mesmo painel ===");
        mostrar(painel);

        System.out.println();
        System.out.println("=== O acelerômetro passa a mandar polegadas e depois lixo ===");
        acelerometro.receber("vib=0.40;unid=in/s");
        mostrar(painel);
        acelerometro.receber("isto nao e uma medicao");
        mostrar(painel);

        System.out.println();
        System.out.println("=== Adapter de classe (herança): mesmo resultado, mas expõe o legado ===");
        AdaptadorTermometroPorHeranca porHeranca = new AdaptadorTermometroPorHeranca("T-03");
        porHeranca.simular(212.0);
        System.out.printf(Locale.ROOT, "  medir() = %.1f °C   (e lerFahrenheit() também aparece: %.1f)%n",
                porHeranca.medir().valor(), porHeranca.lerFahrenheit());
    }

    private static void mostrar(PainelMonitoramento painel) {
        for (PainelMonitoramento.Resultado r : painel.atualizar()) {
            System.out.printf(Locale.ROOT, "  %-12s %8.2f %-5s %s%n",
                    r.sensorId(), r.medicao().valor(), r.medicao().unidade(), r.nivel());
        }
        painel.falhas().forEach((id, motivo) -> System.out.println("  FALHA " + id + ": " + motivo));
    }
}
