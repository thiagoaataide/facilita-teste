package br.com.facilita.apuracao.repository;

import java.util.List;

import br.com.facilita.apuracao.model.AnexoSistema;
import br.com.sankhya.sdk.data.repository.JapeRepository;
import br.com.sankhya.studio.persistence.Modifying;
import br.com.sankhya.studio.persistence.NativeQuery;
import br.com.sankhya.studio.persistence.Parameter;
import br.com.sankhya.studio.stereotypes.Repository;

/** Leitura de anexos ja existentes em TSIANX. Sem gravacao. */
@Repository
public interface AnexoSistemaRepository extends JapeRepository<Integer, AnexoSistema> {

    @NativeQuery("SELECT NUATTACH, NOMEARQUIVO FROM TSIANX "
            + "WHERE NOMEINSTANCIA = :nomeInstancia AND PKREGISTRO = :pkRegistro "
            + "ORDER BY DHCAD DESC")
    List<AnexoArquivoRow> findArquivos(
            @Parameter(name = "nomeInstancia") String nomeInstancia,
            @Parameter(name = "pkRegistro") String pkRegistro);

    @NativeQuery("SELECT NUATTACH, NOMEARQUIVO, CHAVEARQUIVO FROM TSIANX "
            + "WHERE NUATTACH = :nuAttach AND NOMEINSTANCIA = :nomeInstancia "
            + "AND PKREGISTRO = :pkRegistro")
    List<AnexoEscopoRow> findNoRegistro(
            @Parameter(name = "nuAttach") Integer nuAttach,
            @Parameter(name = "nomeInstancia") String nomeInstancia,
            @Parameter(name = "pkRegistro") String pkRegistro);

    @Modifying
    @NativeQuery("UPDATE TSIANX SET BH_TIPO = :tipo, NOMEARQUIVO = :nomeArquivo, "
            + "DESCRICAO = :descricao "
            + "WHERE NUATTACH = :nuAttach AND NOMEINSTANCIA = :nomeInstancia "
            + "AND PKREGISTRO = :pkRegistro")
    void atualizarNome(
            @Parameter(name = "tipo") String tipo,
            @Parameter(name = "nomeArquivo") String nomeArquivo,
            @Parameter(name = "descricao") String descricao,
            @Parameter(name = "nuAttach") Integer nuAttach,
            @Parameter(name = "nomeInstancia") String nomeInstancia,
            @Parameter(name = "pkRegistro") String pkRegistro);
}
