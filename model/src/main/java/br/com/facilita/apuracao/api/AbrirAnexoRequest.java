package br.com.facilita.apuracao.api;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

public class AbrirAnexoRequest extends ApuracaoIdRequest {

    @NotNull(message = "O anexo e obrigatorio.")
    @Min(value = 1, message = "O anexo deve ser maior que zero.")
    private Integer nuAttach;

    public Integer getNuAttach() {
        return nuAttach;
    }

    public void setNuAttach(Integer nuAttach) {
        this.nuAttach = nuAttach;
    }
}
