package br.com.facilita.apuracao.repository;

import java.math.BigDecimal;

import br.com.sankhya.studio.persistence.NativeQuery;

/** Projecao do detalhe. Getters seguem o alias da coluna. */
@NativeQuery.Result
public interface DetalheApuracaoRow {

    BigDecimal getNuapuracao();

    String getCodconta();

    String getNumcontrato();

    String getNunota();

    String getSequenciacon();

    String getOperadora();

    String getCliente();

    String getCodvend();

    String getReferencia();

    String getReferenciaadiada();

    String getDtvenc();

    BigDecimal getValor();

    BigDecimal getValorref();

    String getConfirmado();

    String getAuditoriafinalizada();

    String getEmailenviado();

    String getFaturamentoliberado();

    String getNufila();

    String getPlano();

    String getIdinstprn();

    String getPossuianexo();
}
