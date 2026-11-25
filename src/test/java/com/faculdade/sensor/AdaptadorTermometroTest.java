package com.faculdade.sensor;

import com.faculdade.sensor.legado.TermometroLegadoFahrenheit;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class AdaptadorTermometroTest {

    private final TermometroLegadoFahrenheit legado = new TermometroLegadoFahrenheit("T-01");
    private final AdaptadorTermometro adaptador = new AdaptadorTermometro(legado);

    @Test
    void deveConverterFahrenheitParaCelsius() {
        legado.simular(212.0);
        assertEquals(100.0, adaptador.medir().valor(), 1e-9);
        legado.simular(32.0);
        assertEquals(0.0, adaptador.medir().valor(), 1e-9);
        legado.simular(-40.0);
        assertEquals(-40.0, adaptador.medir().valor(), 1e-9);
        legado.simular(98.6);
        assertEquals(37.0, adaptador.medir().valor(), 1e-9);
    }

    @Test
    void deveEntregarNaUnidadePadraoComOIdDoEquipamento() {
        legado.simular(100.0);

        Medicao m = adaptador.medir();

        assertEquals("°C", m.unidade());
        assertEquals("T-01", m.sensorId());
        assertEquals("T-01", adaptador.id());
    }

    @Test
    void deveConsultarOEquipamentoACadaMedicaoSemGuardarLeitura() {
        legado.simular(32.0);
        assertEquals(0.0, adaptador.medir().valor(), 1e-9);

        legado.simular(212.0);

        assertEquals(100.0, adaptador.medir().valor(), 1e-9);
    }

    @Test
    void semLeituraDeveVirarLeituraIndisponivel() {
        // o termômetro legado começa em NaN: ainda não mediu nada
        assertThrows(LeituraIndisponivelException.class, adaptador::medir);
    }

    @Test
    void naoFinitoDeveVirarLeituraIndisponivel() {
        legado.simular(Double.NaN);
        assertThrows(LeituraIndisponivelException.class, adaptador::medir);
        legado.simular(Double.POSITIVE_INFINITY);
        assertThrows(LeituraIndisponivelException.class, adaptador::medir);
        legado.simular(Double.NEGATIVE_INFINITY);
        assertThrows(LeituraIndisponivelException.class, adaptador::medir);
    }

    @Test
    void deveExigirEquipamento() {
        assertThrows(NullPointerException.class, () -> new AdaptadorTermometro(null));
    }
}
