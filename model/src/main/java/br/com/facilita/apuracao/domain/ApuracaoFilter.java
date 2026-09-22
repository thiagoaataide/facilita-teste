package br.com.facilita.apuracao.domain;

/** Filtros normalizados para a consulta da grade. */
public final class ApuracaoFilter {

    private final String mesReferencia;
    private final boolean somentePendentes;
    private final Boolean possuiAnexo;
    private final String busca;
    private final String campo;
    private final int pagina;
    private final int tamanhoPagina;
    private final String ordenacao;
    private final String direcao;

    public ApuracaoFilter(String mesReferencia, boolean somentePendentes, Boolean possuiAnexo,
            String busca, String campo, int pagina, int tamanhoPagina, String ordenacao,
            String direcao) {
        this.mesReferencia = mesReferencia;
        this.somentePendentes = somentePendentes;
        this.possuiAnexo = possuiAnexo;
        this.busca = busca;
        this.campo = campo;
        this.pagina = pagina;
        this.tamanhoPagina = tamanhoPagina;
        this.ordenacao = ordenacao;
        this.direcao = direcao;
    }

    public String getMesReferencia() { return mesReferencia; }
    public boolean isSomentePendentes() { return somentePendentes; }
    public Boolean getPossuiAnexo() { return possuiAnexo; }
    public String getBusca() { return busca; }
    public String getCampo() { return campo; }
    public int getPagina() { return pagina; }
    public int getTamanhoPagina() { return tamanhoPagina; }
    public String getOrdenacao() { return ordenacao; }
    public String getDirecao() { return direcao; }
}
