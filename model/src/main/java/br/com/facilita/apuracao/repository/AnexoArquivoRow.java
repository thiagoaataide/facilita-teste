package br.com.facilita.apuracao.repository;

import java.math.BigDecimal;

import br.com.sankhya.studio.persistence.NativeQuery;

/** Projecao minima da listagem. Getters seguem o nome da coluna. */
@NativeQuery.Result
public interface AnexoArquivoRow {

    BigDecimal getNuattach();

    String getNomearquivo();
}
