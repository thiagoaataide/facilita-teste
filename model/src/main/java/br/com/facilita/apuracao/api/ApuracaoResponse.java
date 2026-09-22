package br.com.facilita.apuracao.api;

import java.math.BigDecimal;

/** Dados não sensíveis permitidos para a lista e o detalhe da apuração. */
public class ApuracaoResponse {

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

    public ApuracaoResponse() {
    }

    public Integer getNuApuracao() {
        return nuApuracao;
    }

    public void setNuApuracao(Integer nuApuracao) {
        this.nuApuracao = nuApuracao;
    }

    public String getCodConta() {
        return codConta;
    }

    public void setCodConta(String codConta) {
        this.codConta = codConta;
    }

    public String getNumContrato() {
        return numContrato;
    }

    public void setNumContrato(String numContrato) {
        this.numContrato = numContrato;
    }

    public String getNuNota() {
        return nuNota;
    }

    public void setNuNota(String nuNota) {
        this.nuNota = nuNota;
    }

    public String getSequenciaCon() {
        return sequenciaCon;
    }

    public void setSequenciaCon(String sequenciaCon) {
        this.sequenciaCon = sequenciaCon;
    }

    public String getOperadora() {
        return operadora;
    }

    public void setOperadora(String operadora) {
        this.operadora = operadora;
    }

    public String getCliente() {
        return cliente;
    }

    public void setCliente(String cliente) {
        this.cliente = cliente;
    }

    public String getCodVend() {
        return codVend;
    }

    public void setCodVend(String codVend) {
        this.codVend = codVend;
    }

    public String getReferencia() {
        return referencia;
    }

    public void setReferencia(String referencia) {
        this.referencia = referencia;
    }

    public String getReferenciaAdiada() {
        return referenciaAdiada;
    }

    public void setReferenciaAdiada(String referenciaAdiada) {
        this.referenciaAdiada = referenciaAdiada;
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

    public BigDecimal getValorRef() {
        return valorRef;
    }

    public void setValorRef(BigDecimal valorRef) {
        this.valorRef = valorRef;
    }

    public String getConfirmado() {
        return confirmado;
    }

    public void setConfirmado(String confirmado) {
        this.confirmado = confirmado;
    }

    public String getAuditoriaFinalizada() {
        return auditoriaFinalizada;
    }

    public void setAuditoriaFinalizada(String auditoriaFinalizada) {
        this.auditoriaFinalizada = auditoriaFinalizada;
    }

    public String getEmailEnviado() {
        return emailEnviado;
    }

    public void setEmailEnviado(String emailEnviado) {
        this.emailEnviado = emailEnviado;
    }

    public String getFaturamentoLiberado() {
        return faturamentoLiberado;
    }

    public void setFaturamentoLiberado(String faturamentoLiberado) {
        this.faturamentoLiberado = faturamentoLiberado;
    }

    public String getNuFila() {
        return nuFila;
    }

    public void setNuFila(String nuFila) {
        this.nuFila = nuFila;
    }

    public String getPlano() {
        return plano;
    }

    public void setPlano(String plano) {
        this.plano = plano;
    }

    public String getIdInstPrn() {
        return idInstPrn;
    }

    public void setIdInstPrn(String idInstPrn) {
        this.idInstPrn = idInstPrn;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getPossuiAnexo() {
        return possuiAnexo;
    }

    public void setPossuiAnexo(String possuiAnexo) {
        this.possuiAnexo = possuiAnexo;
    }
}
