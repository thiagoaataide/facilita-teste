package br.com.facilita.apuracao.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.time.LocalDate;
import java.util.Collections;

import org.junit.jupiter.api.Test;

import br.com.facilita.apuracao.repository.NomeAnexoRow;

class NomeAnexoDadosTest {

    @Test
    void mapeiaContaParceiroDocumentoEVencimento() {
        NomeAnexoDados dados = NomeAnexoDados.primeira(Collections.singletonList(linha(
                "LINK 20 MB", "Operadora", "07196307000159", "2026-09-10")));

        assertEquals("LINK 20 MB", dados.getIdentificador());
        assertEquals("Operadora", dados.getNomeParceiro());
        assertEquals("07196307000159", dados.getDocumento());
        assertEquals(LocalDate.of(2026, 9, 10), dados.getVencimento());
    }

    @Test
    void linhaAusenteNaoViraDado() {
        assertNull(NomeAnexoDados.primeira(Collections.<NomeAnexoRow>emptyList()));
    }

    private static NomeAnexoRow linha(final String identificador, final String nome,
            final String documento, final String vencimento) {
        return (NomeAnexoRow) Proxy.newProxyInstance(NomeAnexoRow.class.getClassLoader(),
                new Class<?>[] { NomeAnexoRow.class }, new InvocationHandler() {
                    @Override
                    public Object invoke(Object proxy, Method method, Object[] args) {
                        String name = method.getName();
                        if ("getIdentificador".equals(name)) {
                            return identificador;
                        }
                        if ("getNomeparc".equals(name)) {
                            return nome;
                        }
                        if ("getCgccpf".equals(name)) {
                            return documento;
                        }
                        if ("getDtvenc".equals(name)) {
                            return vencimento;
                        }
                        return null;
                    }
                });
    }
}
