package br.com.facilita.apuracao.business;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.math.BigDecimal;
import java.util.Collections;

import org.junit.jupiter.api.Test;

import br.com.facilita.apuracao.api.AnexoResponse;
import br.com.facilita.apuracao.api.error.ErrorCode;
import br.com.facilita.apuracao.domain.ApuracaoSnapshot;
import br.com.facilita.apuracao.error.ApuracaoBusinessException;
import br.com.facilita.apuracao.port.AuthorizationAction;
import br.com.facilita.apuracao.port.AuthorizationContext;
import br.com.facilita.apuracao.port.AuthorizationPort;
import br.com.facilita.apuracao.repository.AnexoEscopoRow;
import br.com.facilita.apuracao.repository.AnexoSistemaRepository;

class AbrirAnexoBusinessTest {

    @Test
    void anexoDeOutraApuracaoNaoDevolveUrl() {
        AbrirAnexoBusiness business = business(null);

        ApuracaoBusinessException exception = assertThrows(ApuracaoBusinessException.class,
                new org.junit.jupiter.api.function.Executable() {
                    @Override
                    public void execute() {
                        business.abrir(Integer.valueOf(10), Integer.valueOf(99),
                                new AuthorizationContext("10"));
                    }
                });

        assertEquals(ErrorCode.VALIDATION, exception.getCode());
    }

    @Test
    void chaveVaziaViraIntegration() {
        AbrirAnexoBusiness business = business(linha("fatura.pdf", "  "));

        ApuracaoBusinessException exception = assertThrows(ApuracaoBusinessException.class,
                new org.junit.jupiter.api.function.Executable() {
                    @Override
                    public void execute() {
                        business.abrir(Integer.valueOf(10), Integer.valueOf(99),
                                new AuthorizationContext("10"));
                    }
                });

        assertEquals(ErrorCode.INTEGRATION, exception.getCode());
    }

    @Test
    void anexoDaApuracaoNaoInventaUrl() {
        AnexoResponse response = business(linha("fatura.pdf", "abc")).abrir(
                Integer.valueOf(10), Integer.valueOf(99), new AuthorizationContext("10"));

        assertEquals("99", response.getIdentifier());
        assertEquals("fatura.pdf", response.getName());
        assertNull(response.getUrl());
    }

    private static AbrirAnexoBusiness business(final AnexoEscopoRow row) {
        AnexoSistemaRepository anexos = (AnexoSistemaRepository) Proxy.newProxyInstance(
                AnexoSistemaRepository.class.getClassLoader(),
                new Class<?>[] { AnexoSistemaRepository.class },
                new InvocationHandler() {
                    @Override
                    public Object invoke(Object proxy, Method method, Object[] args) {
                        if ("findNoRegistro".equals(method.getName())) {
                            return row == null
                                    ? Collections.emptyList()
                                    : Collections.singletonList(row);
                        }
                        return null;
                    }
                });
        AuthorizationPort port = new AuthorizationPort() {
            @Override
            public void requireAllowed(AuthorizationAction action, AuthorizationContext context,
                    ApuracaoSnapshot apuracao) {
            }
        };
        return new AbrirAnexoBusiness(anexos, port);
    }

    private static AnexoEscopoRow linha(final String nome, final String chave) {
        return (AnexoEscopoRow) Proxy.newProxyInstance(AnexoEscopoRow.class.getClassLoader(),
                new Class<?>[] { AnexoEscopoRow.class }, new InvocationHandler() {
                    @Override
                    public Object invoke(Object proxy, Method method, Object[] args) {
                        if ("getNuattach".equals(method.getName())) {
                            return new BigDecimal("99");
                        }
                        if ("getNomearquivo".equals(method.getName())) {
                            return nome;
                        }
                        if ("getChavearquivo".equals(method.getName())) {
                            return chave;
                        }
                        return null;
                    }
                });
    }
}
