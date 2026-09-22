package br.com.facilita.apuracao.business;

import com.google.inject.Inject;

import br.com.facilita.apuracao.api.ApiResponse;
import br.com.facilita.apuracao.api.GetTarefaRequest;
import br.com.facilita.apuracao.api.GetTarefaResponse;
import br.com.facilita.apuracao.api.error.ErrorCode;
import br.com.facilita.apuracao.error.ApuracaoBusinessException;
import br.com.facilita.apuracao.error.CorrelationIds;
import br.com.facilita.apuracao.integration.WorkflowGateway;
import br.com.facilita.apuracao.port.AuthorizationAction;
import br.com.facilita.apuracao.port.AuthorizationContext;
import br.com.facilita.apuracao.port.AuthorizationPort;
import br.com.sankhya.studio.stereotypes.Component;

/** Caso de uso de consulta da tarefa pendente do workflow. */
@Component
public final class GetTarefaBusiness {

    private final WorkflowGateway gateway;
    private final AuthorizationPort authorization;

    @Inject
    protected GetTarefaBusiness(WorkflowGateway gateway, AuthorizationPort authorization) {
        if (gateway == null || authorization == null) {
            throw new IllegalArgumentException("As portas de workflow são obrigatórias.");
        }
        this.gateway = gateway;
        this.authorization = authorization;
    }

    public ApiResponse<GetTarefaResponse> execute(GetTarefaRequest request,
            AuthorizationContext context, String correlationId) {
        String safeCorrelationId = CorrelationIds.normalize(correlationId);
        if (request == null || request.getNuApuracao() == null
                || request.getNuApuracao().intValue() <= 0) {
            throw failure(ErrorCode.VALIDATION, "A apuração informada é inválida.",
                    "nuApuracao", safeCorrelationId);
        }
        if (context == null || isBlank(context.getUserId())) {
            throw failure(ErrorCode.FORBIDDEN, "Usuário não autorizado para esta operação.",
                    null, safeCorrelationId);
        }
        authorization.requireAllowed(AuthorizationAction.VIEW_TASK, context, null);
        GetTarefaResponse response = gateway.getTarefa(request, context);
        if (response == null) {
            throw failure(ErrorCode.INTEGRATION,
                    "O serviço de workflow não retornou uma resposta válida.", null,
                    safeCorrelationId);
        }
        return ApiResponse.success(safeCorrelationId, response);
    }

    private static boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    private static ApuracaoBusinessException failure(ErrorCode code, String message, String field,
            String correlationId) {
        return new ApuracaoBusinessException(code, message, field, correlationId);
    }
}
