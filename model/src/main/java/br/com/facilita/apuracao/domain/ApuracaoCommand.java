package br.com.facilita.apuracao.domain;

/** Base imutável para comandos que alteram uma apuração sob concorrência otimista. */
public abstract class ApuracaoCommand {

    private final Integer nuApuracao;
    private final String expectedVersion;
    private final String idempotencyKey;

    protected ApuracaoCommand(Integer nuApuracao, String expectedVersion, String idempotencyKey) {
        this.nuApuracao = nuApuracao;
        this.expectedVersion = expectedVersion;
        this.idempotencyKey = idempotencyKey;
    }

    public Integer getNuApuracao() { return nuApuracao; }
    public String getExpectedVersion() { return expectedVersion; }
    public String getIdempotencyKey() { return idempotencyKey; }
}
