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

    @NativeQuery("SELECT APU.NUAPURACAO, TO_CHAR(APU.CODCONTA) AS CODCONTA, "
            + "TO_CHAR(APU.NUMCONTRATO) AS NUMCONTRATO, TO_CHAR(APU.NUNOTA) AS NUNOTA, "
            + "TO_CHAR(APU.SEQUENCIACON) AS SEQUENCIACON, TO_CHAR(APU.OPERADORA) AS OPERADORA, "
            + "TO_CHAR(APU.CLIENTE) AS CLIENTE, TO_CHAR(APU.CODVEND) AS CODVEND, "
            + "TO_CHAR(APU.REFERENCIA, 'YYYY-MM-DD') AS REFERENCIA, "
            + "TO_CHAR(APU.REFERENCIAADIADA, 'YYYY-MM-DD') AS REFERENCIAADIADA, "
            + "TO_CHAR(APU.DTVENC, 'YYYY-MM-DD') AS DTVENC, APU.VALOR, APU.VALORREF, "
            + "NVL(APU.CONFIRMADO, 'N') AS CONFIRMADO, "
            + "NVL(APU.AUDITORIAFINALIZADA, 'N') AS AUDITORIAFINALIZADA, "
            + "NVL(APU.EMAILENVIADO, 'N') AS EMAILENVIADO, "
            + "NVL(APU.FATURAMENTOLIBERADO, 'N') AS FATURAMENTOLIBERADO, "
            + "TO_CHAR(APU.NUFILA) AS NUFILA, TO_CHAR(APU.PLANO) AS PLANO, "
            + "TO_CHAR(APU.IDINSTPRN) AS IDINSTPRN, "
            + "CASE WHEN EXISTS (SELECT 1 FROM TSIANX ANX "
            + "WHERE ANX.NOMEINSTANCIA = 'bhApuracao' "
            + "AND ANX.PKREGISTRO = TO_CHAR(APU.NUAPURACAO) || '_bhApuracao') "
            + "THEN 'S' ELSE 'N' END AS POSSUIANEXO "
            + "FROM BH_FACAPU APU WHERE APU.NUAPURACAO = :nuApuracao")
    List<DetalheApuracaoRow> findDetalhe(@Parameter(name = "nuApuracao") Integer nuApuracao);
}
