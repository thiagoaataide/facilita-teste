package br.com.facilita.apuracao.business;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import br.com.facilita.apuracao.api.ApiResponse;
import br.com.facilita.apuracao.api.ApuracaoResponse;
import br.com.facilita.apuracao.api.ConfirmarApuracaoRequest;
import br.com.facilita.apuracao.api.error.ErrorCode;
import br.com.facilita.apuracao.domain.ApuracaoSnapshot;
import br.com.facilita.apuracao.domain.AtualizarApuracaoCommand;
import br.com.facilita.apuracao.domain.ConfirmarApuracaoCommand;
import br.com.facilita.apuracao.domain.SolicitarNovaAuditoriaCommand;
import br.com.facilita.apuracao.error.ApuracaoBusinessException;
import br.com.facilita.apuracao.port.ApuracaoStore;
import br.com.facilita.apuracao.port.AuthorizationAction;
import br.com.facilita.apuracao.port.AuthorizationContext;
import br.com.facilita.apuracao.port.AuthorizationPort;

class ConfirmarApuracaoBusinessTest {

    private static final Integer APURACAO_ID = Integer.valueOf(42);
    private static final String CORRELATION_ID = "12345678-1234-1234-1234-123456789abc";

    private FakeStore store;
    private FakeAuthorization authorization;
    private ConfirmarApuracaoBusiness business;

    @BeforeEach
    void setUp() {
        store = new FakeStore(snapshot("v1", new BigDecimal("100.00"), "N", "N"));
        authorization = new FakeAuthorization();
        business = new ConfirmarApuracaoBusiness(store,
                new TransactionalApuracaoConfirmExecutor(store), authorization);
    }

    @Test
    void confirmationReplayWithSameIdempotencyKeyHasOneEffectAndReturnsCommittedState() {
        ConfirmarApuracaoRequest request = request("v1", "confirm-42");

        ApiResponse<ApuracaoResponse> first = business.execute(request,
                new AuthorizationContext("user-1"), CORRELATION_ID);
        ApiResponse<ApuracaoResponse> replay = business.execute(request,
                new AuthorizationContext("user-1"), CORRELATION_ID);

        assertTrue(first.isOk());
        assertTrue(replay.isOk());
        assertEquals("S", first.getData().getConfirmado());
        assertEquals("S", replay.getData().getConfirmado());
        assertEquals("v2", replay.getData().getVersion());
        assertEquals(1, store.confirmationEffects);
        assertEquals(4, store.findCount);
        assertEquals(1, store.idempotencyReplayCount);
    }

    @Test
    void confirmationRequiresAValidValueAndDoesNotWriteWhenValueIsMissing() {
        store.snapshot = snapshot("v1", null, "N", "N");
        ApuracaoSnapshot before = store.snapshot;

        assertFailure(request("v1", "confirm-42"), ErrorCode.VALIDATION, "valor");

        assertSame(before, store.snapshot);
        assertEquals(0, store.confirmationEffects);
    }

    @Test
    void staleVersionReturnsConflictWithoutChangingTheApuracao() {
        store.snapshot = snapshot("v2", new BigDecimal("100.00"), "N", "N");
        ApuracaoSnapshot before = store.snapshot;

        assertFailure(request("v1", "confirm-42"), ErrorCode.CONFLICT, "version");

        assertSame(before, store.snapshot);
        assertEquals(0, store.confirmationEffects);
    }

    @Test
    void permissionDenialDoesNotConfirmTheApuracao() {
        authorization.failure = new ApuracaoBusinessException(ErrorCode.FORBIDDEN,
                "Usuário sem permissão.");
        ApuracaoSnapshot before = store.snapshot;

        assertFailure(request("v1", "confirm-42"), ErrorCode.FORBIDDEN, null);

        assertSame(before, store.snapshot);
        assertEquals(0, store.confirmationEffects);
    }

    @Test
    void alreadyConfirmedApuracaoRejectsANewIdempotencyKeyWithoutRepeatingEffects() {
        store.snapshot = snapshot("v2", new BigDecimal("100.00"), "S", "N");
        ApuracaoSnapshot before = store.snapshot;

        assertFailure(request("v2", "different-confirmation"), ErrorCode.CONFLICT, null);

        assertSame(before, store.snapshot);
        assertEquals(0, store.confirmationEffects);
    }

    @Test
    void finalizedAuditCannotBeConfirmedAgain() {
        store.snapshot = snapshot("v1", new BigDecimal("100.00"), "N", "S");
        ApuracaoSnapshot before = store.snapshot;

        assertFailure(request("v1", "confirm-42"), ErrorCode.CONFLICT, null);

        assertSame(before, store.snapshot);
        assertEquals(0, store.confirmationEffects);
    }

