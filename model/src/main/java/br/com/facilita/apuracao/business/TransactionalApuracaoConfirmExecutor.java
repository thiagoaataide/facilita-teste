package br.com.facilita.apuracao.business;

import com.google.inject.Inject;

import br.com.facilita.apuracao.api.error.ErrorCode;
import br.com.facilita.apuracao.domain.ApuracaoSnapshot;
import br.com.facilita.apuracao.domain.ConfirmarApuracaoCommand;
import br.com.facilita.apuracao.error.ApuracaoBusinessException;
import br.com.facilita.apuracao.port.ApuracaoConfirmExecutor;
import br.com.facilita.apuracao.port.ApuracaoStore;
import br.com.sankhya.studio.persistence.Transactional;
import br.com.sankhya.studio.stereotypes.Component;

/** Fronteira transacional da confirmação, separada da reconsulta pós-commit. */
@Component
public class TransactionalApuracaoConfirmExecutor implements ApuracaoConfirmExecutor {

    private final ApuracaoStore store;

    @Inject
    public TransactionalApuracaoConfirmExecutor(ApuracaoStore store) {
        if (store == null) {
            throw new IllegalArgumentException("O armazenamento da apuração é obrigatório.");
        }
        this.store = store;
    }

    @Override
    @Transactional(Transactional.TxType.REQUIRED)
    public void confirm(ConfirmarApuracaoCommand command) {
        ApuracaoSnapshot confirmed = store.confirm(command);
        if (confirmed == null) {
            throw new ApuracaoBusinessException(ErrorCode.INTEGRATION,
                    "A confirmação da apuração não confirmou a gravação.");
        }
    }
}
