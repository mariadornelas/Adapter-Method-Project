package com.faculdade.sensor;

import com.faculdade.sensor.legado.TermometroLegadoFahrenheit;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class AdaptadorPorHerancaTest {

    @Test
    void deveDarOMesmoResultadoQueOAdaptadorDeObjeto() {
        TermometroLegadoFahrenheit legado = new TermometroLegadoFahrenheit("T-9");
        AdaptadorTermometro porComposicao = new AdaptadorTermometro(legado);
        AdaptadorTermometroPorHeranca porHeranca = new AdaptadorTermometroPorHeranca("T-9");

        for (double f : List.of(-40.0, 0.0, 32.0, 98.6, 185.0, 212.0, 1000.0)) {
            legado.simular(f);
            porHeranca.simular(f);
            assertEquals(porComposicao.medir(), porHeranca.medir(), "f = " + f);
        }
    }

    @Test
    void deveSerAoMesmoTempoOLegadoEOAlvo() {
        AdaptadorTermometroPorHeranca a = new AdaptadorTermometroPorHeranca("T-9");

        assertInstanceOf(TermometroLegadoFahrenheit.class, a);
        assertInstanceOf(SensorMedicao.class, a);
        assertEquals("T-9", a.id());
    }

    @Test
    void deveVazarAInterfaceDoLegadoParaQuemRecebeOObjeto() {
        AdaptadorTermometroPorHeranca a = new AdaptadorTermometroPorHeranca("T-9");
        a.simular(212.0);

        assertEquals(212.0, a.lerFahrenheit());
        assertEquals(100.0, a.medir().valor(), 1e-9);
    }

    @Test
    void semLeituraDeveVirarLeituraIndisponivel() {
        assertThrows(LeituraIndisponivelException.class, () -> new AdaptadorTermometroPorHeranca("T-9").medir());
        AdaptadorTermometroPorHeranca a = new AdaptadorTermometroPorHeranca("T-9");
        a.simular(Double.POSITIVE_INFINITY);
        assertThrows(LeituraIndisponivelException.class, a::medir);
    }
}
