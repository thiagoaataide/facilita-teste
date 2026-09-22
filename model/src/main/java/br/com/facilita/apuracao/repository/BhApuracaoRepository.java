package br.com.facilita.apuracao.repository;

import java.util.Optional;

import br.com.facilita.apuracao.model.BhApuracao;
import br.com.sankhya.sdk.data.repository.JapeRepository;
import br.com.sankhya.studio.persistence.Criteria;
import br.com.sankhya.studio.persistence.Parameter;
import br.com.sankhya.studio.stereotypes.Repository;

/** Repositório nativo de leitura da apuração, sem DML. */
@Repository
public interface BhApuracaoRepository extends JapeRepository<Integer, BhApuracao> {

    @Criteria(clause = "this.NUAPURACAO = :nuApuracao")
    Optional<BhApuracao> findByNuApuracao(
            @Parameter(name = "nuApuracao") Integer nuApuracao);
}
