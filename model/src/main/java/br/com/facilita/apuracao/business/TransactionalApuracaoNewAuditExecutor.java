package br.com.facilita.apuracao.business;

import com.google.inject.Inject;

import br.com.facilita.apuracao.api.error.ErrorCode;
import br.com.facilita.apuracao.domain.ApuracaoSnapshot;
import br.com.facilita.apuracao.domain.SolicitarNovaAuditoriaCommand;
import br.com.facilita.apuracao.error.ApuracaoBusinessException;
import br.com.facilita.apuracao.port.ApuracaoNewAuditExecutor;
import br.com.facilita.apuracao.port.ApuracaoStore;
import br.com.sankhya.studio.persistence.Transactional;
import br.com.sankhya.studio.stereotypes.Component;

/** Fronteira transacional do reset, separada da releitura pós-commit. */
@Component
public class TransactionalApuracaoNewAuditExecutor implements ApuracaoNewAuditExecutor {

    private final ApuracaoStore store;

    @Inject
    public TransactionalApuracaoNewAuditExecutor(ApuracaoStore store) {
        if (store == null) {
            throw new IllegalArgumentException("O armazenamento da apuração é obrigatório.");
        }
        this.store = store;
    }

    @Override
    @Transactional(Transactional.TxType.REQUIRED)
    public void requestNewAudit(SolicitarNovaAuditoriaCommand command) {
        ApuracaoSnapshot reopened = store.requestNewAudit(command);
        if (reopened == null) {
            throw new ApuracaoBusinessException(ErrorCode.INTEGRATION,
                    "A solicitação de nova auditoria não confirmou a gravação.");
        }
    }
}
