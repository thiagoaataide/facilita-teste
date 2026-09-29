package br.com.facilita.apuracao.model;

import br.com.sankhya.studio.persistence.Column;
import br.com.sankhya.studio.persistence.DataType;
import br.com.sankhya.studio.persistence.Id;
import br.com.sankhya.studio.persistence.JapeEntity;

/** Mapeamento parcial e nativo de TSIANX, somente colunas usadas na listagem. */
@JapeEntity(
        entity = "AnexoSistema",
        table = "TSIANX",
        description = "Anexos Sistema",
        isNativeTable = true,
        isNativeInstance = true)
public class AnexoSistema {

    @Id
    @Column(name = "NUATTACH", dataType = DataType.INTEGER, description = "Sequencia")
    private Integer nuAttach;

    @Column(name = "NOMEINSTANCIA", dataType = DataType.TEXT, size = 30, description = "Instancia")
    private String nomeInstancia;

    @Column(name = "PKREGISTRO", dataType = DataType.TEXT, size = 512, description = "Registro")
    private String pkRegistro;

    @Column(name = "NOMEARQUIVO", dataType = DataType.TEXT, size = 1000, description = "Nome do arquivo")
    private String nomeArquivo;

    public String getNomeInstancia() {
        return nomeInstancia;
    }

    public void setNomeInstancia(String nomeInstancia) {
        this.nomeInstancia = nomeInstancia;
    }

    public Integer getNuAttach() {
        return nuAttach;
    }

    public void setNuAttach(Integer nuAttach) {
        this.nuAttach = nuAttach;
    }

    public String getPkRegistro() {
        return pkRegistro;
    }

    public void setPkRegistro(String pkRegistro) {
        this.pkRegistro = pkRegistro;
    }

    public String getNomeArquivo() {
        return nomeArquivo;
    }

    public void setNomeArquivo(String nomeArquivo) {
        this.nomeArquivo = nomeArquivo;
    }
}
