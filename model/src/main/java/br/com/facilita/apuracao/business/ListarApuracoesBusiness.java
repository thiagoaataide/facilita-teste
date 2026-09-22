package br.com.facilita.apuracao.business;

import java.util.ArrayList;
import java.util.List;

import com.google.inject.Inject;

import br.com.facilita.apuracao.api.ApiResponse;
import br.com.facilita.apuracao.api.ApuracaoResponse;
import br.com.facilita.apuracao.api.ListarApuracoesRequest;
import br.com.facilita.apuracao.api.ListarApuracoesResponse;
import br.com.facilita.apuracao.api.error.ErrorCode;
import br.com.facilita.apuracao.domain.ApuracaoFilter;
import br.com.facilita.apuracao.domain.ApuracaoPage;
import br.com.facilita.apuracao.domain.ApuracaoResponseMapper;
import br.com.facilita.apuracao.error.ApuracaoBusinessException;
import br.com.facilita.apuracao.error.CorrelationIds;
import br.com.facilita.apuracao.port.ApuracaoQuery;
import br.com.facilita.apuracao.port.AuthorizationAction;
import br.com.facilita.apuracao.port.AuthorizationContext;
import br.com.facilita.apuracao.port.AuthorizationPort;
import br.com.sankhya.studio.stereotypes.Component;

/** Caso de uso de consulta paginada e autorizada da grade. */
@Component
public final class ListarApuracoesBusiness {

    private static final int DEFAULT_PAGE_SIZE = 50;
    private static final int MAX_PAGE_SIZE = 500;

    private final ApuracaoQuery query;
    private final AuthorizationPort authorization;

    @Inject
    protected ListarApuracoesBusiness(ApuracaoQuery query, AuthorizationPort authorization) {
        if (query == null || authorization == null) {
            throw new IllegalArgumentException("As portas de consulta são obrigatórias.");
        }
        this.query = query;
        this.authorization = authorization;
    }

    public ApiResponse<ListarApuracoesResponse> execute(ListarApuracoesRequest request,
            AuthorizationContext context, String correlationId) {
        String safeCorrelationId = CorrelationIds.normalize(correlationId);
        requireUser(context, safeCorrelationId);
        ApuracaoFilter filter = toFilter(request, safeCorrelationId);
        authorization.requireAllowed(AuthorizationAction.LIST, context);

        ApuracaoPage page = query.find(filter, context);
        if (page == null) {
            throw failure(ErrorCode.INTEGRATION,
                    "A consulta não retornou uma página válida.", null, safeCorrelationId);
        }
        ListarApuracoesResponse response = new ListarApuracoesResponse();
        List<ApuracaoResponse> items = new ArrayList<ApuracaoResponse>();
        for (br.com.facilita.apuracao.domain.ApuracaoSnapshot item : page.getItems()) {
            items.add(ApuracaoResponseMapper.toResponse(item));
        }
        response.setItems(items);
        response.setTotal(page.getTotal());
        response.setPendingCount(page.getPendingCount());
        response.setWithAttachmentCount(page.getWithAttachmentCount());
        response.setNextPage(page.isNextPage());
        return ApiResponse.success(safeCorrelationId, response);
    }

    private static ApuracaoFilter toFilter(ListarApuracoesRequest request, String correlationId) {
        if (request == null) {
            throw failure(ErrorCode.VALIDATION, "A solicitação de consulta é obrigatória.", null,
                    correlationId);
        }
        int page = request.getPagina() == null ? 0 : request.getPagina().intValue();
        int size = request.getTamanhoPagina() == null
                ? DEFAULT_PAGE_SIZE : request.getTamanhoPagina().intValue();
        if (page < 0 || size < 1 || size > MAX_PAGE_SIZE) {
            throw failure(ErrorCode.VALIDATION, "A paginação informada é inválida.",
                    "tamanhoPagina", correlationId);
        }
        String direction = trimUpper(request.getDirecao());
        if (direction != null && !"ASC".equals(direction) && !"DESC".equals(direction)) {
            throw failure(ErrorCode.VALIDATION, "A direção da ordenação é inválida.",
                    "direcao", correlationId);
        }
        return new ApuracaoFilter(trim(request.getMesReferencia()),
                Boolean.TRUE.equals(request.getSomentePendentes()), request.getPossuiAnexo(),
                trim(request.getBusca()), trimUpper(request.getCampo()), page, size,
                trimUpper(request.getOrdenacao()), direction);
    }

    private static void requireUser(AuthorizationContext context, String correlationId) {
        if (context == null || isBlank(context.getUserId())) {
            throw failure(ErrorCode.FORBIDDEN, "Usuário não autorizado para esta operação.",
                    null, correlationId);
        }
    }

    private static String trim(String value) {
        return isBlank(value) ? null : value.trim();
    }

    private static String trimUpper(String value) {
        String normalized = trim(value);
        return normalized == null ? null : normalized.toUpperCase(java.util.Locale.ROOT);
    }

    private static boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    private static ApuracaoBusinessException failure(ErrorCode code, String message, String field,
            String correlationId) {
        return new ApuracaoBusinessException(code, message, field, correlationId);
    }
}
