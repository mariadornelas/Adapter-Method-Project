package com.faculdade.sensor.legado;

/**
 * ADAPTEE 2: dispositivo Modbus de terceiros, que só fala em registradores de 16 bits
 * (inteiros). Quem sabe o que cada registrador significa é o integrador, não o dispositivo.
 * Lê um registrador inexistente com {@link ArrayIndexOutOfBoundsException}.
 */
public class TransdutorModbus {

    private final int enderecoEscravo;
    private final int[] registradores = new int[8];

    public TransdutorModbus(int enderecoEscravo) {
        this.enderecoEscravo = enderecoEscravo;
    }

    public int enderecoEscravo() {
        return enderecoEscravo;
    }

    public int lerRegistrador(int numero) {
        return registradores[numero];
    }

    /** Simulação: o hardware real atualizaria isto sozinho. */
    public void escreverRegistrador(int numero, int valor) {
        registradores[numero] = valor;
    }
}
