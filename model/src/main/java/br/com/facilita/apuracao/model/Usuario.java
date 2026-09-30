package br.com.facilita.apuracao.model;

import java.math.BigDecimal;

import br.com.sankhya.studio.persistence.Column;
import br.com.sankhya.studio.persistence.DataType;
import br.com.sankhya.studio.persistence.Id;
import br.com.sankhya.studio.persistence.JapeEntity;

/** Mapeamento parcial e nativo de TSIUSU. So a chave; a flag e lida por SQL nativo. */
@JapeEntity(
        entity = "Usuario",
        table = "TSIUSU",
        description = "Usuario",
        isNativeTable = true,
        isNativeInstance = true)
public class Usuario {

    @Id
    @Column(name = "CODUSU", dataType = DataType.INTEGER, description = "Codigo do usuario")
    private BigDecimal codUsu;

    public BigDecimal getCodUsu() {
        return codUsu;
    }

    public void setCodUsu(BigDecimal codUsu) {
        this.codUsu = codUsu;
    }
}
