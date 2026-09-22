package br.com.facilita.apuracao.api.error;

public class ApiError {

    private ErrorCode code;
    private String message;
    private String field;

    public ApiError() {
    }

    public ApiError(ErrorCode code, String message) {
        this(code, message, null);
    }

    public ApiError(ErrorCode code, String message, String field) {
        this.code = code;
        this.message = message;
        this.field = field;
    }

    public ErrorCode getCode() {
        return code;
    }

    public void setCode(ErrorCode code) {
        this.code = code;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getField() {
        return field;
    }

    public void setField(String field) {
        this.field = field;
    }
}
