package br.com.facilita.apuracao.api;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

public class VersionedApuracaoRequest extends ApuracaoIdRequest {

    @NotBlank(message = "A versão observada é obrigatória.")
    @Size(max = 100, message = "A versão observada deve ter no máximo 100 caracteres.")
    private String version;

    @NotBlank(message = "A chave de idempotência é obrigatória.")
    @Size(max = 200, message = "A chave de idempotência deve ter no máximo 200 caracteres.")
    private String idempotencyKey;

    public VersionedApuracaoRequest() {
        super();
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getIdempotencyKey() {
        return idempotencyKey;
    }

    public void setIdempotencyKey(String idempotencyKey) {
        this.idempotencyKey = idempotencyKey;
    }
}
