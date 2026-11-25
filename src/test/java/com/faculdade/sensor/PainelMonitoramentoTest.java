package com.faculdade.sensor;

import com.faculdade.sensor.legado.AcelerometroMqtt;
import com.faculdade.sensor.legado.TermometroLegadoFahrenheit;
import com.faculdade.sensor.legado.TransdutorModbus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/** O painel (cliente) lendo equipamentos incompatíveis sem saber disso. */
class PainelMonitoramentoTest {

    private TermometroLegadoFahrenheit termometro;
    private TransdutorModbus modbus;
    private AcelerometroMqtt acelerometro;
    private PainelMonitoramento painel;

    @BeforeEach
    void montar() {
        termometro = new TermometroLegadoFahrenheit("T-01");
        modbus = new TransdutorModbus(5);
        acelerometro = new AcelerometroMqtt();
        termometro.simular(185.0);                       // 85 °C   -> ALERTA (80/90)
        modbus.escreverRegistrador(2, 43008);            // 10,5 bar -> CRITICO (8/10)
        acelerometro.receber("vib=3.0;unid=mm/s");       // 3,0     -> NORMAL (4,5/7,1)

        painel = new PainelMonitoramento();
        painel.registrar(new AdaptadorTermometro(termometro), 80, 90);
        painel.registrar(new AdaptadorTransdutorModbus(modbus, 2, 16.0), 8, 10);
        painel.registrar(new AdaptadorAcelerometroMqtt("V-01", acelerometro), 4.5, 7.1);
    }

    @Test
    void deveClassificarEquipamentosDiferentesPelaMesmaRegra() {
        List<PainelMonitoramento.Resultado> r = painel.atualizar();

        assertEquals(3, r.size());
        assertEquals("T-01", r.get(0).sensorId());
        assertEquals(Nivel.ALERTA, r.get(0).nivel());
        assertEquals(85.0, r.get(0).medicao().valor(), 1e-9);
        assertEquals("MODBUS-5-R2", r.get(1).sensorId());
        assertEquals(Nivel.CRITICO, r.get(1).nivel());
        assertEquals(10.5, r.get(1).medicao().valor(), 1e-3);
        assertEquals("V-01", r.get(2).sensorId());
        assertEquals(Nivel.NORMAL, r.get(2).nivel());
        assertTrue(painel.falhas().isEmpty());
    }

    @Test
    void deveRefletirMudancasNosEquipamentosACadaAtualizacao() {
        painel.atualizar();

        acelerometro.receber("vib=0.4;unid=in/s");       // 10,16 mm/s -> CRITICO

        assertEquals(Nivel.CRITICO, painel.atualizar().get(2).nivel());
    }

    @Test
    void sensorComFalhaNaoDeveInterromperOsDemais() {
        painel.registrar(new AdaptadorTermometro(new TermometroLegadoFahrenheit("T-MUDO")), 80, 90);
        acelerometro.receber("lixo");

        List<PainelMonitoramento.Resultado> r = painel.atualizar();

        assertEquals(2, r.size());                       // termômetro e Modbus seguem funcionando
        assertEquals(2, painel.falhas().size());
        assertTrue(painel.falhas().containsKey("V-01"));
        assertTrue(painel.falhas().containsKey("T-MUDO"));
        assertTrue(painel.falhas().get("T-MUDO").contains("T-MUDO"));
    }

    @Test
    void falhasDevemSerLimpasQuandoOEquipamentoSeRecupera() {
        acelerometro.receber("lixo");
        painel.atualizar();
        assertEquals(1, painel.falhas().size());

        acelerometro.receber("vib=1;unid=mm/s");
        painel.atualizar();

        assertTrue(painel.falhas().isEmpty());
    }

    @Test
    void falhasDevemSerSomenteLeitura() {
        acelerometro.receber("lixo");
        painel.atualizar();

        assertThrows(UnsupportedOperationException.class, () -> painel.falhas().clear());
    }

    @Test
    void painelNaoDevePrecisarDeAdaptadorParaFuncionar() {
        // qualquer implementação do alvo serve: o painel não conhece os adaptadores
        PainelMonitoramento outro = new PainelMonitoramento();
        outro.registrar(new SensorMedicao() {
            public String id() { return "FIXO"; }
            public Medicao medir() { return new Medicao("FIXO", 95.0, "°C"); }
        }, 80, 90);

        assertEquals(Nivel.CRITICO, outro.atualizar().get(0).nivel());
    }

    @Test
    void limitesDevemSerInclusivos() {
        PainelMonitoramento outro = new PainelMonitoramento();
        TermometroLegadoFahrenheit t = new TermometroLegadoFahrenheit("T");
        outro.registrar(new AdaptadorTermometro(t), 80, 90);

        t.simular(176.0);   // 80 °C exatos
        assertEquals(Nivel.ALERTA, outro.atualizar().get(0).nivel());
        t.simular(194.0);   // 90 °C exatos
        assertEquals(Nivel.CRITICO, outro.atualizar().get(0).nivel());
        t.simular(175.0);   // 79,4 °C
        assertEquals(Nivel.NORMAL, outro.atualizar().get(0).nivel());
    }

    @Test
    void registroDeveValidarArgumentos() {
        assertThrows(NullPointerException.class, () -> painel.registrar(null, 1, 2));
        assertThrows(IllegalArgumentException.class,
                () -> painel.registrar(new AdaptadorAcelerometroMqtt("X", acelerometro), 5, 5));
        assertThrows(IllegalArgumentException.class,
                () -> painel.registrar(new AdaptadorAcelerometroMqtt("X", acelerometro), 6, 5));
        assertThrows(IllegalArgumentException.class,
                () -> painel.registrar(new AdaptadorAcelerometroMqtt("X", acelerometro), Double.NaN, 5));
    }

    @Test
    void idRepetidoDeveSerRecusado() {
        assertThrows(IllegalArgumentException.class,
                () -> painel.registrar(new AdaptadorTermometro(new TermometroLegadoFahrenheit("T-01")), 80, 90));
        assertEquals(3, painel.atualizar().size() + painel.falhas().size());
    }
}
