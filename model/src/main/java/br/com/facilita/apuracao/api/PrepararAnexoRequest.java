package br.com.facilita.apuracao.api;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

public class PrepararAnexoRequest extends ApuracaoIdRequest {

    @NotBlank(message = "O nome do anexo e obrigatorio.")
    @Size(max = 255, message = "O nome do anexo deve ter no maximo 255 caracteres.")
    private String nameAttach;

    @NotBlank(message = "O tipo do anexo e obrigatorio.")
    @Size(max = 2, message = "O tipo do anexo deve ter no maximo 2 caracteres.")
    private String tipo;

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
