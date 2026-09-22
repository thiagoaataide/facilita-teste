package br.com.facilita.apuracao.domain;

import java.math.BigDecimal;

/** Comando restrito aos dois campos editáveis pela tela. */
public final class AtualizarApuracaoCommand extends ApuracaoCommand {

    private final BigDecimal valor;
    private final String dtVenc;

    public AtualizarApuracaoCommand(Integer nuApuracao, BigDecimal valor, String dtVenc,
            String expectedVersion, String idempotencyKey) {
        super(nuApuracao, expectedVersion, idempotencyKey);
        this.valor = valor;
        this.dtVenc = dtVenc;
    }

    public BigDecimal getValor() { return valor; }
    public String getDtVenc() { return dtVenc; }
}
