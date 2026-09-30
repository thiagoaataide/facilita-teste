package br.com.facilita.apuracao.api;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

public class AnexarRequest extends VersionedApuracaoRequest {

    @NotNull(message = "A chave do arquivo temporário é obrigatória.")
    @Size(min = 1, max = 200, message = "A chave do arquivo temporário deve ter no máximo 200 caracteres.")
    private String sessionKey;

    @NotNull(message = "O nome do anexo é obrigatório.")
    @Size(min = 1, max = 255, message = "O nome do anexo deve ter no máximo 255 caracteres.")
    private String nameAttach;

    @NotNull(message = "O tipo do anexo é obrigatório.")
    @Size(min = 1, max = 2, message = "O tipo do anexo deve ter no máximo 2 caracteres.")
    private String tipo;

    public AnexarRequest() {
        super();
    }

    public String getSessionKey() {
        return sessionKey;
    }

    public void setSessionKey(String sessionKey) {
        this.sessionKey = sessionKey;
    }

    public String getNameAttach() {
        return nameAttach;
    }

    public void setNameAttach(String nameAttach) {
        this.nameAttach = nameAttach;
    }

    public String getTipo() {
        return tipo;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }
}
