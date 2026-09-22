package br.com.facilita.apuracao.api;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

public class ListarApuracoesRequest {

    @Size(max = 7, message = "O mês de referência deve ter no máximo 7 caracteres.")
    private String mesReferencia;

    private Boolean somentePendentes;
    private Boolean possuiAnexo;

    @Size(max = 100, message = "A busca deve ter no máximo 100 caracteres.")
    private String busca;

    @Size(max = 50, message = "O campo de busca deve ter no máximo 50 caracteres.")
    private String campo;

    @Min(value = 0, message = "A página deve ser maior ou igual a zero.")
    private Integer pagina;

    @Min(value = 1, message = "O tamanho da página deve ser maior que zero.")
    @Max(value = 500, message = "O tamanho da página não pode exceder 500 registros.")
    private Integer tamanhoPagina;

    @Size(max = 50, message = "A ordenação deve ter no máximo 50 caracteres.")
    private String ordenacao;

    @Size(max = 4, message = "A direção deve ter no máximo 4 caracteres.")
    @Pattern(regexp = "(?i)(ASC|DESC)", message = "A direção deve ser ASC ou DESC.")
    private String direcao;

    public ListarApuracoesRequest() {
    }

    public String getMesReferencia() {
        return mesReferencia;
    }

    public void setMesReferencia(String mesReferencia) {
        this.mesReferencia = mesReferencia;
    }

    public Boolean getSomentePendentes() {
        return somentePendentes;
    }

    public void setSomentePendentes(Boolean somentePendentes) {
        this.somentePendentes = somentePendentes;
    }

    public Boolean getPossuiAnexo() {
        return possuiAnexo;
    }

    public void setPossuiAnexo(Boolean possuiAnexo) {
        this.possuiAnexo = possuiAnexo;
    }

    public String getBusca() {
        return busca;
    }

    public void setBusca(String busca) {
        this.busca = busca;
    }

    public String getCampo() {
        return campo;
    }

    public void setCampo(String campo) {
        this.campo = campo;
    }

    public Integer getPagina() {
        return pagina;
    }

    public void setPagina(Integer pagina) {
        this.pagina = pagina;
    }

    public Integer getTamanhoPagina() {
        return tamanhoPagina;
    }

    public void setTamanhoPagina(Integer tamanhoPagina) {
        this.tamanhoPagina = tamanhoPagina;
    }

    public String getOrdenacao() {
        return ordenacao;
    }

    public void setOrdenacao(String ordenacao) {
        this.ordenacao = ordenacao;
    }

    public String getDirecao() {
        return direcao;
    }

    public void setDirecao(String direcao) {
        this.direcao = direcao;
    }
}
