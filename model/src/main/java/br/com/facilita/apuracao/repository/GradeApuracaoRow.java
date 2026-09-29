package br.com.facilita.apuracao.repository;

import java.math.BigDecimal;

import br.com.sankhya.studio.persistence.NativeQuery;

/** Projecao da grade. Getters seguem o alias da coluna. */
@NativeQuery.Result
public interface GradeApuracaoRow {

    BigDecimal getNuapuracao();

    BigDecimal getCodconta();

    BigDecimal getNumcontrato();

    String getDtvenc();

    BigDecimal getValor();

    String getConfirmado();

    String getPossuianexo();
}
