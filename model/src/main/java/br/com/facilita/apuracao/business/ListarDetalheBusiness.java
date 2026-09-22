package br.com.facilita.apuracao.business;

import java.util.Optional;

import com.google.inject.Inject;

import br.com.facilita.apuracao.api.ApiResponse;
import br.com.facilita.apuracao.api.ApuracaoResponse;
import br.com.facilita.apuracao.api.ListarDetalheRequest;
import br.com.facilita.apuracao.api.error.ErrorCode;
import br.com.facilita.apuracao.domain.ApuracaoResponseMapper;
import br.com.facilita.apuracao.domain.ApuracaoSnapshot;
import br.com.facilita.apuracao.error.ApuracaoBusinessException;
import br.com.facilita.apuracao.error.CorrelationIds;
import br.com.facilita.apuracao.port.ApuracaoQuery;
import br.com.facilita.apuracao.port.AuthorizationAction;
import br.com.facilita.apuracao.port.AuthorizationContext;
import br.com.facilita.apuracao.port.AuthorizationPort;
import br.com.sankhya.studio.stereotypes.Component;

/** Caso de uso de detalhe, com escopo de autorização antes da resposta. */
@Component
public final class ListarDetalheBusiness {

    private final ApuracaoQuery query;
    private final AuthorizationPort authorization;

    @Inject
    protected ListarDetalheBusiness(ApuracaoQuery query, AuthorizationPort authorization) {
        if (query == null || authorization == null) {
            throw new IllegalArgumentException("As portas de consulta são obrigatórias.");
        }
        this.query = query;
        this.authorization = authorization;
    }

    public ApiResponse<ApuracaoResponse> execute(ListarDetalheRequest request,
            AuthorizationContext context, String correlationId) {
        String safeCorrelationId = CorrelationIds.normalize(correlationId);
        requireUser(context, safeCorrelationId);
        if (request == null || request.getNuApuracao() == null
                || request.getNuApuracao().intValue() <= 0) {
            throw failure(ErrorCode.VALIDATION, "A apuração informada é inválida.",
                    "nuApuracao", safeCorrelationId);
        }
        Optional<ApuracaoSnapshot> found = query.findById(request.getNuApuracao(), context);
        if (found == null || !found.isPresent() || found.get() == null) {
            throw failure(ErrorCode.VALIDATION, "A apuração informada não foi encontrada.",
                    "nuApuracao", safeCorrelationId);
        }
        ApuracaoSnapshot snapshot = found.get();
        authorization.requireAllowed(AuthorizationAction.DETAIL, context, snapshot);
        return ApiResponse.success(safeCorrelationId, ApuracaoResponseMapper.toResponse(snapshot));
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
