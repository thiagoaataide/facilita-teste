package br.com.facilita.apuracao.business;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import br.com.facilita.apuracao.api.ApiResponse;
import br.com.facilita.apuracao.api.ApuracaoResponse;
import br.com.facilita.apuracao.api.AtualizarApuracaoRequest;
import br.com.facilita.apuracao.api.error.ErrorCode;
import br.com.facilita.apuracao.domain.ApuracaoSnapshot;
import br.com.facilita.apuracao.domain.AtualizarApuracaoCommand;
import br.com.facilita.apuracao.domain.ConfirmarApuracaoCommand;
import br.com.facilita.apuracao.domain.SolicitarNovaAuditoriaCommand;
import br.com.facilita.apuracao.error.ApuracaoBusinessException;
import br.com.facilita.apuracao.port.ApuracaoStore;
import br.com.facilita.apuracao.port.ApuracaoUpdateExecutor;
import br.com.facilita.apuracao.port.AuthorizationAction;
import br.com.facilita.apuracao.port.AuthorizationContext;
import br.com.facilita.apuracao.port.AuthorizationPort;

class AtualizarApuracaoBusinessTest {

    private static final Integer APURACAO_ID = Integer.valueOf(42);
    private static final String CORRELATION_ID = "12345678-1234-1234-1234-123456789abc";

    private FakeStore store;
    private FakeAuthorization authorization;
    private AtualizarApuracaoBusiness business;

    @BeforeEach
    void setUp() {
        store = new FakeStore(snapshot("v1", new BigDecimal("10.00"), "2026-10-15"));
        authorization = new FakeAuthorization();
        business = new AtualizarApuracaoBusiness(store, command -> {
            store.updateTo(command.getValor() == null ? store.snapshot.getValor()
                    : command.getValor(), command.getDtVenc() == null
                            ? store.snapshot.getDtVenc() : command.getDtVenc(), "v2");
        }, authorization);
    }

    @Test
    void rejectsNegativeValueAsValidationError() {
        AtualizarApuracaoRequest request = request();
        request.setValor(new BigDecimal("-0.01"));

        assertFailure(request, ErrorCode.VALIDATION, "valor");
        assertEquals(0, store.findCount);
    }

    @Test
    void rejectsBlankDueDateAsValidationError() {
        AtualizarApuracaoRequest request = request();
        request.setDtVenc("   ");

        assertFailure(request, ErrorCode.VALIDATION, "dtVenc");
        assertEquals(0, store.findCount);
    }

    @Test
    void rejectsDueDateLongerThanContractLimitAsValidationError() {
        AtualizarApuracaoRequest request = request();
        request.setDtVenc("12345678901");

        assertFailure(request, ErrorCode.VALIDATION, "dtVenc");
        assertEquals(0, store.findCount);
    }

    @Test
    void rejectsRequestWithoutEditableFieldsAsValidationError() {
        assertFailure(request(), ErrorCode.VALIDATION, null);
        assertEquals(0, store.findCount);
    }

    @Test
    void rejectsMissingApuracaoAsValidationError() {
        store.snapshot = null;
        AtualizarApuracaoRequest request = request();
        request.setValor(new BigDecimal("20.00"));

        assertFailure(request, ErrorCode.VALIDATION, "nuApuracao");
        assertEquals(1, store.findCount);
    }

    @Test
    void rejectsObservedVersionThatDoesNotMatchAsConflict() {
        AtualizarApuracaoRequest request = request();
        request.setVersion("stale-version");
        request.setValor(new BigDecimal("20.00"));

        assertFailure(request, ErrorCode.CONFLICT, "version");
        assertEquals(0, store.updateCount);
        assertEquals(1, store.findCount);
    }

    @Test
    void returnsPostCommitStateAfterAuthorizingAndCheckingObservedVersion() {
        AtualizarApuracaoRequest request = request();
        request.setValor(new BigDecimal("20.50"));
        request.setDtVenc("2026-11-30");

        ApiResponse<ApuracaoResponse> response = business.execute(request,
                new AuthorizationContext("user-1"), CORRELATION_ID);

        assertTrue(response.isOk());
        assertEquals(CORRELATION_ID, response.getCorrelationId());
        assertEquals(new BigDecimal("20.50"), response.getData().getValor());
        assertEquals("2026-11-30", response.getData().getDtVenc());
        assertEquals("v2", response.getData().getVersion());
        assertEquals(2, store.findCount, "deve reler depois da gravação transacional");
        assertEquals(1, store.updateCount);
        assertEquals(AuthorizationAction.UPDATE, authorization.action);
        assertEquals("user-1", authorization.context.getUserId());
    }

    @Test
    void mapsConflictRaisedInsideTransactionalWriteAndAddsCorrelationId() {
        ApuracaoUpdateExecutor conflictingUpdate = command -> {
            throw new ApuracaoBusinessException(ErrorCode.CONFLICT,
                    "A apuração foi alterada durante a gravação.", "version");
        };
        AtualizarApuracaoBusiness conflictBusiness = new AtualizarApuracaoBusiness(store,
                conflictingUpdate, authorization);
        AtualizarApuracaoRequest request = request();
        request.setValor(new BigDecimal("20.00"));

        ApuracaoBusinessException exception = assertThrows(ApuracaoBusinessException.class,
                () -> conflictBusiness.execute(request, new AuthorizationContext("user-1"),
                        CORRELATION_ID));

        assertEquals(ErrorCode.CONFLICT, exception.getCode());
        assertEquals("version", exception.getField());
        assertEquals(CORRELATION_ID, exception.getCorrelationId());
        assertEquals(1, store.findCount, "não deve reler quando a transação falha");
    }

