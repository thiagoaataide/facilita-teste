package br.com.facilita.apuracao.port;

import br.com.facilita.apuracao.domain.ConfirmarApuracaoCommand;

/** Executa a confirmação idempotente numa transação independente. */
public interface ApuracaoConfirmExecutor {

    void confirm(ConfirmarApuracaoCommand command);
}
