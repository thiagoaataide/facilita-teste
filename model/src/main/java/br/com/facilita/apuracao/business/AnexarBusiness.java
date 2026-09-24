package br.com.facilita.apuracao.business;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import com.google.inject.Inject;

import br.com.facilita.apuracao.api.AnexarRequest;
import br.com.facilita.apuracao.api.AnexoResponse;
import br.com.facilita.apuracao.api.ApiResponse;
import br.com.facilita.apuracao.api.error.ErrorCode;
import br.com.facilita.apuracao.error.ApuracaoBusinessException;
import br.com.facilita.apuracao.error.CorrelationIds;
import br.com.facilita.apuracao.integration.AnexoGateway;
import br.com.facilita.apuracao.port.AuthorizationAction;
import br.com.facilita.apuracao.port.AuthorizationContext;
import br.com.facilita.apuracao.port.AuthorizationPort;
import br.com.sankhya.studio.stereotypes.Component;

/** Caso de uso de associação de um arquivo pelo gateway homologado. */
@Component
public final class AnexarBusiness {

    private static final String SESSION_KEY_PREFIX = "ANEXO_SISTEMA_bhApuracao_";
    private static final Set<String> SUPPORTED_ATTACHMENT_TYPES = Collections
            .unmodifiableSet(new HashSet<String>(Arrays.asList(
                    "FO", "2V", "FA", "BO", "NF", "RE")));

    private final AnexoGateway gateway;
    private final AuthorizationPort authorization;

    @Inject
    protected AnexarBusiness(AnexoGateway gateway, AuthorizationPort authorization) {
        if (gateway == null || authorization == null) {
            throw new IllegalArgumentException("As portas de anexo são obrigatórias.");
        }
        this.gateway = gateway;
        this.authorization = authorization;
    }

    public ApiResponse<AnexoResponse> execute(AnexarRequest request,
            AuthorizationContext context, String correlationId) {
        String safeCorrelationId = CorrelationIds.normalize(correlationId);
        validate(request, safeCorrelationId);
        requireUser(context, safeCorrelationId);
        authorization.requireAllowed(AuthorizationAction.ATTACH, context, null);
        AnexoResponse response = gateway.anexar(request, context);
        if (response == null) {
            throw failure(ErrorCode.INTEGRATION,
                    "O serviço de anexos não retornou os metadados do arquivo.", null,
                    safeCorrelationId);
        }
        return ApiResponse.success(safeCorrelationId, response);
    }

    private static void validate(AnexarRequest request, String correlationId) {
        if (request == null || request.getNuApuracao() == null
                || request.getNuApuracao().intValue() <= 0) {
            throw failure(ErrorCode.VALIDATION, "A apuração informada é inválida.",
                    "nuApuracao", correlationId);
        }
        if (isBlank(request.getVersion())) {
            throw failure(ErrorCode.VALIDATION, "A versão observada é obrigatória.",
                    "version", correlationId);
        }
        if (isBlank(request.getIdempotencyKey())) {
            throw failure(ErrorCode.VALIDATION, "A chave de idempotência é obrigatória.",
                    "idempotencyKey", correlationId);
        }
        if (isBlank(request.getSessionKey())) {
            throw failure(ErrorCode.VALIDATION, "A chave do arquivo temporário é obrigatória.",
                    "sessionKey", correlationId);
        }
        if (isBlank(request.getNameAttach())) {
            throw failure(ErrorCode.VALIDATION, "O nome do anexo é obrigatório.",
                    "nameAttach", correlationId);
        }
        if (isBlank(request.getTipo())) {
            throw failure(ErrorCode.VALIDATION, "O tipo do anexo é obrigatório.", "tipo",
                    correlationId);
        }
        if (!SUPPORTED_ATTACHMENT_TYPES.contains(request.getTipo())) {
            throw failure(ErrorCode.VALIDATION, "O tipo do anexo não é aceito.", "tipo",
                    correlationId);
        }
        String expectedSessionKey = SESSION_KEY_PREFIX + request.getNuApuracao();
        if (!expectedSessionKey.equals(request.getSessionKey())) {
            throw failure(ErrorCode.VALIDATION,
                    "A chave do upload não corresponde à apuração informada.", "sessionKey",
                    correlationId);
        }
    }

    private static void requireUser(AuthorizationContext context, String correlationId) {
        if (context == null || isBlank(context.getUserId())) {
            throw failure(ErrorCode.FORBIDDEN, "Usuário não autorizado para esta operação.",
                    null, correlationId);
        }
    }

    private static boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    private static ApuracaoBusinessException failure(ErrorCode code, String message, String field,
            String correlationId) {
        return new ApuracaoBusinessException(code, message, field, correlationId);
    }
}