    @Test
    void reportsIntegrationErrorWhenCommittedRowCannotBeReadBack() {
        ApuracaoUpdateExecutor writeWithoutVisibleRow = command -> {
            store.snapshot = null;
            store.updateCount++;
        };
        AtualizarApuracaoBusiness readbackBusiness = new AtualizarApuracaoBusiness(store,
                writeWithoutVisibleRow, authorization);
        AtualizarApuracaoRequest request = request();
        request.setValor(new BigDecimal("20.00"));

        ApuracaoBusinessException exception = assertThrows(ApuracaoBusinessException.class,
                () -> readbackBusiness.execute(request, new AuthorizationContext("user-1"),
                        CORRELATION_ID));

        assertEquals(ErrorCode.INTEGRATION, exception.getCode());
        assertEquals(CORRELATION_ID, exception.getCorrelationId());
        assertEquals(2, store.findCount);
    }

    @Test
    void rejectsUnauthorizedUserBeforeWriting() {
        authorization.failure = new ApuracaoBusinessException(ErrorCode.FORBIDDEN,
                "Usuário sem permissão.");
        AtualizarApuracaoRequest request = request();
        request.setValor(new BigDecimal("20.00"));

        assertFailure(request, ErrorCode.FORBIDDEN, null);
        assertEquals(0, store.updateCount);
    }

    @Test
    void transactionalExecutorDelegatesTheUpdateCommandToTheStore() {
        AtualizarApuracaoCommand command = new AtualizarApuracaoCommand(APURACAO_ID,
                new BigDecimal("20.00"), "2026-11-30", "v1", "update-42");

        new TransactionalApuracaoUpdateExecutor(store).update(command);

        assertEquals(command, store.lastUpdateCommand);
        assertEquals(1, store.updateCount);
    }

    @Test
    void transactionalExecutorRejectsWriteWithoutConfirmedSnapshot() {
        store.returnNullOnUpdate = true;
        AtualizarApuracaoCommand command = new AtualizarApuracaoCommand(APURACAO_ID,
                new BigDecimal("20.00"), null, "v1", "update-42");

        ApuracaoBusinessException exception = assertThrows(ApuracaoBusinessException.class,
                () -> new TransactionalApuracaoUpdateExecutor(store).update(command));

        assertEquals(ErrorCode.INTEGRATION, exception.getCode());
        assertEquals(1, store.updateCount);
    }

    private void assertFailure(AtualizarApuracaoRequest request, ErrorCode expectedCode,
            String expectedField) {
        ApuracaoBusinessException exception = assertThrows(ApuracaoBusinessException.class,
                () -> business.execute(request, new AuthorizationContext("user-1"),
                        CORRELATION_ID));

        assertEquals(expectedCode, exception.getCode());
        assertEquals(expectedField, exception.getField());
        assertEquals(CORRELATION_ID, exception.getCorrelationId());
    }

    private static AtualizarApuracaoRequest request() {
        AtualizarApuracaoRequest request = new AtualizarApuracaoRequest();
        request.setNuApuracao(APURACAO_ID);
        request.setVersion("v1");
        request.setIdempotencyKey("update-42");
        return request;
    }

    private static ApuracaoSnapshot snapshot(String version, BigDecimal value, String dueDate) {
        return ApuracaoSnapshot.builder().nuApuracao(APURACAO_ID).version(version)
                .valor(value).dtVenc(dueDate).build();
    }

    private static final class FakeAuthorization implements AuthorizationPort {

        private AuthorizationAction action;
        private AuthorizationContext context;
        private ApuracaoBusinessException failure;

        @Override
        public void requireAllowed(AuthorizationAction action, AuthorizationContext context,
                ApuracaoSnapshot apuracao) {
            this.action = action;
            this.context = context;
            if (failure != null) {
                throw failure;
            }
            assertNotNull(apuracao);
        }
    }

    private static final class FakeStore implements ApuracaoStore {

        private ApuracaoSnapshot snapshot;
        private int findCount;
        private int updateCount;
        private AtualizarApuracaoCommand lastUpdateCommand;
        private boolean returnNullOnUpdate;

        private FakeStore(ApuracaoSnapshot snapshot) {
            this.snapshot = snapshot;
        }

        @Override
        public Optional<ApuracaoSnapshot> findById(Integer nuApuracao) {
            findCount++;
            return Optional.ofNullable(snapshot);
        }

        private void updateTo(BigDecimal value, String dueDate, String version) {
            updateCount++;
            snapshot = AtualizarApuracaoBusinessTest.snapshot(version, value, dueDate);
        }

        @Override
        public ApuracaoSnapshot updateEditableFields(AtualizarApuracaoCommand command) {
            lastUpdateCommand = command;
            updateCount++;
            if (returnNullOnUpdate) {
                return null;
            }
            return snapshot;
        }

        @Override
        public ApuracaoSnapshot confirm(ConfirmarApuracaoCommand command) {
            throw new UnsupportedOperationException();
        }

        @Override
        public ApuracaoSnapshot requestNewAudit(SolicitarNovaAuditoriaCommand command) {
            throw new UnsupportedOperationException();
        }
    }
}
