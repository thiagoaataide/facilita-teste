package br.com.facilita.apuracao.business;

import com.google.inject.Inject;

import br.com.facilita.apuracao.api.ApiResponse;
import br.com.facilita.apuracao.api.ListarAnexosRequest;
import br.com.facilita.apuracao.api.ListarAnexosResponse;
import br.com.facilita.apuracao.api.error.ErrorCode;
import br.com.facilita.apuracao.error.ApuracaoBusinessException;
import br.com.facilita.apuracao.error.CorrelationIds;
import br.com.facilita.apuracao.integration.AnexoGateway;
import br.com.facilita.apuracao.port.AuthorizationAction;
import br.com.facilita.apuracao.port.AuthorizationContext;
import br.com.facilita.apuracao.port.AuthorizationPort;
import br.com.sankhya.studio.stereotypes.Component;

/** Caso de uso de listagem de anexos autorizados. */
@Component
public final class ListarAnexosBusiness {

    private final AnexoGateway gateway;
    private final AuthorizationPort authorization;

    @Inject
    protected ListarAnexosBusiness(AnexoGateway gateway, AuthorizationPort authorization) {
        if (gateway == null || authorization == null) {
            throw new IllegalArgumentException("As portas de anexo são obrigatórias.");
        }
        this.gateway = gateway;
        this.authorization = authorization;
    }

    public ApiResponse<ListarAnexosResponse> execute(ListarAnexosRequest request,
            AuthorizationContext context, String correlationId) {
        String safeCorrelationId = CorrelationIds.normalize(correlationId);
        validate(request, safeCorrelationId);
        requireUser(context, safeCorrelationId);
        authorization.requireAllowed(AuthorizationAction.LIST_ATTACHMENTS, context, null);
        ListarAnexosResponse response = gateway.listarAnexos(request, context);
        if (response == null) {
            throw failure(ErrorCode.INTEGRATION,
                    "O serviço de anexos não retornou uma lista válida.", null,
                    safeCorrelationId);
        }
        return ApiResponse.success(safeCorrelationId, response);
    }

    private static void validate(ListarAnexosRequest request, String correlationId) {
        if (request == null || request.getNuApuracao() == null
                || request.getNuApuracao().intValue() <= 0) {
            throw failure(ErrorCode.VALIDATION, "A apuração informada é inválida.",
                    "nuApuracao", correlationId);
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
