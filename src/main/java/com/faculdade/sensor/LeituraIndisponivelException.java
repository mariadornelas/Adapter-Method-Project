package com.faculdade.sensor;

public class LeituraIndisponivelException extends RuntimeException {

    public LeituraIndisponivelException(String mensagem) {
        super(mensagem);
    }

    public LeituraIndisponivelException(String mensagem, Throwable causa) {
        super(mensagem, causa);
    }
}