    @Test
    void missingApuracaoReturnsValidationWithoutWriting() {
        store.snapshot = null;

        assertFailure(request("v1", "confirm-42"), ErrorCode.VALIDATION, "nuApuracao");

        assertEquals(0, store.confirmationEffects);
    }

    @Test
    void conflictDetectedDuringTransactionalConfirmationKeepsRequestCorrelationId() {
        store.forceConflict = true;

        ApuracaoBusinessException exception = assertThrows(ApuracaoBusinessException.class,
                () -> business.execute(request("v1", "confirm-42"),
                        new AuthorizationContext("user-1"), CORRELATION_ID));

        assertEquals(ErrorCode.CONFLICT, exception.getCode());
        assertEquals("version", exception.getField());
        assertEquals(CORRELATION_ID, exception.getCorrelationId());
        assertEquals(0, store.confirmationEffects);
        assertEquals(1, store.findCount);
    }

    private void assertFailure(ConfirmarApuracaoRequest request, ErrorCode expectedCode,
            String expectedField) {
        ApuracaoBusinessException exception = assertThrows(ApuracaoBusinessException.class,
                () -> business.execute(request, new AuthorizationContext("user-1"),
                        CORRELATION_ID));

        assertEquals(expectedCode, exception.getCode());
        assertEquals(expectedField, exception.getField());
        assertEquals(CORRELATION_ID, exception.getCorrelationId());
    }

    private static ConfirmarApuracaoRequest request(String version, String idempotencyKey) {
        ConfirmarApuracaoRequest request = new ConfirmarApuracaoRequest();
        request.setNuApuracao(APURACAO_ID);
        request.setVersion(version);
        request.setIdempotencyKey(idempotencyKey);
        return request;
    }

    private static ApuracaoSnapshot snapshot(String version, BigDecimal value, String confirmed,
            String auditFinalized) {
        return ApuracaoSnapshot.builder().nuApuracao(APURACAO_ID).version(version)
                .valor(value).confirmado(confirmed).auditoriaFinalizada(auditFinalized).build();
    }

    private static final class FakeAuthorization implements AuthorizationPort {

        private ApuracaoBusinessException failure;

        @Override
        public void requireAllowed(AuthorizationAction action, AuthorizationContext context,
                ApuracaoSnapshot apuracao) {
            assertEquals(AuthorizationAction.CONFIRM, action);
            assertNotNull(context);
            assertNotNull(apuracao);
            if (failure != null) {
                throw failure;
            }
        }
    }

    private static final class FakeStore implements ApuracaoStore {

        private ApuracaoSnapshot snapshot;
        private final Map<String, ApuracaoSnapshot> confirmationsByKey = new HashMap<String, ApuracaoSnapshot>();
        private int findCount;
        private int confirmationEffects;
        private int idempotencyReplayCount;
        private boolean forceConflict;

        private FakeStore(ApuracaoSnapshot snapshot) {
            this.snapshot = snapshot;
        }

        @Override
        public Optional<ApuracaoSnapshot> findById(Integer nuApuracao) {
            findCount++;
            return Optional.ofNullable(snapshot);
        }

        @Override
        public ApuracaoSnapshot updateEditableFields(AtualizarApuracaoCommand command) {
            throw new UnsupportedOperationException();
        }

        @Override
        public ApuracaoSnapshot confirm(ConfirmarApuracaoCommand command) {
            ApuracaoSnapshot replay = confirmationsByKey.get(command.getIdempotencyKey());
            if (replay != null) {
                idempotencyReplayCount++;
                return replay;
            }
            if (forceConflict || !command.getExpectedVersion().equals(snapshot.getVersion())) {
                throw new ApuracaoBusinessException(ErrorCode.CONFLICT,
                        "A apuração foi alterada por outro usuário.", "version");
            }
            if (!snapshot.hasValidValue()) {
                throw new ApuracaoBusinessException(ErrorCode.VALIDATION,
                        "Para confirmar uma apuração, o valor deve ser preenchido.", "valor");
            }
            if (snapshot.isConfirmed() || snapshot.isAuditFinalized()) {
                throw new ApuracaoBusinessException(ErrorCode.CONFLICT,
                        "A apuração não está elegível para confirmação.");
            }

            ApuracaoSnapshot beforeWrite = snapshot;
            snapshot = snapshot("v2", snapshot.getValor(), "S", "N");
            confirmationEffects++;
            confirmationsByKey.put(command.getIdempotencyKey(), snapshot);
            return beforeWrite;
        }

        @Override
        public ApuracaoSnapshot requestNewAudit(SolicitarNovaAuditoriaCommand command) {
            throw new UnsupportedOperationException();
        }
    }
}
