package br.com.facilita.apuracao.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;

import br.com.facilita.apuracao.api.error.ErrorCode;
import br.com.facilita.apuracao.domain.ApuracaoFilter;
import br.com.facilita.apuracao.domain.ApuracaoPage;
import br.com.facilita.apuracao.domain.ApuracaoSnapshot;
import br.com.facilita.apuracao.error.ApuracaoBusinessException;
import br.com.facilita.apuracao.port.AuthorizationContext;
import br.com.sankhya.studio.persistence.NativeQuery;

class BhApuracaoReadAdapterTest {

    @Test
    void devolvePaginaVaziaQuandoOMesNaoTemApuracao() {
        BhApuracaoReadAdapter adapter = adapter(Collections.<GradeApuracaoRow>emptyList());

        ApuracaoPage page = adapter.find(filter("2026-09"), new AuthorizationContext("10"));

        assertTrue(page.getItems().isEmpty());
        assertEquals(0L, page.getTotal());
        assertFalse(page.isNextPage());
    }

    @Test
    void devolveSequenciaContaContratoVencimentoValorConfirmadoEAnexo() {
        BhApuracaoReadAdapter adapter = adapter(Collections.singletonList(linhaConhecida()));

        ApuracaoPage page = adapter.find(filter("2026-09"), new AuthorizationContext("10"));

        assertEquals(1, page.getItems().size());
        ApuracaoSnapshot item = page.getItems().get(0);
        assertEquals(Integer.valueOf(185045240), item.getNuApuracao());
        assertEquals("10", item.getCodConta());
        assertEquals("20", item.getNumContrato());
        assertEquals("2026-09-15", item.getDtVenc());
        assertEquals(new BigDecimal("12.50"), item.getValor());
        assertEquals("N", item.getConfirmado());
        assertEquals("S", item.getPossuiAnexo());
    }

    @Test
    void rejeitaMesInvalidoSemMontarSqlComOTexto() throws Exception {
        final BhApuracaoReadAdapter adapter = adapter(Collections.<GradeApuracaoRow>emptyList());
        ApuracaoBusinessException exception = assertThrows(ApuracaoBusinessException.class,
                new org.junit.jupiter.api.function.Executable() {
                    @Override
                    public void execute() {
                        adapter.find(filter("nao-e-mes"), new AuthorizationContext("10"));
                    }
                });
        assertEquals(ErrorCode.VALIDATION, exception.getCode());
        assertEquals("mesReferencia", exception.getField());

        Method method = BhApuracaoRepository.class.getMethod("findGrade", String.class,
                String.class, String.class, String.class);
        String sql = method.getAnnotation(NativeQuery.class).value();
        assertFalse(sql.toUpperCase().contains("SELECT *"));
        assertTrue(sql.contains(":inicio"));
        assertTrue(sql.contains(":fim"));
        assertTrue(sql.contains(":somentePendentes"));
        assertFalse(sql.contains("nao-e-mes"));
    }

    @Test
    void detalheDevolveALinhaDoNuApuracaoPedido() {
        BhApuracaoReadAdapter adapter = adapter(Collections.<GradeApuracaoRow>emptyList(),
                Collections.singletonList(detalheConhecido()));

        Optional<ApuracaoSnapshot> found = adapter.findById(Integer.valueOf(185044207),
                new AuthorizationContext("10"));

        assertTrue(found.isPresent());
        ApuracaoSnapshot item = found.get();
        assertEquals(Integer.valueOf(185044207), item.getNuApuracao());
        assertEquals("10", item.getCodConta());
        assertEquals("2026-09-15", item.getDtVenc());
        assertEquals(new BigDecimal("12.50"), item.getValor());
        assertEquals("N", item.getConfirmado());
        assertEquals("S", item.getPossuiAnexo());
        assertEquals("12.5|2026-09-15", item.getVersion());
    }

    @Test
    void detalheVazioQuandoANuApuracaoNaoExiste() {
        BhApuracaoReadAdapter adapter = adapter(Collections.<GradeApuracaoRow>emptyList(),
                Collections.<DetalheApuracaoRow>emptyList());

        assertFalse(adapter.findById(Integer.valueOf(185044207),
                new AuthorizationContext("10")).isPresent());
    }

    private static BhApuracaoReadAdapter adapter(final List<GradeApuracaoRow> rows) {
        return adapter(rows, Collections.<DetalheApuracaoRow>emptyList());
    }

    private static BhApuracaoReadAdapter adapter(final List<GradeApuracaoRow> rows,
            final List<DetalheApuracaoRow> detalhe) {
        BhApuracaoRepository repository = (BhApuracaoRepository) Proxy.newProxyInstance(
                BhApuracaoRepository.class.getClassLoader(),
                new Class<?>[] { BhApuracaoRepository.class },
                new InvocationHandler() {
                    @Override
                    public Object invoke(Object proxy, Method method, Object[] args) {
                        if ("findGrade".equals(method.getName())) {
                            assertEquals("2026-09-01", args[0]);
                            assertEquals("2026-10-01", args[1]);
                            assertEquals("S", args[2]);
                            assertEquals("N", args[3]);
                            return rows;
                        }
                        if ("findDetalhe".equals(method.getName())) {
                            assertEquals(Integer.valueOf(185044207), args[0]);
                            return detalhe;
                        }
                        return null;
                    }
                });
        return new BhApuracaoReadAdapter(repository);
    }

    private static ApuracaoFilter filter(String mes) {
        return new ApuracaoFilter(mes, true, null, null, null, 0, 50, null, null);
    }

    private static DetalheApuracaoRow detalheConhecido() {
        return (DetalheApuracaoRow) Proxy.newProxyInstance(
                DetalheApuracaoRow.class.getClassLoader(),
                new Class<?>[] { DetalheApuracaoRow.class },
                new InvocationHandler() {
                    @Override
                    public Object invoke(Object proxy, Method method, Object[] args) {
                        String name = method.getName();
                        if ("getNuapuracao".equals(name)) {
                            return new BigDecimal("185044207");
                        }
                        if ("getCodconta".equals(name)) {
                            return "10";
                        }
                        if ("getDtvenc".equals(name)) {
                            return "2026-09-15";
                        }
                        if ("getValor".equals(name)) {
                            return new BigDecimal("12.50");
                        }
                        if ("getConfirmado".equals(name)) {
                            return "N";
                        }
                        if ("getPossuianexo".equals(name)) {
                            return "S";
                        }
                        return null;
                    }
                });
    }

    private static GradeApuracaoRow linhaConhecida() {
        return new GradeApuracaoRow() {
            @Override
            public BigDecimal getNuapuracao() {
                return new BigDecimal("185045240");
            }

            @Override
            public BigDecimal getCodconta() {
                return new BigDecimal("10");
            }

            @Override
            public BigDecimal getNumcontrato() {
                return new BigDecimal("20");
            }

            @Override
            public String getDtvenc() {
                return "2026-09-15";
            }

            @Override
            public BigDecimal getValor() {
                return new BigDecimal("12.50");
            }

            @Override
            public String getConfirmado() {
                return "N";
            }

            @Override
            public String getPossuianexo() {
                return "S";
            }
        };
    }
}
