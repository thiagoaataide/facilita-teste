package br.com.facilita.apuracao.port;

/** Ações mutáveis que podem ser submetidas à política de autorização do Om. */
public enum AuthorizationAction {
    LIST,
    DETAIL,
    UPDATE,
    CONFIRM,
    REQUEST_NEW_AUDIT,
    ATTACH,
    LIST_ATTACHMENTS,
    VIEW_TASK
}
