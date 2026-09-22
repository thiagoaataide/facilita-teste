package br.com.facilita.apuracao.security;

import java.math.BigDecimal;

import com.google.inject.Inject;

import br.com.facilita.apuracao.port.AuthorizationContext;
import br.com.sankhya.modelcore.auth.AuthenticationInfo;
import br.com.sankhya.studio.stereotypes.Component;

/** Resolve somente a identidade da sessão atual; não aceita usuário no payload. */
@Component
public final class OmAuthorizationContextResolver {

    @Inject
    protected OmAuthorizationContextResolver() {
    }

    public AuthorizationContext current() {
        try {
            AuthenticationInfo authentication = AuthenticationInfo.getCurrent();
            if (authentication != null && authentication.getUserID() != null) {
                BigDecimal userId = authentication.getUserID();
                return new AuthorizationContext(userId.toPlainString());
            }
        } catch (RuntimeException ignored) {
            // A ausência de sessão será tratada como FORBIDDEN pelo adapter.
        }
        return new AuthorizationContext(null);
    }
}
