package br.com.facilita.apuracao.api;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

public class ConcluirAnexoRequest extends ApuracaoIdRequest {

    @NotNull(message = "O anexo e obrigatorio.")
    @Min(value = 1, message = "O anexo deve ser maior que zero.")
    private Integer nuAttach;

    @NotBlank(message = "O tipo do anexo e obrigatorio.")
    @Size(max = 2, message = "O tipo do anexo deve ter no maximo 2 caracteres.")
    private String tipo;

    public Integer getNuAttach() {
        return nuAttach;
    }

    public void setNuAttach(Integer nuAttach) {
        this.nuAttach = nuAttach;
    }

    public String getTipo() {
        return tipo;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }
}
