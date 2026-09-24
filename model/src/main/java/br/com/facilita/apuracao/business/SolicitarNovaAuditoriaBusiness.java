package br.com.facilita.apuracao.business;

import java.util.Optional;

import com.google.inject.Inject;

import br.com.facilita.apuracao.api.ApiResponse;
import br.com.facilita.apuracao.api.ApuracaoResponse;
import br.com.facilita.apuracao.api.SolicitarNovaAuditoriaRequest;
import br.com.facilita.apuracao.api.error.ErrorCode;
import br.com.facilita.apuracao.domain.ApuracaoResponseMapper;
import br.com.facilita.apuracao.domain.ApuracaoSnapshot;
import br.com.facilita.apuracao.domain.SolicitarNovaAuditoriaCommand;
import br.com.facilita.apuracao.error.ApuracaoBusinessException;
import br.com.facilita.apuracao.error.CorrelationIds;
import br.com.facilita.apuracao.port.ApuracaoNewAuditExecutor;
import br.com.facilita.apuracao.port.ApuracaoStore;
import br.com.facilita.apuracao.port.AuthorizationAction;
import br.com.facilita.apuracao.port.AuthorizationContext;
import br.com.facilita.apuracao.port.AuthorizationPort;
import br.com.sankhya.studio.stereotypes.Component;

/** Caso de uso puro para reiniciar a auditoria de uma apuração confirmada. */
@Component
public final class SolicitarNovaAuditoriaBusiness {

    private final ApuracaoStore store;
    private final ApuracaoNewAuditExecutor newAuditExecutor;
    private final AuthorizationPort authorization;

    @Inject
    protected SolicitarNovaAuditoriaBusiness(ApuracaoStore store,
            ApuracaoNewAuditExecutor newAuditExecutor, AuthorizationPort authorization) {
        if (store == null || newAuditExecutor == null || authorization == null) {
            throw new IllegalArgumentException("As portas de apuração são obrigatórias.");
        }
        this.store = store;
        this.newAuditExecutor = newAuditExecutor;
        this.authorization = authorization;
    }

    public ApiResponse<ApuracaoResponse> execute(SolicitarNovaAuditoriaRequest request,
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
        if (!current.isConfirmed()) {
            throw failure(ErrorCode.CONFLICT,
                    "Somente uma apuração confirmada pode solicitar nova auditoria.",
                    "nuApuracao", safeCorrelationId);
        }
        if (!sameVersion(request.getVersion(), current.getVersion())) {
            throw failure(ErrorCode.CONFLICT,
                    "A apuração foi alterada por outro usuário. Recarregue os dados.",
                    "version", safeCorrelationId);
        }

        try {
            newAuditExecutor.requestNewAudit(new SolicitarNovaAuditoriaCommand(
                    request.getNuApuracao(), request.getVersion().trim(),
                    request.getIdempotencyKey().trim(), request.getMotivo()));
        } catch (ApuracaoBusinessException exception) {
            throw withCorrelation(exception, safeCorrelationId);
        }
        ApuracaoSnapshot reopened = findAfterCommit(request.getNuApuracao(), safeCorrelationId);
        return ApiResponse.success(safeCorrelationId, ApuracaoResponseMapper.toResponse(reopened));
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
                    "A solicitação foi concluída, mas não foi possível reler a apuração.",
                    null, correlationId);
        }
        return found.get();
    }

    private void requireAuthorization(AuthorizationContext context, ApuracaoSnapshot snapshot,
            String correlationId) {
        try {
            authorization.requireAllowed(AuthorizationAction.REQUEST_NEW_AUDIT, context, snapshot);
        } catch (ApuracaoBusinessException exception) {
            if (correlationId.equals(exception.getCorrelationId())) {
                throw exception;
            }
            throw withCorrelation(exception, correlationId);
        }
    }

    private static void requireUser(AuthorizationContext context, String correlationId) {
        if (context == null || isBlank(context.getUserId())) {
            throw failure(ErrorCode.FORBIDDEN, "Usuário não autorizado para esta operação.",
                    null, correlationId);
        }
    }

    private static boolean sameVersion(String expected, String current) {
        return !isBlank(expected) && !isBlank(current) && expected.trim().equals(current.trim());
    }

    private static boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    private static ApuracaoBusinessException withCorrelation(ApuracaoBusinessException exception,
            String correlationId) {
        if (correlationId.equals(exception.getCorrelationId())) {
            return exception;
        }
        return failure(exception.getCode(), exception.getMessage(), exception.getField(),
                correlationId);
    }

    private static ApuracaoBusinessException failure(ErrorCode code, String message, String field,
            String correlationId) {
        return new ApuracaoBusinessException(code, message, field, correlationId);
    }
}
