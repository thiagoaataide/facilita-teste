package br.com.facilita.apuracao.repository;

import java.util.List;

import br.com.facilita.apuracao.model.AnexoSistema;
import br.com.sankhya.sdk.data.repository.JapeRepository;
import br.com.sankhya.studio.persistence.NativeQuery;
import br.com.sankhya.studio.stereotypes.Repository;

/** Leitura de anexos ja existentes em TSIANX. Sem gravacao. */
@Repository
public interface AnexoSistemaRepository extends JapeRepository<Integer, AnexoSistema> {

    @NativeQuery("SELECT NUATTACH, NOMEARQUIVO FROM TSIANX "
            + "WHERE NOMEINSTANCIA = :nomeInstancia AND PKREGISTRO = :pkRegistro "
            + "ORDER BY DHCAD DESC")
    List<AnexoArquivoRow> findArquivos(String nomeInstancia, String pkRegistro);
}
