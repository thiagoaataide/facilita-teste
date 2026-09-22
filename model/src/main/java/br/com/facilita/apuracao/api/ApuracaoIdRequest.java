package br.com.facilita.apuracao.api;

import javax.validation.constraints.NotNull;

public class ApuracaoIdRequest {

    @NotNull(message = "A apuração é obrigatória.")
    private Integer nuApuracao;

    public ApuracaoIdRequest() {
    }

    public Integer getNuApuracao() {
        return nuApuracao;
    }

    public void setNuApuracao(Integer nuApuracao) {
        this.nuApuracao = nuApuracao;
    }
}
