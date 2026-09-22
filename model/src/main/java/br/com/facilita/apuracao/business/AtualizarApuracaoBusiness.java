package br.com.facilita.apuracao.business;

import java.math.BigDecimal;
import java.util.Optional;

import com.google.inject.Inject;

import br.com.facilita.apuracao.api.ApiResponse;
import br.com.facilita.apuracao.api.ApuracaoResponse;
import br.com.facilita.apuracao.api.AtualizarApuracaoRequest;
import br.com.facilita.apuracao.api.error.ErrorCode;
import br.com.facilita.apuracao.domain.ApuracaoResponseMapper;
import br.com.facilita.apuracao.domain.ApuracaoSnapshot;
import br.com.facilita.apuracao.domain.AtualizarApuracaoCommand;
import br.com.facilita.apuracao.error.ApuracaoBusinessException;
import br.com.facilita.apuracao.error.CorrelationIds;
import br.com.facilita.apuracao.port.ApuracaoStore;
import br.com.facilita.apuracao.port.ApuracaoUpdateExecutor;
import br.com.facilita.apuracao.port.AuthorizationAction;
import br.com.facilita.apuracao.port.AuthorizationContext;
import br.com.facilita.apuracao.port.AuthorizationPort;
import br.com.sankhya.studio.stereotypes.Component;

/** Caso de uso puro para a alteração dos campos editáveis da apuração. */
@Component
public final class AtualizarApuracaoBusiness {

    private final ApuracaoStore store;
    private final ApuracaoUpdateExecutor updateExecutor;
    private final AuthorizationPort authorization;

    @Inject
    protected AtualizarApuracaoBusiness(ApuracaoStore store,
            ApuracaoUpdateExecutor updateExecutor, AuthorizationPort authorization) {
        if (store == null) {
            throw new IllegalArgumentException("O armazenamento da apuração é obrigatório.");
        }
        if (updateExecutor == null) {
            throw new IllegalArgumentException("O executor transacional é obrigatório.");
        }
        if (authorization == null) {
            throw new IllegalArgumentException("A autorização da apuração é obrigatória.");
        }
        this.store = store;
        this.updateExecutor = updateExecutor;
        this.authorization = authorization;
    }

    public ApiResponse<ApuracaoResponse> execute(AtualizarApuracaoRequest request,
            AuthorizationContext context) {
        return execute(request, context, null);
    }

    public ApiResponse<ApuracaoResponse> execute(AtualizarApuracaoRequest request,
            AuthorizationContext context, String correlationId) {
        String safeCorrelationId = CorrelationIds.normalize(correlationId);
        validateRequest(request, safeCorrelationId);
        requireUser(context, safeCorrelationId);

        ApuracaoSnapshot current = findRequired(request.getNuApuracao(), safeCorrelationId);
        requireAuthorization(context, current, safeCorrelationId);
        requireExpectedVersion(request.getVersion(), current, safeCorrelationId);

        AtualizarApuracaoCommand command = new AtualizarApuracaoCommand(
                request.getNuApuracao(), request.getValor(), normalizeDate(request.getDtVenc()),
                request.getVersion().trim(), request.getIdempotencyKey().trim());
        try {
            updateExecutor.update(command);
        } catch (ApuracaoBusinessException exception) {
            throw withCorrelation(exception, safeCorrelationId);
        }
        ApuracaoSnapshot updated = findAfterCommit(request.getNuApuracao(), safeCorrelationId);
        return ApiResponse.success(safeCorrelationId, ApuracaoResponseMapper.toResponse(updated));
    }

    public ApiResponse<ApuracaoResponse> atualizar(AtualizarApuracaoRequest request,
            AuthorizationContext context) {
        return execute(request, context);
    }

    private ApuracaoSnapshot findRequired(Integer nuApuracao, String correlationId) {
        Optional<ApuracaoSnapshot> found = store.findById(nuApuracao);
        if (found == null || !found.isPresent() || found.get() == null) {
            throw failure(ErrorCode.VALIDATION, "A apuração informada não foi encontrada.",
                    "nuApuracao", correlationId);
        }
        return found.get();
    }

    private ApuracaoSnapshot findAfterCommit(Integer nuApuracao, String correlationId) {
        Optional<ApuracaoSnapshot> found = store.findById(nuApuracao);
        if (found == null || !found.isPresent() || found.get() == null) {
            throw failure(ErrorCode.INTEGRATION,
                    "A atualização foi confirmada, mas não foi possível reler a apuração.",
                    null, correlationId);
        }
        return found.get();
    }

    private void requireAuthorization(AuthorizationContext context, ApuracaoSnapshot snapshot,
            String correlationId) {
        try {
            authorization.requireAllowed(AuthorizationAction.UPDATE, context, snapshot);
        } catch (ApuracaoBusinessException exception) {
            throw withCorrelation(exception, correlationId);
        }
    }

    private static void validateRequest(AtualizarApuracaoRequest request, String correlationId) {
        if (request == null) {
            throw failure(ErrorCode.VALIDATION, "A solicitação de atualização é obrigatória.",
                    null, correlationId);
        }
        if (request.getNuApuracao() == null || request.getNuApuracao().intValue() <= 0) {
            throw failure(ErrorCode.VALIDATION, "A apuração informada é inválida.",
                    "nuApuracao", correlationId);
        }
        if (isBlank(request.getVersion())) {
            throw failure(ErrorCode.VALIDATION, "A versão observada é obrigatória.",
                    "version", correlationId);
        }
        if (request.getVersion().trim().length() > 100) {
            throw failure(ErrorCode.VALIDATION, "A versão observada é inválida.",
                    "version", correlationId);
        }
        if (isBlank(request.getIdempotencyKey())) {
            throw failure(ErrorCode.VALIDATION, "A chave de idempotência é obrigatória.",
                    "idempotencyKey", correlationId);
        }
        if (request.getIdempotencyKey().trim().length() > 200) {
            throw failure(ErrorCode.VALIDATION, "A chave de idempotência é inválida.",
                    "idempotencyKey", correlationId);
        }
        BigDecimal value = request.getValor();
        if (value != null && value.signum() < 0) {
            throw failure(ErrorCode.VALIDATION, "O valor deve ser maior ou igual a zero.",
                    "valor", correlationId);
        }
        String date = request.getDtVenc();
        if (date != null && (date.trim().isEmpty() || date.trim().length() > 10)) {
            throw failure(ErrorCode.VALIDATION, "O vencimento informado é inválido.",
                    "dtVenc", correlationId);
        }
        if (value == null && isBlank(date)) {
            throw failure(ErrorCode.VALIDATION,
                    "Informe ao menos o valor ou o vencimento para atualizar.", null,
                    correlationId);
        }
    }

    private static void requireUser(AuthorizationContext context, String correlationId) {
        if (context == null || isBlank(context.getUserId())) {
            throw failure(ErrorCode.FORBIDDEN, "Usuário não autorizado para esta operação.",
                    null, correlationId);
        }
    }

    private static void requireExpectedVersion(String expectedVersion, ApuracaoSnapshot current,
            String correlationId) {
        String currentVersion = current.getVersion();
        if (isBlank(currentVersion)
                || !currentVersion.trim().equals(expectedVersion.trim())) {
            throw failure(ErrorCode.CONFLICT,
                    "A apuração foi alterada por outro usuário. Recarregue os dados.",
                    "version", correlationId);
        }
    }

    private static String normalizeDate(String value) {
        return value == null ? null : value.trim();
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
