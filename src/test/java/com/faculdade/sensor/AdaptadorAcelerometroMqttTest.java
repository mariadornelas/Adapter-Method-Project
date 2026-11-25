package com.faculdade.sensor;

import com.faculdade.sensor.legado.AcelerometroMqtt;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class AdaptadorAcelerometroMqttTest {

    private final AcelerometroMqtt legado = new AcelerometroMqtt();
    private final AdaptadorAcelerometroMqtt adaptador = new AdaptadorAcelerometroMqtt("V-01", legado);

    private Medicao medir(String payload) {
        legado.receber(payload);
        return adaptador.medir();
    }

    private void assertIndisponivel(String payload) {
        legado.receber(payload);
        assertThrows(LeituraIndisponivelException.class, adaptador::medir, "payload: " + payload);
    }

    @Test
    void deveLerValorEmMmPorSegundo() {
        Medicao m = medir("vib=3.20;unid=mm/s");

        assertEquals(3.2, m.valor(), 1e-12);
        assertEquals("mm/s", m.unidade());
        assertEquals("V-01", m.sensorId());
    }

    @Test
    void deveConverterPolegadasParaMilimetros() {
        Medicao m = medir("vib=0.5;unid=in/s");

        assertEquals(12.7, m.valor(), 1e-12);
        assertEquals("mm/s", m.unidade());
    }

    @Test
    void deveAceitarEspacosEOrdemQualquerEIgnorarCamposExtras() {
        assertEquals(3.2, medir(" unid = mm/s ; vib = 3.2 ").valor(), 1e-12);
        assertEquals(3.2, medir("temp=25.0;vib=3.2;unid=mm/s;bat=97").valor(), 1e-12);
        assertEquals(3.2, medir("vib=3.2;unid=mm/s;").valor(), 1e-12);
    }

    @Test
    void zeroDeveSerUmaLeituraValida() {
        assertEquals(0.0, medir("vib=0;unid=mm/s").valor());
    }

    @Test
    void semMensagemDeveVirarLeituraIndisponivel() {
        // o equipamento legado começa com payload vazio
        assertThrows(LeituraIndisponivelException.class, adaptador::medir);
        assertIndisponivel("");
        assertIndisponivel("   ");
        assertIndisponivel(null);
    }

    @Test
    void mensagemMalformadaDeveVirarLeituraIndisponivel() {
        assertIndisponivel("isto nao e uma medicao");
        assertIndisponivel("vib=3.2;unid");
        assertIndisponivel("vib");

        legado.receber("vib=3.2;unid=mm/s;lixo");
        LeituraIndisponivelException e = assertThrows(LeituraIndisponivelException.class, adaptador::medir);
        assertTrue(e.getMessage().contains("malformada"), e.getMessage());
    }

    @Test
    void mensagemVaziaDeveTerMotivoPropriosemConfundirComCampoAusente() {
        legado.receber("   ");

        LeituraIndisponivelException e = assertThrows(LeituraIndisponivelException.class, adaptador::medir);

        assertTrue(e.getMessage().contains("nenhuma mensagem"), e.getMessage());
    }

    @Test
    void camposAusentesDevemVirarLeituraIndisponivel() {
        assertIndisponivel("unid=mm/s");
        assertIndisponivel("vib=3.2");
    }

    @Test
    void numeroInvalidoDeveVirarLeituraIndisponivelComACausa() {
        legado.receber("vib=abc;unid=mm/s");

        LeituraIndisponivelException e = assertThrows(LeituraIndisponivelException.class, adaptador::medir);

        assertInstanceOf(NumberFormatException.class, e.getCause());
        assertIndisponivel("vib=3,2;unid=mm/s");
        assertIndisponivel("vib=;unid=mm/s");
    }

    @Test
    void naoFinitoENegativoDevemVirarLeituraIndisponivel() {
        assertIndisponivel("vib=NaN;unid=mm/s");
        assertIndisponivel("vib=Infinity;unid=mm/s");
        assertIndisponivel("vib=-1;unid=mm/s");
    }

    @Test
    void unidadeDesconhecidaDeveVirarLeituraIndisponivel() {
        assertIndisponivel("vib=3.2;unid=g");
        assertIndisponivel("vib=3.2;unid=MM/S");
    }

    @Test
    void deveRefletirSempreAUltimaMensagem() {
        assertEquals(1.0, medir("vib=1;unid=mm/s").valor());
        assertEquals(2.0, medir("vib=2;unid=mm/s").valor());
    }

    @Test
    void deveValidarConstrucao() {
        assertThrows(NullPointerException.class, () -> new AdaptadorAcelerometroMqtt(null, legado));
        assertThrows(IllegalArgumentException.class, () -> new AdaptadorAcelerometroMqtt(" ", legado));
        assertThrows(NullPointerException.class, () -> new AdaptadorAcelerometroMqtt("V", null));
    }
}
