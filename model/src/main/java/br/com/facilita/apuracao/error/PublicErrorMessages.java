package br.com.facilita.apuracao.error;

import java.util.Locale;

import br.com.facilita.apuracao.api.error.ErrorCode;

/** Mantém detalhes técnicos fora das mensagens públicas do envelope. */
public final class PublicErrorMessages {

    private PublicErrorMessages() {
    }

    public static String forBusiness(ApuracaoBusinessException exception) {
        String message = exception.getMessage();
        if (message == null || containsTechnicalDetail(message)) {
            return fallbackMessage(exception.getCode());
        }
        return message;
    }

    private static boolean containsTechnicalDetail(String message) {
        String normalized = message.toLowerCase(Locale.ROOT);
        return normalized.matches(".*\\b(select|insert|update|delete|merge|drop|alter|create)\\b.*")
                || normalized.contains("stack trace")
                || normalized.contains("exception")
                || normalized.contains("session")
                || normalized.contains("senha")
                || normalized.contains("password")
                || normalized.contains("token")
                || normalized.contains("jdbc")
                || normalized.contains("sql");
    }

    private static String fallbackMessage(ErrorCode code) {
        switch (code) {
        case VALIDATION:
            return "Os dados informados são inválidos.";
        case FORBIDDEN:
            return "Usuário não autorizado para esta operação.";
        case CONFLICT:
            return "A operação conflita com uma alteração existente.";
        case INTEGRATION:
            return "Não foi possível concluir a integração.";
        case INTERNAL:
        default:
            return "Não foi possível concluir a operação.";
        }
    }
}
