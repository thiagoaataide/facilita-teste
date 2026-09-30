package br.com.facilita.apuracao.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;

import br.com.facilita.apuracao.api.error.ErrorCode;
import br.com.facilita.apuracao.domain.ApuracaoSnapshot;
import br.com.facilita.apuracao.domain.ConfirmarApuracaoCommand;
import br.com.facilita.apuracao.domain.SolicitarNovaAuditoriaCommand;
import br.com.facilita.apuracao.error.ApuracaoBusinessException;
import br.com.facilita.apuracao.model.BhApuracao;

class BhApuracaoJapeStoreTest {

    private static final Integer ID = Integer.valueOf(185044207);

    @Test
    void findByIdLeAApuracaoPeloDetalheNativo() {
        FakeRepository fake = new FakeRepository();
        fake.detalhe = Collections.singletonList(detalhe("N"));
        BhApuracaoJapeStore store = new BhApuracaoJapeStore(fake.proxy());

        Optional<ApuracaoSnapshot> found = store.findById(ID);

        assertTrue(found.isPresent());
        assertEquals(ID, found.get().getNuApuracao());
        assertEquals(new BigDecimal("12.50"), found.get().getValor());
        assertEquals("2026-09-15", found.get().getDtVenc());
        assertEquals("N", found.get().getConfirmado());
        assertEquals("12.5|2026-09-15", found.get().getVersion());
        assertEquals(ID, fake.detalheId);
    }

    @Test
    void findByIdVazioQuandoNaoExiste() {
        FakeRepository fake = new FakeRepository();
        fake.detalhe = Collections.<DetalheApuracaoRow>emptyList();

        assertFalse(new BhApuracaoJapeStore(fake.proxy()).findById(ID).isPresent());
    }

    @Test
    void findByIdFalhaDeSqlViraIntegration() {
        FakeRepository fake = new FakeRepository();
        fake.falhaDetalhe = true;
        final BhApuracaoJapeStore store = new BhApuracaoJapeStore(fake.proxy());

        ApuracaoBusinessException exception = assertThrows(ApuracaoBusinessException.class,
                new org.junit.jupiter.api.function.Executable() {
                    @Override
                    public void execute() {
                        store.findById(ID);
                    }
                });
        assertEquals(ErrorCode.INTEGRATION, exception.getCode());
    }

    @Test
    void confirmaApuracaoAbertaComValorPelaEntidade() {
        FakeRepository fake = new FakeRepository();
        fake.entidade = entidade(false, false, new BigDecimal("12.50"));
        BhApuracaoJapeStore store = new BhApuracaoJapeStore(fake.proxy());

        ApuracaoSnapshot saved = store.confirm(
                new ConfirmarApuracaoCommand(ID, "12.5|2026-09-15", "k1"));

        assertEquals(1, fake.saves);
        assertEquals(Boolean.TRUE, fake.salva.getConfirmado());
        assertEquals("S", saved.getConfirmado());
        assertEquals(new BigDecimal("12.50"), fake.salva.getValor());
    }

    @Test
    void confirmarSemValorViraValidationSemGravar() {
        FakeRepository fake = new FakeRepository();
        fake.entidade = entidade(false, false, null);

        assertFailure(fake, ErrorCode.VALIDATION, new ConfirmarApuracaoCommand(ID, "|2026-09-15", "k1"));
        assertEquals(0, fake.saves);
    }

    @Test
    void confirmarJaConfirmadaViraConflictSemGravar() {
        FakeRepository fake = new FakeRepository();
        fake.entidade = entidade(true, false, new BigDecimal("12.50"));

        assertFailure(fake, ErrorCode.CONFLICT,
                new ConfirmarApuracaoCommand(ID, "12.5|2026-09-15", "k1"));
        assertEquals(0, fake.saves);
    }

    @Test
    void confirmarComAuditoriaFinalizadaViraConflictSemGravar() {
        FakeRepository fake = new FakeRepository();
        fake.entidade = entidade(false, true, new BigDecimal("12.50"));

        assertFailure(fake, ErrorCode.CONFLICT,
                new ConfirmarApuracaoCommand(ID, "12.5|2026-09-15", "k1"));
        assertEquals(0, fake.saves);
    }

    @Test
    void confirmarComVersaoVelhaViraConflictSemGravar() {
        FakeRepository fake = new FakeRepository();
        fake.entidade = entidade(false, false, new BigDecimal("12.50"));

        assertFailure(fake, ErrorCode.CONFLICT,
                new ConfirmarApuracaoCommand(ID, "99|2026-09-15", "k1"));
        assertEquals(0, fake.saves);
    }

