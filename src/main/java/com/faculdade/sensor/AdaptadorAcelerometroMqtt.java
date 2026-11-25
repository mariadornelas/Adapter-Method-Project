package com.faculdade.sensor;

import com.faculdade.sensor.legado.AcelerometroMqtt;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class AdaptadorAcelerometroMqtt implements SensorMedicao {

    private final String id;
    private final AcelerometroMqtt acelerometro;

    public AdaptadorAcelerometroMqtt(String id, AcelerometroMqtt acelerometro) {
        this.id = Objects.requireNonNull(id, "id não pode ser nulo");
        if (id.isBlank()) {
            throw new IllegalArgumentException("id não pode ser vazio");
        }
        this.acelerometro = Objects.requireNonNull(acelerometro, "acelerômetro não pode ser nulo");
    }

    @Override
    public String id() {
        return id;
    }

    @Override
    public Medicao medir() {
        String payload = acelerometro.ultimoPayload();
        if (payload == null || payload.isBlank()) {
            throw indisponivel("nenhuma mensagem recebida");
        }
        Map<String, String> campos = interpretar(payload);

        String textoVib = campos.get("vib");
        String unidade = campos.get("unid");
        if (textoVib == null) throw indisponivel("campo 'vib' ausente");
        if (unidade == null) throw indisponivel("campo 'unid' ausente");

        double valor;
        try {
            valor = Double.parseDouble(textoVib);
        } catch (NumberFormatException e) {
            throw new LeituraIndisponivelException("vibração inválida em " + id + ": '" + textoVib + "'", e);
        }
        if (!Double.isFinite(valor) || valor < 0) {
            throw indisponivel("vibração fora do aceitável: " + textoVib);
        }

        double emMmPorSegundo = switch (unidade) {
            case "mm/s" -> valor;
            case "in/s" -> valor * Conversoes.MILIMETROS_POR_POLEGADA;
            default -> throw indisponivel("unidade desconhecida: " + unidade);
        };
        return new Medicao(id, emMmPorSegundo, "mm/s");
    }

    private Map<String, String> interpretar(String payload) {
        Map<String, String> campos = new HashMap<>();
        for (String par : payload.split(";")) {
            if (par.isBlank()) {
                continue;
            }
            int igual = par.indexOf('=');
            if (igual < 0) {
                throw indisponivel("mensagem malformada: '" + par.trim() + "'");
            }
            campos.put(par.substring(0, igual).trim(), par.substring(igual + 1).trim());
        }
        return campos;
    }

    private LeituraIndisponivelException indisponivel(String motivo) {
        return new LeituraIndisponivelException("acelerômetro " + id + ": " + motivo);
    }
}
