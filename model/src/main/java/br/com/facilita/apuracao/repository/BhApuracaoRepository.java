package br.com.facilita.apuracao.repository;

import java.util.List;
import java.util.Optional;

import br.com.facilita.apuracao.model.BhApuracao;
import br.com.sankhya.sdk.data.repository.JapeRepository;
import br.com.sankhya.studio.persistence.Criteria;
import br.com.sankhya.studio.persistence.NativeQuery;
import br.com.sankhya.studio.persistence.Parameter;
import br.com.sankhya.studio.stereotypes.Repository;

/** Repositório nativo de leitura da apuração, sem DML. */
@Repository
public interface BhApuracaoRepository extends JapeRepository<Integer, BhApuracao> {

    @Criteria(clause = "this.NUAPURACAO = :nuApuracao")
    Optional<BhApuracao> findByNuApuracao(
            @Parameter(name = "nuApuracao") Integer nuApuracao);

    @NativeQuery("SELECT APU.NUAPURACAO, APU.CODCONTA, APU.NUMCONTRATO, "
            + "TO_CHAR(APU.DTVENC, 'YYYY-MM-DD') AS DTVENC, APU.VALOR, "
            + "NVL(APU.CONFIRMADO, 'N') AS CONFIRMADO, "
            + "CASE WHEN EXISTS (SELECT 1 FROM TSIANX ANX "
            + "WHERE ANX.NOMEINSTANCIA = 'bhApuracao' "
            + "AND ANX.PKREGISTRO = TO_CHAR(APU.NUAPURACAO) || '_bhApuracao') "
            + "THEN 'S' ELSE 'N' END AS POSSUIANEXO "
            + "FROM BH_FACAPU APU "
            + "WHERE APU.REFERENCIA >= TO_DATE(:inicio, 'YYYY-MM-DD') "
            + "AND APU.REFERENCIA < TO_DATE(:fim, 'YYYY-MM-DD') "
            + "AND (:somentePendentes = 'N' OR NVL(APU.CONFIRMADO, 'N') <> 'S') "
            + "AND (:possuiAnexo = 'N' OR EXISTS (SELECT 1 FROM TSIANX ANX_FILTRO "
            + "WHERE ANX_FILTRO.NOMEINSTANCIA = 'bhApuracao' "
            + "AND ANX_FILTRO.PKREGISTRO = TO_CHAR(APU.NUAPURACAO) || '_bhApuracao')) "
            + "ORDER BY APU.DTVENC, APU.NUAPURACAO")
    List<GradeApuracaoRow> findGrade(
            @Parameter(name = "inicio") String inicio,
            @Parameter(name = "fim") String fim,
            @Parameter(name = "somentePendentes") String somentePendentes,
            @Parameter(name = "possuiAnexo") String possuiAnexo);
}
