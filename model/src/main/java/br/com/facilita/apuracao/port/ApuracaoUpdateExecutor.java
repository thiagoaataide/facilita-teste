package br.com.facilita.apuracao.port;

import br.com.facilita.apuracao.domain.AtualizarApuracaoCommand;

/** Executa a alteração em uma transação independente da orquestração. */
public interface ApuracaoUpdateExecutor {

    void update(AtualizarApuracaoCommand command);
}
