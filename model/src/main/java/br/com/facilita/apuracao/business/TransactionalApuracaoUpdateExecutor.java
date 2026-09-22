package br.com.facilita.apuracao.business;

import com.google.inject.Inject;

import br.com.facilita.apuracao.api.error.ErrorCode;
import br.com.facilita.apuracao.domain.ApuracaoSnapshot;
import br.com.facilita.apuracao.domain.AtualizarApuracaoCommand;
import br.com.facilita.apuracao.error.ApuracaoBusinessException;
import br.com.facilita.apuracao.port.ApuracaoStore;
import br.com.facilita.apuracao.port.ApuracaoUpdateExecutor;
import br.com.sankhya.studio.persistence.Transactional;
import br.com.sankhya.studio.stereotypes.Component;

/** Fronteira transacional da gravação, separada da reconsulta pós-commit. */
@Component
public class TransactionalApuracaoUpdateExecutor implements ApuracaoUpdateExecutor {

    private final ApuracaoStore store;

    @Inject
    public TransactionalApuracaoUpdateExecutor(ApuracaoStore store) {
        if (store == null) {
            throw new IllegalArgumentException("O armazenamento da apuração é obrigatório.");
        }
        this.store = store;
    }

    @Override
    @Transactional(Transactional.TxType.REQUIRED)
    public void update(AtualizarApuracaoCommand command) {
        ApuracaoSnapshot updated = store.updateEditableFields(command);
        if (updated == null) {
            throw new ApuracaoBusinessException(ErrorCode.INTEGRATION,
                    "A atualização da apuração não confirmou a gravação.");
        }
    }
}
