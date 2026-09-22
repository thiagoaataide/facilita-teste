package br.com.facilita.apuracao.security;

import com.google.inject.Inject;

import br.com.facilita.apuracao.api.error.ErrorCode;
import br.com.facilita.apuracao.domain.ApuracaoSnapshot;
import br.com.facilita.apuracao.error.ApuracaoBusinessException;
import br.com.facilita.apuracao.port.AuthorizationAction;
import br.com.facilita.apuracao.port.AuthorizationContext;
import br.com.facilita.apuracao.port.AuthorizationPort;
import br.com.sankhya.studio.stereotypes.Component;

/** Política temporária fail-closed até a permissão do cliente ser homologada. */
@Component
public final class FailClosedAuthorizationPort implements AuthorizationPort {

    @Inject
    protected FailClosedAuthorizationPort() {
    }

    @Override
    public void requireAllowed(AuthorizationAction action, AuthorizationContext context,
            ApuracaoSnapshot apuracao) {
        throw new ApuracaoBusinessException(ErrorCode.FORBIDDEN,
                "A permissão desta operação ainda não foi homologada no Om.");
    }
}
