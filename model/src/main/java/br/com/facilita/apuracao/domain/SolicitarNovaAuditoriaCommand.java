package br.com.facilita.apuracao.domain;

/**
 * Único comando que autoriza o adapter a executar o reset de nova auditoria.
 * Os campos a limpar não são recebidos pelo cliente nem ficam expostos como
 * opções mutáveis neste tipo.
 */
public final class SolicitarNovaAuditoriaCommand extends ApuracaoCommand {

    private final String motivo;

    public SolicitarNovaAuditoriaCommand(Integer nuApuracao, String expectedVersion,
            String idempotencyKey, String motivo) {
        super(nuApuracao, expectedVersion, idempotencyKey);
        this.motivo = motivo;
    }

    public String getMotivo() { return motivo; }
}
