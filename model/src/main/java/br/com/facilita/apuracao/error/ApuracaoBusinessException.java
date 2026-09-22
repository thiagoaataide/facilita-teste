package br.com.facilita.apuracao.error;

import br.com.facilita.apuracao.api.error.ErrorCode;

/**
 * Exceção segura para falhas de negócio que podem ser convertidas no
 * envelope público da fachada.
 *
 * <p>A exceção carrega somente o código estável, o campo opcional e o
 * identificador de correlação. Detalhes de infraestrutura devem ser
 * registrados internamente pelo chamador e não propagados como mensagem
 * pública.</p>
 */
public final class ApuracaoBusinessException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    private final ErrorCode code;
    private final String field;
    private final String correlationId;

    public ApuracaoBusinessException(ErrorCode code, String message, String field, String correlationId) {
        super(requireMessage(message));
        if (code == null) {
            throw new IllegalArgumentException("O código do erro é obrigatório.");
        }
        this.code = code;
        this.field = normalizeField(field);
        this.correlationId = CorrelationIds.normalize(correlationId);
    }

    public ApuracaoBusinessException(ErrorCode code, String message, String field) {
        this(code, message, field, null);
    }

    public ApuracaoBusinessException(ErrorCode code, String message) {
        this(code, message, null, null);
    }

    public ErrorCode getCode() {
        return code;
    }

    public String getField() {
        return field;
    }

    public String getCorrelationId() {
        return correlationId;
    }

    private static String requireMessage(String message) {
        if (message == null || message.trim().isEmpty()) {
            throw new IllegalArgumentException("A mensagem segura do erro é obrigatória.");
        }
        return message;
    }

    private static String normalizeField(String field) {
        if (field == null) {
            return null;
        }

        String normalized = field.trim();
        if (normalized.isEmpty()) {
            return null;
        }
        if (normalized.length() > 100 || !normalized.matches("[A-Za-z0-9_.-]+")) {
            throw new IllegalArgumentException("O campo do erro possui formato inválido.");
        }
        return normalized;
    }
}
