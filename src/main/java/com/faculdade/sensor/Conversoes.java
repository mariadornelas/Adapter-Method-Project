package com.faculdade.sensor;

final class Conversoes {

    static final double MILIMETROS_POR_POLEGADA = 25.4;

    private Conversoes() {
    }

    static double fahrenheitParaCelsius(double fahrenheit) {
        return (fahrenheit - 32) * 5 / 9;
    }
}
