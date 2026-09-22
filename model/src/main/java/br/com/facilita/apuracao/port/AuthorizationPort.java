package br.com.facilita.apuracao.port;

import br.com.facilita.apuracao.domain.ApuracaoSnapshot;

/**
 * Porta para a autorização do usuário corrente. A implementação deve usar a
 * sessão/permissões do Om e lançar {@code ApuracaoBusinessException} segura em
 * caso de negativa.
 */
public interface AuthorizationPort {

    default void requireAllowed(AuthorizationAction action, AuthorizationContext context) {
        requireAllowed(action, context, null);
    }

    void requireAllowed(AuthorizationAction action, AuthorizationContext context,
            ApuracaoSnapshot apuracao);
}
