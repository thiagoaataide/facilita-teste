package br.com.facilita.apuracao.api;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.util.Set;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;

import org.junit.jupiter.api.Test;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import br.com.facilita.apuracao.api.error.ApiError;
import br.com.facilita.apuracao.api.error.ErrorCode;
import br.com.facilita.apuracao.error.ApuracaoBusinessException;
import br.com.facilita.apuracao.error.ApuracaoControllerAdvice;

class ApiContractValidationTest {

    private static final Validator VALIDATOR = Validation.buildDefaultValidatorFactory()
            .getValidator();
    private static final Gson JSON = new GsonBuilder().serializeNulls().create();

    @Test
    void rejectsMissingAndNonPositiveApuracaoId() {
        ApuracaoIdRequest request = new ApuracaoIdRequest();

        assertHasViolation(request, "nuApuracao");

        request.setNuApuracao(Integer.valueOf(0));
        assertHasViolation(request, "nuApuracao");
    }

    @Test
    void rejectsMissingVersionAndIdempotencyKeyOnMutationRequests() {
        VersionedApuracaoRequest request = new VersionedApuracaoRequest();
        request.setNuApuracao(Integer.valueOf(1));

        assertHasViolation(request, "version");
        assertHasViolation(request, "idempotencyKey");
    }

    @Test
    void rejectsMissingIdOnAllIdBasedRequests() {
        assertHasViolation(new GetTarefaRequest(), "nuApuracao");
        assertHasViolation(new ListarAnexosRequest(), "nuApuracao");
        assertHasViolation(new ListarDetalheRequest(), "nuApuracao");
    }

    @Test
    void rejectsMissingMutationFieldsOnConfirmationAndNewAuditRequests() {
        ConfirmarApuracaoRequest confirmation = new ConfirmarApuracaoRequest();
        SolicitarNovaAuditoriaRequest newAudit = new SolicitarNovaAuditoriaRequest();

        assertHasViolation(confirmation, "nuApuracao");
        assertHasViolation(confirmation, "version");
        assertHasViolation(confirmation, "idempotencyKey");
        assertHasViolation(newAudit, "nuApuracao");
        assertHasViolation(newAudit, "version");
        assertHasViolation(newAudit, "idempotencyKey");
    }

    @Test
    void rejectsInvalidAttachmentFields() {
        AnexarRequest request = validVersionedRequest(new AnexarRequest());

        assertHasViolation(request, "sessionKey");
        assertHasViolation(request, "nameAttach");
        assertHasViolation(request, "tipo");

        request.setSessionKey(repeat('a', 201));
        assertHasViolation(request, "sessionKey");
    }

    @Test
    void rejectsUpdateWithoutEditableFieldAndNegativeValue() {
        AtualizarApuracaoRequest request = validVersionedRequest(
                new AtualizarApuracaoRequest());

        assertHasViolation(request, "anyFieldProvided");

        request.setValor(new BigDecimal("-0.01"));
        assertHasViolation(request, "valor");
    }

    @Test
    void rejectsInvalidListPaginationAndDirection() {
        ListarApuracoesRequest request = new ListarApuracoesRequest();
        request.setPagina(Integer.valueOf(-1));
        request.setTamanhoPagina(Integer.valueOf(501));
        request.setDirecao("SIDEWAYS");

        assertHasViolation(request, "pagina");
        assertHasViolation(request, "tamanhoPagina");
        assertHasViolation(request, "direcao");
    }

    @Test
    void rejectsOversizedNewAuditReason() {
        SolicitarNovaAuditoriaRequest request = validVersionedRequest(
                new SolicitarNovaAuditoriaRequest());
        request.setMotivo(repeat('a', 501));

        assertHasViolation(request, "motivo");
    }

    @Test
    void serializesStableErrorCodesInThePublicEnvelope() {
        for (ErrorCode code : ErrorCode.values()) {
            String json = JSON.toJson(ApiResponse.failure("corr-123",
                    new ApiError(code, "Mensagem segura", "nuApuracao")));

            assertTrue(json.contains("\"ok\":false"));
            assertTrue(json.contains("\"correlationId\":\"corr-123\""));
            assertTrue(json.contains("\"code\":\"" + code.name() + "\""));
            assertTrue(json.contains("\"data\":null"));
        }
    }

    @Test
    void hidesTechnicalDetailsWhenUnexpectedExceptionIsHandled() {
        ApuracaoControllerAdvice advice = new ApuracaoControllerAdvice();
        ApiResponse<Void> response = advice.handleUnexpected(
                new IllegalStateException("SELECT password FROM session; stack trace"));
        String json = JSON.toJson(response);

        assertFalse(json.contains("SELECT password"));
        assertFalse(json.contains("IllegalStateException"));
        assertFalse(json.contains("stack trace"));
        assertTrue(json.contains("\"code\":\"INTERNAL\""));
        assertTrue(json.contains("\"message\":\"Não foi possível concluir a operação.\""));
        assertTrue(response.getCorrelationId() != null && !response.getCorrelationId().isEmpty());
    }

    @Test
    void preservesBusinessErrorFieldAndCorrelationId() {
        ApuracaoControllerAdvice advice = new ApuracaoControllerAdvice();
        ApiResponse<Void> response = advice.handleBusiness(new ApuracaoBusinessException(
                ErrorCode.CONFLICT, "A apuração foi alterada.", "version", "corr-456"));
        String json = JSON.toJson(response);

        assertTrue(json.contains("\"code\":\"CONFLICT\""));
        assertTrue(json.contains("\"field\":\"version\""));
        assertTrue(json.contains("\"correlationId\":\"corr-456\""));
    }

    @Test
    void hidesTechnicalDetailsFromBusinessErrorMessages() {
        ApuracaoControllerAdvice advice = new ApuracaoControllerAdvice();
        ApiResponse<Void> response = advice.handleBusiness(new ApuracaoBusinessException(
                ErrorCode.INTEGRATION, "SELECT secret FROM session; stack trace", null,
                "corr-789"));
        String json = JSON.toJson(response);

        assertFalse(json.contains("SELECT secret"));
        assertFalse(json.contains("session"));
        assertFalse(json.contains("stack trace"));
        assertTrue(json.contains("\"code\":\"INTEGRATION\""));
        assertTrue(json.contains("\"message\":\"Não foi possível concluir a integração.\""));
    }

    private static <T extends VersionedApuracaoRequest> T validVersionedRequest(T request) {
        request.setNuApuracao(Integer.valueOf(1));
        request.setVersion("2026-09-22T10:00:00");
        request.setIdempotencyKey("operation-1");
        return request;
    }

    private static void assertHasViolation(Object request, String property) {
        Set<ConstraintViolation<Object>> violations = VALIDATOR.validate(request);
        assertTrue(violations.stream().anyMatch(violation -> property
                .equals(violation.getPropertyPath().toString())),
                "Esperava violação no campo " + property + ": " + violations);
    }

    private static String repeat(char value, int length) {
        StringBuilder result = new StringBuilder(length);
        for (int index = 0; index < length; index++) {
            result.append(value);
        }
        return result.toString();
    }
}
