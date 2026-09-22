package br.com.facilita.apuracao.domain;

import java.math.BigDecimal;

/**
 * Visão imutável do estado necessário aos casos de uso.
 *
 * <p>O adaptador de persistência é responsável por mapear os campos nativos
 * para esta visão. O domínio não conhece nomes de tabelas, JAPE ou tipos de
 * coluna.</p>
 */
public final class ApuracaoSnapshot {

    private final Integer nuApuracao;
    private final String codConta;
    private final String numContrato;
    private final String nuNota;
    private final String sequenciaCon;
    private final String operadora;
    private final String cliente;
    private final String codVend;
    private final String referencia;
    private final String referenciaAdiada;
    private final String dtVenc;
    private final BigDecimal valor;
    private final BigDecimal valorRef;
    private final String confirmado;
    private final String auditoriaFinalizada;
    private final String emailEnviado;
    private final String faturamentoLiberado;
    private final String nuFila;
    private final String plano;
    private final String idInstPrn;
    private final String version;
    private final String possuiAnexo;
    private final String novaAuditoriaPermitida;

    private ApuracaoSnapshot(Builder builder) {
        this.nuApuracao = builder.nuApuracao;
        this.codConta = builder.codConta;
        this.numContrato = builder.numContrato;
        this.nuNota = builder.nuNota;
        this.sequenciaCon = builder.sequenciaCon;
        this.operadora = builder.operadora;
        this.cliente = builder.cliente;
        this.codVend = builder.codVend;
        this.referencia = builder.referencia;
        this.referenciaAdiada = builder.referenciaAdiada;
        this.dtVenc = builder.dtVenc;
        this.valor = builder.valor;
        this.valorRef = builder.valorRef;
        this.confirmado = builder.confirmado;
        this.auditoriaFinalizada = builder.auditoriaFinalizada;
        this.emailEnviado = builder.emailEnviado;
        this.faturamentoLiberado = builder.faturamentoLiberado;
        this.nuFila = builder.nuFila;
        this.plano = builder.plano;
        this.idInstPrn = builder.idInstPrn;
        this.version = builder.version;
        this.possuiAnexo = builder.possuiAnexo;
        this.novaAuditoriaPermitida = builder.novaAuditoriaPermitida;
    }

    public static Builder builder() {
        return new Builder();
    }

    public Integer getNuApuracao() { return nuApuracao; }
    public String getCodConta() { return codConta; }
    public String getNumContrato() { return numContrato; }
    public String getNuNota() { return nuNota; }
    public String getSequenciaCon() { return sequenciaCon; }
    public String getOperadora() { return operadora; }
    public String getCliente() { return cliente; }
    public String getCodVend() { return codVend; }
    public String getReferencia() { return referencia; }
    public String getReferenciaAdiada() { return referenciaAdiada; }
    public String getDtVenc() { return dtVenc; }
    public BigDecimal getValor() { return valor; }
    public BigDecimal getValorRef() { return valorRef; }
    public String getConfirmado() { return confirmado; }
    public String getAuditoriaFinalizada() { return auditoriaFinalizada; }
    public String getEmailEnviado() { return emailEnviado; }
    public String getFaturamentoLiberado() { return faturamentoLiberado; }
    public String getNuFila() { return nuFila; }
    public String getPlano() { return plano; }
    public String getIdInstPrn() { return idInstPrn; }
    public String getVersion() { return version; }
    public String getPossuiAnexo() { return possuiAnexo; }

    /** O adaptador mapeia aqui a regra equivalente ao flag nativo. */
    public String getNovaAuditoriaPermitida() { return novaAuditoriaPermitida; }

    public boolean hasValidValue() {
        return valor != null && valor.signum() >= 0;
    }

    public boolean isConfirmed() { return isYes(confirmado); }
    public boolean isAuditFinalized() { return isYes(auditoriaFinalizada); }

    public boolean isEligibleForConfirmation() {
        return hasValidValue() && !isConfirmed() && !isAuditFinalized();
    }

    public boolean allowsNewAudit() { return isYes(novaAuditoriaPermitida); }

    private static boolean isYes(String value) {
        return value != null && "S".equalsIgnoreCase(value.trim());
    }

    public static final class Builder {
        private Integer nuApuracao;
        private String codConta;
        private String numContrato;
        private String nuNota;
        private String sequenciaCon;
        private String operadora;
        private String cliente;
        private String codVend;
        private String referencia;
        private String referenciaAdiada;
        private String dtVenc;
        private BigDecimal valor;
        private BigDecimal valorRef;
        private String confirmado;
        private String auditoriaFinalizada;
        private String emailEnviado;
        private String faturamentoLiberado;
        private String nuFila;
        private String plano;
        private String idInstPrn;
        private String version;
        private String possuiAnexo;
        private String novaAuditoriaPermitida;

        public Builder nuApuracao(Integer value) { this.nuApuracao = value; return this; }
        public Builder codConta(String value) { this.codConta = value; return this; }
        public Builder numContrato(String value) { this.numContrato = value; return this; }
        public Builder nuNota(String value) { this.nuNota = value; return this; }
        public Builder sequenciaCon(String value) { this.sequenciaCon = value; return this; }
        public Builder operadora(String value) { this.operadora = value; return this; }
        public Builder cliente(String value) { this.cliente = value; return this; }
        public Builder codVend(String value) { this.codVend = value; return this; }
        public Builder referencia(String value) { this.referencia = value; return this; }
        public Builder referenciaAdiada(String value) { this.referenciaAdiada = value; return this; }
        public Builder dtVenc(String value) { this.dtVenc = value; return this; }
        public Builder valor(BigDecimal value) { this.valor = value; return this; }
        public Builder valorRef(BigDecimal value) { this.valorRef = value; return this; }
        public Builder confirmado(String value) { this.confirmado = value; return this; }
        public Builder auditoriaFinalizada(String value) { this.auditoriaFinalizada = value; return this; }
        public Builder emailEnviado(String value) { this.emailEnviado = value; return this; }
        public Builder faturamentoLiberado(String value) { this.faturamentoLiberado = value; return this; }
        public Builder nuFila(String value) { this.nuFila = value; return this; }
        public Builder plano(String value) { this.plano = value; return this; }
        public Builder idInstPrn(String value) { this.idInstPrn = value; return this; }
        public Builder version(String value) { this.version = value; return this; }
        public Builder possuiAnexo(String value) { this.possuiAnexo = value; return this; }
        public Builder novaAuditoriaPermitida(String value) { this.novaAuditoriaPermitida = value; return this; }

        public ApuracaoSnapshot build() { return new ApuracaoSnapshot(this); }
    }
}
