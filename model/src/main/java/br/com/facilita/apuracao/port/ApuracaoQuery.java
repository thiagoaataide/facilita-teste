package br.com.facilita.apuracao.port;

import java.util.Optional;

import br.com.facilita.apuracao.domain.ApuracaoFilter;
import br.com.facilita.apuracao.domain.ApuracaoPage;
import br.com.facilita.apuracao.domain.ApuracaoSnapshot;

/** Consulta autorizada de apurações, sem expor a tecnologia de persistência. */
public interface ApuracaoQuery {

    ApuracaoPage find(ApuracaoFilter filter, AuthorizationContext context);

    Optional<ApuracaoSnapshot> findById(Integer nuApuracao, AuthorizationContext context);
}
