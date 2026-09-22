package br.com.facilita.apuracao.api;

import java.math.BigDecimal;
import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.Size;

public class AtualizarApuracaoRequest extends VersionedApuracaoRequest {

    @Size(max = 10, message = "O vencimento deve ter no máximo 10 caracteres.")
    private String dtVenc;

    @DecimalMin(value = "0", inclusive = true, message = "O valor deve ser maior ou igual a zero.")
    private BigDecimal valor;

    public AtualizarApuracaoRequest() {
        super();
    }

    public String getDtVenc() {
        return dtVenc;
    }

    public void setDtVenc(String dtVenc) {
        this.dtVenc = dtVenc;
    }

    public BigDecimal getValor() {
        return valor;
    }

    public void setValor(BigDecimal valor) {
        this.valor = valor;
    }
}
