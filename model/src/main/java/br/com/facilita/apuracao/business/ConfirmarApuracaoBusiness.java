package br.com.facilita.apuracao.business;

import java.util.Optional;

import com.google.inject.Inject;

import br.com.facilita.apuracao.api.ApiResponse;
import br.com.facilita.apuracao.api.ApuracaoResponse;
import br.com.facilita.apuracao.api.ConfirmarApuracaoRequest;
import br.com.facilita.apuracao.api.error.ErrorCode;
import br.com.facilita.apuracao.domain.ApuracaoResponseMapper;
import br.com.facilita.apuracao.domain.ApuracaoSnapshot;
import br.com.facilita.apuracao.domain.ConfirmarApuracaoCommand;
import br.com.facilita.apuracao.error.ApuracaoBusinessException;
import br.com.facilita.apuracao.error.CorrelationIds;
import br.com.facilita.apuracao.port.ApuracaoConfirmExecutor;
import br.com.facilita.apuracao.port.ApuracaoStore;
import br.com.facilita.apuracao.port.AuthorizationAction;
import br.com.facilita.apuracao.port.AuthorizationContext;
import br.com.facilita.apuracao.port.AuthorizationPort;
import br.com.sankhya.studio.stereotypes.Component;

/** Caso de uso puro para confirmar uma apuração. */
@Component
public final class ConfirmarApuracaoBusiness {

    private final ApuracaoStore store;
    private final ApuracaoConfirmExecutor confirmExecutor;
    private final AuthorizationPort authorization;

    @Inject
    protected ConfirmarApuracaoBusiness(ApuracaoStore store,
            ApuracaoConfirmExecutor confirmExecutor, AuthorizationPort authorization) {
        if (store == null || confirmExecutor == null || authorization == null) {
            throw new IllegalArgumentException("As portas de apuração são obrigatórias.");
        }
        this.store = store;
        this.confirmExecutor = confirmExecutor;
        this.authorization = authorization;
    }

    public ApiResponse<ApuracaoResponse> execute(ConfirmarApuracaoRequest request,
            AuthorizationContext context, String correlationId) {
        String safeCorrelationId = CorrelationIds.normalize(correlationId);
        if (request == null || request.getNuApuracao() == null
                || request.getNuApuracao().intValue() <= 0) {
            throw failure(ErrorCode.VALIDATION, "A apuração informada é inválida.",
                    "nuApuracao", safeCorrelationId);
        }
        if (isBlank(request.getVersion())) {
            throw failure(ErrorCode.VALIDATION, "A versão observada é obrigatória.",
                    "version", safeCorrelationId);
        }
        if (isBlank(request.getIdempotencyKey())) {
            throw failure(ErrorCode.VALIDATION, "A chave de idempotência é obrigatória.",
                    "idempotencyKey", safeCorrelationId);
        }
        requireUser(context, safeCorrelationId);

        ApuracaoSnapshot current = findRequired(request.getNuApuracao(), safeCorrelationId);
        requireAuthorization(context, current, safeCorrelationId);
        try {
            confirmExecutor.confirm(new ConfirmarApuracaoCommand(request.getNuApuracao(),
                    request.getVersion().trim(), request.getIdempotencyKey().trim()));
        } catch (ApuracaoBusinessException exception) {
            throw withCorrelation(exception, safeCorrelationId);
        }
        ApuracaoSnapshot confirmed = findAfterCommit(request.getNuApuracao(), safeCorrelationId);
        return ApiResponse.success(safeCorrelationId, ApuracaoResponseMapper.toResponse(confirmed));
    }

    private ApuracaoSnapshot findRequired(Integer id, String correlationId) {
        Optional<ApuracaoSnapshot> found = store.findById(id);
        if (found == null || !found.isPresent() || found.get() == null) {
            throw failure(ErrorCode.VALIDATION, "A apuração informada não foi encontrada.",
                    "nuApuracao", correlationId);
        }
        return found.get();
    }

    private ApuracaoSnapshot findAfterCommit(Integer id, String correlationId) {
        Optional<ApuracaoSnapshot> found = store.findById(id);
        if (found == null || !found.isPresent() || found.get() == null) {
            throw failure(ErrorCode.INTEGRATION,
                    "A confirmação foi concluída, mas não foi possível reler a apuração.",
                    null, correlationId);
        }
        return found.get();
    }

    private void requireAuthorization(AuthorizationContext context, ApuracaoSnapshot snapshot,
            String correlationId) {
        try {
            authorization.requireAllowed(AuthorizationAction.CONFIRM, context, snapshot);
        } catch (ApuracaoBusinessException exception) {
            if (correlationId.equals(exception.getCorrelationId())) {
                throw exception;
            }
            throw failure(exception.getCode(), exception.getMessage(), exception.getField(),
                    correlationId);
        }
    }

    private static void requireUser(AuthorizationContext context, String correlationId) {
        if (context == null || isBlank(context.getUserId())) {
            throw failure(ErrorCode.FORBIDDEN, "Usuário não autorizado para esta operação.",
                    null, correlationId);
        }
    }

    private static ApuracaoBusinessException withCorrelation(ApuracaoBusinessException exception,
            String correlationId) {
        if (correlationId.equals(exception.getCorrelationId())) {
            return exception;
        }
        return failure(exception.getCode(), exception.getMessage(), exception.getField(),
                correlationId);
    }

    private static boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    private static ApuracaoBusinessException failure(ErrorCode code, String message, String field,
            String correlationId) {
        return new ApuracaoBusinessException(code, message, field, correlationId);
    }
}
