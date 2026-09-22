package br.com.facilita.apuracao.api;

import javax.validation.constraints.Size;

public class SolicitarNovaAuditoriaRequest extends VersionedApuracaoRequest {

    @Size(max = 500, message = "O motivo deve ter no máximo 500 caracteres.")
    private String motivo;

    public SolicitarNovaAuditoriaRequest() {
        super();
    }

    public String getMotivo() {
        return motivo;
    }

    public void setMotivo(String motivo) {
        this.motivo = motivo;
    }
}
