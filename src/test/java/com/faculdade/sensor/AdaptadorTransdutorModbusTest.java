package com.faculdade.sensor;

import com.faculdade.sensor.legado.TransdutorModbus;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class AdaptadorTransdutorModbusTest {

    private final TransdutorModbus legado = new TransdutorModbus(5);
    private final AdaptadorTransdutorModbus adaptador = new AdaptadorTransdutorModbus(legado, 2, 16.0);

    @Test
    void deveEscalarOsDezesseisBitsParaAFaixaEmBar() {
        legado.escreverRegistrador(2, 0);
        assertEquals(0.0, adaptador.medir().valor(), 1e-12);

        legado.escreverRegistrador(2, 65535);
        assertEquals(16.0, adaptador.medir().valor(), 1e-12);

        legado.escreverRegistrador(2, 32768);
        assertEquals(32768 * 16.0 / 65535, adaptador.medir().valor(), 1e-12);
    }

    @Test
    void faixaDiferenteDeveMudarAEscala() {
        AdaptadorTransdutorModbus de10bar = new AdaptadorTransdutorModbus(legado, 2, 10.0);
        legado.escreverRegistrador(2, 65535);

        assertEquals(10.0, de10bar.medir().valor(), 1e-12);
    }

    @Test
    void deveEntregarEmBarComIdQueIdentificaEscravoERegistrador() {
        legado.escreverRegistrador(2, 100);

        Medicao m = adaptador.medir();

        assertEquals("bar", m.unidade());
        assertEquals("MODBUS-5-R2", adaptador.id());
        assertEquals("MODBUS-5-R2", m.sensorId());
    }

    @Test
    void deveLerSomenteORegistradorConfigurado() {
        legado.escreverRegistrador(1, 65535);
        legado.escreverRegistrador(2, 0);

        assertEquals(0.0, adaptador.medir().valor(), 1e-12);
    }

    @Test
    void registradorInexistenteDeveVirarLeituraIndisponivelComACausa() {
        AdaptadorTransdutorModbus alem = new AdaptadorTransdutorModbus(legado, 99, 16.0);

        LeituraIndisponivelException e = assertThrows(LeituraIndisponivelException.class, alem::medir);

        assertInstanceOf(IndexOutOfBoundsException.class, e.getCause());
    }

    @Test
    void valorForaDosDezesseisBitsDeveVirarLeituraIndisponivel() {
        legado.escreverRegistrador(2, 65535);
        assertDoesNotThrow(adaptador::medir);          // 65535 ainda vale

        legado.escreverRegistrador(2, 65536);
        assertThrows(LeituraIndisponivelException.class, adaptador::medir);

        legado.escreverRegistrador(2, -1);
        assertThrows(LeituraIndisponivelException.class, adaptador::medir);
    }

    @Test
    void deveValidarConstrucao() {
        assertThrows(NullPointerException.class, () -> new AdaptadorTransdutorModbus(null, 0, 16.0));
        assertThrows(IllegalArgumentException.class, () -> new AdaptadorTransdutorModbus(legado, -1, 16.0));
        assertThrows(IllegalArgumentException.class, () -> new AdaptadorTransdutorModbus(legado, 0, 0));
        assertThrows(IllegalArgumentException.class, () -> new AdaptadorTransdutorModbus(legado, 0, -5));
        assertThrows(IllegalArgumentException.class, () -> new AdaptadorTransdutorModbus(legado, 0, Double.NaN));
    }
}
