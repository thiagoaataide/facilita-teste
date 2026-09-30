package br.com.facilita.apuracao.repository;

import java.math.BigDecimal;

import br.com.sankhya.studio.persistence.NativeQuery;

/** Anexo restrito a uma apuracao. */
@NativeQuery.Result
public interface AnexoEscopoRow {

    BigDecimal getNuattach();

    String getNomearquivo();

    String getChavearquivo();
}
