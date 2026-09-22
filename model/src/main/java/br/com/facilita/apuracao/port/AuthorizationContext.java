package br.com.facilita.apuracao.port;

/** Contexto mínimo fornecido pelo controller; não contém sessão ou credenciais. */
public final class AuthorizationContext {

    private final String userId;

    public AuthorizationContext(String userId) {
        this.userId = userId;
    }

    public String getUserId() { return userId; }
}