    @Test
    void novaAuditoriaLimpaOsCincoCamposEPreservaValorEVencimento() {
        FakeRepository fake = new FakeRepository();
        fake.entidade = entidade(true, true, new BigDecimal("12.50"));
        fake.entidade.setEmailEnviado(Boolean.TRUE);
        fake.entidade.setFaturamentoLiberado(Boolean.TRUE);
        fake.entidade.setIdInstPrn(Integer.valueOf(10814303));
        BhApuracaoJapeStore store = new BhApuracaoJapeStore(fake.proxy());

        ApuracaoSnapshot saved = store.requestNewAudit(new SolicitarNovaAuditoriaCommand(
                ID, "12.5|2026-09-15", "k2", null));

        assertEquals(1, fake.saves);
        assertEquals("N", saved.getConfirmado());
        assertEquals("N", saved.getAuditoriaFinalizada());
        assertEquals("N", saved.getEmailEnviado());
        assertEquals("N", saved.getFaturamentoLiberado());
        assertNull(saved.getIdInstPrn());
        assertEquals(new BigDecimal("12.50"), saved.getValor());
        assertEquals("2026-09-15", saved.getDtVenc());
    }

    @Test
    void novaAuditoriaDeApuracaoAbertaViraConflictSemGravar() {
        FakeRepository fake = new FakeRepository();
        fake.entidade = entidade(false, false, new BigDecimal("12.50"));

        assertFailure(fake, ErrorCode.CONFLICT, new SolicitarNovaAuditoriaCommand(
                ID, "12.5|2026-09-15", "k2", null));
        assertEquals(0, fake.saves);
    }

    @Test
    void novaAuditoriaComVersaoVelhaViraConflictSemGravar() {
        FakeRepository fake = new FakeRepository();
        fake.entidade = entidade(true, false, new BigDecimal("12.50"));

        assertFailure(fake, ErrorCode.CONFLICT, new SolicitarNovaAuditoriaCommand(
                ID, "99|2026-09-15", "k2", null));
        assertEquals(0, fake.saves);
    }

    @Test
    void falhaAoCarregarAEntidadeViraIntegration() {
        FakeRepository fake = new FakeRepository();
        fake.falhaCarga = true;

        assertFailure(fake, ErrorCode.INTEGRATION,
                new ConfirmarApuracaoCommand(ID, "12.5|2026-09-15", "k1"));
        assertEquals(0, fake.saves);
    }

    private static void assertFailure(FakeRepository fake, ErrorCode code,
            final Object command) {
        final BhApuracaoJapeStore store = new BhApuracaoJapeStore(fake.proxy());
        ApuracaoBusinessException exception = assertThrows(ApuracaoBusinessException.class,
                new org.junit.jupiter.api.function.Executable() {
                    @Override
                    public void execute() {
                        if (command instanceof ConfirmarApuracaoCommand) {
                            store.confirm((ConfirmarApuracaoCommand) command);
                        } else {
                            store.requestNewAudit((SolicitarNovaAuditoriaCommand) command);
                        }
                    }
                });
        assertEquals(code, exception.getCode());
    }

    private static BhApuracao entidade(boolean confirmado, boolean finalizada, BigDecimal valor) {
        BhApuracao entity = new BhApuracao();
        entity.setNuApuracao(ID);
        entity.setValor(valor);
        entity.setDtVenc(LocalDate.of(2026, 9, 15));
        entity.setConfirmado(Boolean.valueOf(confirmado));
        entity.setAuditoriaFinalizada(Boolean.valueOf(finalizada));
        return entity;
    }

    static DetalheApuracaoRow detalhe(final String confirmado) {
        return (DetalheApuracaoRow) Proxy.newProxyInstance(
                DetalheApuracaoRow.class.getClassLoader(),
                new Class<?>[] { DetalheApuracaoRow.class },
                new InvocationHandler() {
                    @Override
                    public Object invoke(Object proxy, Method method, Object[] args) {
                        String name = method.getName();
                        if ("getNuapuracao".equals(name)) {
                            return new BigDecimal(ID.intValue());
                        }
                        if ("getValor".equals(name)) {
                            return new BigDecimal("12.50");
                        }
                        if ("getDtvenc".equals(name)) {
                            return "2026-09-15";
                        }
                        if ("getConfirmado".equals(name)) {
                            return confirmado;
                        }
                        return null;
                    }
                });
    }

    static final class FakeRepository {
        List<DetalheApuracaoRow> detalhe = Collections.emptyList();
        boolean falhaDetalhe;
        Object detalheId;
        BhApuracao entidade;
        boolean falhaCarga;
        BhApuracao salva;
        int saves;

        BhApuracaoRepository proxy() {
            return (BhApuracaoRepository) Proxy.newProxyInstance(
                    BhApuracaoRepository.class.getClassLoader(),
                    new Class<?>[] { BhApuracaoRepository.class },
                    new InvocationHandler() {
                        @Override
                        public Object invoke(Object proxy, Method method, Object[] args) {
                            if ("findDetalhe".equals(method.getName())) {
                                detalheId = args[0];
                                if (falhaDetalhe) {
                                    throw new IllegalStateException("falha simulada");
                                }
                                return detalhe;
                            }
                            if ("findByPK".equals(method.getName())) {
                                assertEquals(ID, args[0]);
                                if (falhaCarga) {
                                    throw new IllegalStateException("falha simulada");
                                }
                                return entidade;
                            }
                            if ("save".equals(method.getName())) {
                                saves++;
                                salva = (BhApuracao) args[0];
                                return salva;
                            }
                            throw new UnsupportedOperationException(method.getName());
                        }
                    });
        }
    }
}
