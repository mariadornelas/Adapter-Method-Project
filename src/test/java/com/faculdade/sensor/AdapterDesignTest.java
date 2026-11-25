package com.faculdade.sensor;

import com.faculdade.sensor.legado.AcelerometroMqtt;
import com.faculdade.sensor.legado.TermometroLegadoFahrenheit;
import com.faculdade.sensor.legado.TransdutorModbus;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

class AdapterDesignTest {

    @Test
    void equipamentosLegadosNaoDevemConhecerOAlvo() {
        for (Class<?> c : List.of(TermometroLegadoFahrenheit.class, TransdutorModbus.class, AcelerometroMqtt.class)) {
            assertFalse(SensorMedicao.class.isAssignableFrom(c), c.getSimpleName());
            assertEquals("com.faculdade.sensor.legado", c.getPackageName());
        }
    }

    @Test
    void todoAdaptadorDeveImplementarOAlvo() {
        for (Class<?> c : List.of(AdaptadorTermometro.class, AdaptadorTransdutorModbus.class,
                AdaptadorAcelerometroMqtt.class, AdaptadorTermometroPorHeranca.class)) {
            assertTrue(SensorMedicao.class.isAssignableFrom(c), c.getSimpleName());
        }
    }

    @Test
    void adaptadoresDeObjetoDevemUsarComposicaoENaoHeranca() {
        for (Class<?> c : List.of(AdaptadorTermometro.class, AdaptadorTransdutorModbus.class,
                AdaptadorAcelerometroMqtt.class)) {
            assertEquals(Object.class, c.getSuperclass(), c.getSimpleName());
        }
        assertEquals(TermometroLegadoFahrenheit.class, AdaptadorTermometroPorHeranca.class.getSuperclass());
    }

    @Test
    void cadaAdaptadorDeObjetoDeveGuardarOEquipamentoQueAdapta() {
        assertTrue(temCampoDoTipo(AdaptadorTermometro.class, TermometroLegadoFahrenheit.class));
        assertTrue(temCampoDoTipo(AdaptadorTransdutorModbus.class, TransdutorModbus.class));
        assertTrue(temCampoDoTipo(AdaptadorAcelerometroMqtt.class, AcelerometroMqtt.class));
    }

    private static boolean temCampoDoTipo(Class<?> classe, Class<?> tipo) {
        return Arrays.stream(classe.getDeclaredFields()).anyMatch(f -> f.getType() == tipo);
    }

    @Test
    void clienteNaoDeveDependerDeAdaptadoresNemDoLegado() {
        for (Field f : PainelMonitoramento.class.getDeclaredFields()) {
            String tipo = f.getGenericType().getTypeName();
            assertFalse(tipo.contains("legado"), f.getName() + ": " + tipo);
            assertFalse(tipo.contains("Adaptador"), f.getName() + ": " + tipo);
        }
    }

    @Test
    void adaptadoresDiferentesDevemSerUsadosPelaMesmaInterfaceComUnidadesPadronizadas() {
        TermometroLegadoFahrenheit t = new TermometroLegadoFahrenheit("T");
        t.simular(100.0);
        TransdutorModbus m = new TransdutorModbus(1);
        m.escreverRegistrador(0, 1000);
        AcelerometroMqtt a = new AcelerometroMqtt();
        a.receber("vib=1.0;unid=in/s");

        List<SensorMedicao> todos = List.of(new AdaptadorTermometro(t),
                new AdaptadorTransdutorModbus(m, 0, 16.0), new AdaptadorAcelerometroMqtt("V", a));

        Set<String> unidades = todos.stream().map(s -> s.medir().unidade()).collect(Collectors.toSet());
        assertEquals(Set.of("°C", "bar", "mm/s"), unidades);
        assertTrue(todos.stream().allMatch(s -> Double.isFinite(s.medir().valor())));
    }

    @Test
    void medicaoDeveRecusarValoresInvalidos() {
        assertThrows(IllegalArgumentException.class, () -> new Medicao("x", Double.NaN, "u"));
        assertThrows(IllegalArgumentException.class, () -> new Medicao("x", Double.POSITIVE_INFINITY, "u"));
        assertThrows(NullPointerException.class, () -> new Medicao(null, 1, "u"));
        assertThrows(NullPointerException.class, () -> new Medicao("x", 1, null));
    }

    @Test
    void excecaoDoAlvoDeveGuardarACausaDoEquipamento() {
        Throwable causa = new IndexOutOfBoundsException("x");

        LeituraIndisponivelException e = new LeituraIndisponivelException("msg", causa);

        assertSame(causa, e.getCause());
        assertEquals("msg", e.getMessage());
    }
}
