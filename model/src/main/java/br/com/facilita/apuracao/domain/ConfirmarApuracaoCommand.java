package br.com.facilita.apuracao.domain;

/** Comando de confirmação; o adapter decide a mutação nativa dentro da transação. */
public final class ConfirmarApuracaoCommand extends ApuracaoCommand {

    public ConfirmarApuracaoCommand(Integer nuApuracao, String expectedVersion, String idempotencyKey) {
        super(nuApuracao, expectedVersion, idempotencyKey);
    }
}
