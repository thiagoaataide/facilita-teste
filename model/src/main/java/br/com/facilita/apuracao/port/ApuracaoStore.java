package br.com.facilita.apuracao.port;

import java.util.Optional;

import br.com.facilita.apuracao.domain.ApuracaoSnapshot;
import br.com.facilita.apuracao.domain.AtualizarApuracaoCommand;
import br.com.facilita.apuracao.domain.ConfirmarApuracaoCommand;
import br.com.facilita.apuracao.domain.SolicitarNovaAuditoriaCommand;

/** Porta de persistência; implementações concretas ficam na borda do add-on. */
public interface ApuracaoStore {

    Optional<ApuracaoSnapshot> findById(Integer nuApuracao);
    ApuracaoSnapshot updateEditableFields(AtualizarApuracaoCommand command);

    /**
     * Confirma sob a versao observada, na transacao do executor. A chave
     * idempotente nao e guardada (AD-008): um reenvio depois do commit encontra
     * a linha confirmada e recebe CONFLICT, sem segundo efeito.
     */
    ApuracaoSnapshot confirm(ConfirmarApuracaoCommand command);

    /** O adapter deve executar o reset somente para este comando explícito. */
    ApuracaoSnapshot requestNewAudit(SolicitarNovaAuditoriaCommand command);
}
