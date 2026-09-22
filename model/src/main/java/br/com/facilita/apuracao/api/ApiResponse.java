package br.com.facilita.apuracao.api;

import br.com.facilita.apuracao.api.error.ApiError;

/** Envelope único de resposta do Service Provider. */
public class ApiResponse<T> {

    private boolean ok;
    private String correlationId;
    private T data;
    private ApiError error;

    public ApiResponse() {
    }

    private ApiResponse(boolean ok, String correlationId, T data, ApiError error) {
        this.ok = ok;
        this.correlationId = correlationId;
        this.data = data;
        this.error = error;
    }

    public static <T> ApiResponse<T> success(String correlationId, T data) {
        return new ApiResponse<T>(true, correlationId, data, null);
    }

    public static <T> ApiResponse<T> failure(String correlationId, ApiError error) {
        return new ApiResponse<T>(false, correlationId, null, error);
    }

    public boolean isOk() {
        return ok;
    }

    public void setOk(boolean ok) {
        this.ok = ok;
    }

    public String getCorrelationId() {
        return correlationId;
    }

    public void setCorrelationId(String correlationId) {
        this.correlationId = correlationId;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }

    public ApiError getError() {
        return error;
    }

    public void setError(ApiError error) {
        this.error = error;
    }
}
