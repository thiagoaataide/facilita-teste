package br.com.facilita.apuracao.error;

import java.util.logging.Level;
import java.util.logging.Logger;

import javax.validation.ConstraintViolationException;

import br.com.facilita.apuracao.api.ApiResponse;
import br.com.facilita.apuracao.api.error.ApiError;
import br.com.facilita.apuracao.api.error.ErrorCode;
import br.com.sankhya.studio.web.ControllerAdvice;
import br.com.sankhya.studio.web.ExceptionHandler;

/** Converte falhas da fachada em um envelope seguro e correlacionável. */
@ControllerAdvice
public class ApuracaoControllerAdvice {

    private static final Logger LOGGER = Logger.getLogger(ApuracaoControllerAdvice.class.getName());

    @ExceptionHandler({ApuracaoBusinessException.class})
    public ApiResponse<Void> handleBusiness(ApuracaoBusinessException exception) {
        String correlationId = CorrelationIds.normalize(exception.getCorrelationId());
        ApiError error = new ApiError(exception.getCode(), exception.getMessage(),
                exception.getField());
        return ApiResponse.failure(correlationId, error);
    }

    @ExceptionHandler({ConstraintViolationException.class})
    public ApiResponse<Void> handleValidation(ConstraintViolationException exception) {
        String correlationId = CorrelationIds.newId();
        LOGGER.log(Level.WARNING, "Validação rejeitada na Apuração. correlationId="
                + correlationId, exception);
        ApiError error = new ApiError(ErrorCode.VALIDATION,
                "Os dados informados são inválidos.");
        return ApiResponse.failure(correlationId, error);
    }

    /**
     * Mantém detalhes técnicos fora do payload para o fallback exigido por
     * SP-09; o stack trace fica somente no log correlacionado.
     */
    @ExceptionHandler({Exception.class})
    public ApiResponse<Void> handleUnexpected(Exception exception) {
        String correlationId = CorrelationIds.newId();
        LOGGER.log(Level.SEVERE, "Falha interna na Apuração. correlationId="
                + correlationId, exception);
        ApiError error = new ApiError(ErrorCode.INTERNAL,
                "Não foi possível concluir a operação.");
        return ApiResponse.failure(correlationId, error);
    }
}
