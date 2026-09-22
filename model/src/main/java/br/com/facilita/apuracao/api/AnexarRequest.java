package br.com.facilita.apuracao.api;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

public class AnexarRequest extends VersionedApuracaoRequest {

    @NotBlank(message = "A chave do arquivo temporário é obrigatória.")
    @Size(max = 200, message = "A chave do arquivo temporário deve ter no máximo 200 caracteres.")
    private String sessionKey;

    @NotBlank(message = "O nome do anexo é obrigatório.")
    @Size(max = 255, message = "O nome do anexo deve ter no máximo 255 caracteres.")
    private String nameAttach;

    @NotBlank(message = "O tipo do anexo é obrigatório.")
    @Size(max = 100, message = "O tipo do anexo deve ter no máximo 100 caracteres.")
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
