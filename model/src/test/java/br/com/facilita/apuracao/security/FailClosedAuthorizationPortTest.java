package br.com.facilita.apuracao.security;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.math.BigDecimal;

import org.junit.jupiter.api.Test;

import br.com.facilita.apuracao.api.error.ErrorCode;
import br.com.facilita.apuracao.error.ApuracaoBusinessException;
import br.com.facilita.apuracao.port.AuthorizationAction;
import br.com.facilita.apuracao.port.AuthorizationContext;
import br.com.facilita.apuracao.repository.UsuarioRepository;

class FailClosedAuthorizationPortTest {

    private final FailClosedAuthorizationPort port = new FailClosedAuthorizationPort(usuarios("N"));

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
    void permiteConfirmarParaOUsuarioDaSessao() {
        port.requireAllowed(AuthorizationAction.CONFIRM,
                new AuthorizationContext("10"), null);
    }

    @Test
    void permiteNovaAuditoriaQuandoBhNovaAuditEhS() {
        new FailClosedAuthorizationPort(usuarios("S")).requireAllowed(
                AuthorizationAction.REQUEST_NEW_AUDIT, new AuthorizationContext("0"), null);
    }

    @Test
    void recusaNovaAuditoriaQuandoBhNovaAuditEhN() {
        assertForbidden(new FailClosedAuthorizationPort(usuarios("N")),
                AuthorizationAction.REQUEST_NEW_AUDIT);
    }

    @Test
    void recusaNovaAuditoriaQuandoOUsuarioNaoTemLinha() {
        assertForbidden(new FailClosedAuthorizationPort(usuarios(null)),
                AuthorizationAction.REQUEST_NEW_AUDIT);
    }

    @Test
    void recusaNovaAuditoriaQuandoALeituraDaFlagFalha() {
        assertForbidden(new FailClosedAuthorizationPort(usuariosQueFalham()),
                AuthorizationAction.REQUEST_NEW_AUDIT);
    }

    @Test
    void permiteAnexarParaOUsuarioDaSessao() {
        port.requireAllowed(AuthorizationAction.ATTACH, new AuthorizationContext("10"), null);
    }

    @Test
    void mantemAsDemaisAcoesFechadas() {
        assertForbidden(port, AuthorizationAction.UPDATE);
        assertForbidden(port, AuthorizationAction.VIEW_TASK);
    }

    private static void assertForbidden(final FailClosedAuthorizationPort alvo,
            final AuthorizationAction action) {
        ApuracaoBusinessException exception = assertThrows(ApuracaoBusinessException.class,
                new org.junit.jupiter.api.function.Executable() {
                    @Override
                    public void execute() {
                        alvo.requireAllowed(action, new AuthorizationContext("10"), null);
                    }
                });
        assertEquals(ErrorCode.FORBIDDEN, exception.getCode());
    }

    private static UsuarioRepository usuarios(final String flag) {
        return (UsuarioRepository) Proxy.newProxyInstance(
                UsuarioRepository.class.getClassLoader(),
                new Class<?>[] { UsuarioRepository.class },
                new InvocationHandler() {
                    @Override
                    public Object invoke(Object proxy, Method method, Object[] args) {
                        if ("findNovaAuditoria".equals(method.getName())) {
                            assertEquals(BigDecimal.class, args[0].getClass());
                            return flag;
                        }
                        throw new UnsupportedOperationException(method.getName());
                    }
                });
    }

    private static UsuarioRepository usuariosQueFalham() {
        return (UsuarioRepository) Proxy.newProxyInstance(
                UsuarioRepository.class.getClassLoader(),
                new Class<?>[] { UsuarioRepository.class },
                new InvocationHandler() {
                    @Override
                    public Object invoke(Object proxy, Method method, Object[] args) {
                        throw new IllegalStateException("falha simulada");
                    }
                });
    }
}
