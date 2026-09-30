package br.com.facilita.apuracao.repository;

import br.com.sankhya.studio.persistence.NativeQuery;

/** Dados usados so para compor o nome do anexo. */
@NativeQuery.Result
public interface NomeAnexoRow {

    String getIdentificador();

    String getNomeparc();

    String getCgccpf();

    String getDtvenc();
}
