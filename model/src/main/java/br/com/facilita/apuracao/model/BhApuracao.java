package br.com.facilita.apuracao.model;

import java.math.BigDecimal;
import java.time.LocalDate;

import br.com.sankhya.studio.persistence.Column;
import br.com.sankhya.studio.persistence.DataType;
import br.com.sankhya.studio.persistence.Id;
import br.com.sankhya.studio.persistence.JapeEntity;

/** Mapeamento parcial, nativo e somente de campos comprovados de BH_FACAPU. */
@JapeEntity(
        entity = "bhApuracao",
        table = "BH_FACAPU",
        description = "Apuração",
        isNativeTable = true,
        isNativeInstance = true)
public class BhApuracao {

    @Id
    @Column(name = "NUAPURACAO", dataType = DataType.INTEGER, description = "Sequência")
    private Integer nuApuracao;

    @Column(name = "CODCONTA", dataType = DataType.INTEGER, description = "Código da conta")
    private Integer codConta;

    @Column(name = "NUMCONTRATO", dataType = DataType.INTEGER, description = "Número do contrato")
    private Integer numContrato;

    @Column(name = "NUNOTA", dataType = DataType.INTEGER, description = "Número da nota")
    private Integer nuNota;

    @Column(name = "SEQUENCIACON", dataType = DataType.INTEGER, description = "Sequência do contrato")
    private Integer sequenciaCon;

    @Column(name = "OPERADORA", dataType = DataType.INTEGER, description = "Operadora")
    private Integer operadora;

    @Column(name = "CLIENTE", dataType = DataType.INTEGER, description = "Cliente")
    private Integer cliente;

    @Column(name = "CODVEND", dataType = DataType.INTEGER, description = "Vendedor")
    private Integer codVend;

    @Column(name = "REFERENCIA", dataType = DataType.DATE, description = "Referência")
    private LocalDate referencia;

    @Column(name = "REFERENCIAADIADA", dataType = DataType.DATE,
            description = "Referência para faturamento adiada")
    private LocalDate referenciaAdiada;

    @Column(name = "DTVENC", dataType = DataType.DATE, description = "Data de vencimento")
    private LocalDate dtVenc;

    @Column(name = "VALOR", dataType = DataType.DECIMAL, description = "Valor")
    private BigDecimal valor;

    @Column(name = "VALORREF", dataType = DataType.DECIMAL, description = "Valor de referência")
    private BigDecimal valorRef;

    @Column(name = "CONFIRMADO", dataType = DataType.CHECKBOX, description = "Apurado")
    private Boolean confirmado;

    @Column(name = "AUDITORIAFINALIZADA", dataType = DataType.CHECKBOX,
            description = "Auditoria finalizada")
    private Boolean auditoriaFinalizada;

    @Column(name = "EMAILENVIADO", dataType = DataType.CHECKBOX,
            description = "E-mail enviado")
    private Boolean emailEnviado;

    @Column(name = "FATURAMENTOLIBERADO", dataType = DataType.CHECKBOX,
            description = "Faturamento liberado")
    private Boolean faturamentoLiberado;

    @Column(name = "NUFILA", dataType = DataType.INTEGER, description = "Número único da fila")
    private Integer nuFila;

    @Column(name = "PLANO", dataType = DataType.INTEGER, description = "Plano")
    private Integer plano;

    @Column(name = "IDINSTPRN", dataType = DataType.INTEGER,
            description = "Identificador da instância do workflow")
    private Integer idInstPrn;

    @Column(name = "POSSUIANEXO", dataType = DataType.CHECKBOX, description = "Possui anexo")
    private Boolean possuiAnexo;

    @Column(name = "SEQUENCIAFATURAMENTO", dataType = DataType.INTEGER,
            description = "Sequência de faturamento")
    private Integer sequenciaFaturamento;

    @Column(name = "LINK", dataType = DataType.TEXT, size = 1000, description = "Link")
    private String link;

    @Column(name = "TAMANHOANEXO", dataType = DataType.DECIMAL,
            description = "Tamanho do anexo")
    private BigDecimal tamanhoAnexo;

    public Integer getNuApuracao() { return nuApuracao; }
    public void setNuApuracao(Integer value) { this.nuApuracao = value; }
    public Integer getCodConta() { return codConta; }
    public void setCodConta(Integer value) { this.codConta = value; }
    public Integer getNumContrato() { return numContrato; }
    public void setNumContrato(Integer value) { this.numContrato = value; }
    public Integer getNuNota() { return nuNota; }
    public void setNuNota(Integer value) { this.nuNota = value; }
    public Integer getSequenciaCon() { return sequenciaCon; }
    public void setSequenciaCon(Integer value) { this.sequenciaCon = value; }
    public Integer getOperadora() { return operadora; }
    public void setOperadora(Integer value) { this.operadora = value; }
    public Integer getCliente() { return cliente; }
    public void setCliente(Integer value) { this.cliente = value; }
    public Integer getCodVend() { return codVend; }
    public void setCodVend(Integer value) { this.codVend = value; }
    public LocalDate getReferencia() { return referencia; }
    public void setReferencia(LocalDate value) { this.referencia = value; }
    public LocalDate getReferenciaAdiada() { return referenciaAdiada; }
    public void setReferenciaAdiada(LocalDate value) { this.referenciaAdiada = value; }
    public LocalDate getDtVenc() { return dtVenc; }
    public void setDtVenc(LocalDate value) { this.dtVenc = value; }
    public BigDecimal getValor() { return valor; }
    public void setValor(BigDecimal value) { this.valor = value; }
    public BigDecimal getValorRef() { return valorRef; }
    public void setValorRef(BigDecimal value) { this.valorRef = value; }
    public Boolean getConfirmado() { return confirmado; }
    public void setConfirmado(Boolean value) { this.confirmado = value; }
    public Boolean getAuditoriaFinalizada() { return auditoriaFinalizada; }
    public void setAuditoriaFinalizada(Boolean value) { this.auditoriaFinalizada = value; }
    public Boolean getEmailEnviado() { return emailEnviado; }
    public void setEmailEnviado(Boolean value) { this.emailEnviado = value; }
    public Boolean getFaturamentoLiberado() { return faturamentoLiberado; }
    public void setFaturamentoLiberado(Boolean value) { this.faturamentoLiberado = value; }
    public Integer getNuFila() { return nuFila; }
    public void setNuFila(Integer value) { this.nuFila = value; }
    public Integer getPlano() { return plano; }
    public void setPlano(Integer value) { this.plano = value; }
    public Integer getIdInstPrn() { return idInstPrn; }
    public void setIdInstPrn(Integer value) { this.idInstPrn = value; }
    public Boolean getPossuiAnexo() { return possuiAnexo; }
    public void setPossuiAnexo(Boolean value) { this.possuiAnexo = value; }
    public Integer getSequenciaFaturamento() { return sequenciaFaturamento; }
    public void setSequenciaFaturamento(Integer value) { this.sequenciaFaturamento = value; }
    public String getLink() { return link; }
    public void setLink(String value) { this.link = value; }
    public BigDecimal getTamanhoAnexo() { return tamanhoAnexo; }
    public void setTamanhoAnexo(BigDecimal value) { this.tamanhoAnexo = value; }
}
