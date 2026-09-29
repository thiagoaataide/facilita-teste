package br.com.facilita.apuracao.security;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

import br.com.facilita.apuracao.api.error.ErrorCode;
import br.com.facilita.apuracao.error.ApuracaoBusinessException;
import br.com.facilita.apuracao.port.AuthorizationAction;
import br.com.facilita.apuracao.port.AuthorizationContext;

class FailClosedAuthorizationPortTest {

    private final FailClosedAuthorizationPort port = new FailClosedAuthorizationPort();

    @Test
    void permiteListarAnexoDoUsuarioDaSessao() {
        port.requireAllowed(AuthorizationAction.LIST_ATTACHMENTS,
                new AuthorizationContext("10"), null);
    }

    @Test
    void permiteListarAGradeDoUsuarioDaSessao() {
        port.requireAllowed(AuthorizationAction.LIST,
                new AuthorizationContext("10"), null);
    }

    @Test
    void permiteDetalheDoUsuarioDaSessao() {
        port.requireAllowed(AuthorizationAction.DETAIL,
                new AuthorizationContext("10"), null);
    }

    @Test
    void mantemAsDemaisAcoesFechadas() {
        ApuracaoBusinessException exception = assertThrows(ApuracaoBusinessException.class,
                new org.junit.jupiter.api.function.Executable() {
                    @Override
                    public void execute() {
                        port.requireAllowed(AuthorizationAction.CONFIRM,
                                new AuthorizationContext("10"), null);
                    }
                });
        assertEquals(ErrorCode.FORBIDDEN, exception.getCode());
    }
}
