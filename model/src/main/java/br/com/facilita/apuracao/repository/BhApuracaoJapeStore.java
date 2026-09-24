package br.com.facilita.apuracao.repository;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Optional;

import com.google.inject.Inject;

import br.com.facilita.apuracao.api.error.ErrorCode;
import br.com.facilita.apuracao.domain.ApuracaoSnapshot;
import br.com.facilita.apuracao.domain.AtualizarApuracaoCommand;
import br.com.facilita.apuracao.domain.ConfirmarApuracaoCommand;
import br.com.facilita.apuracao.domain.SolicitarNovaAuditoriaCommand;
import br.com.facilita.apuracao.error.ApuracaoBusinessException;
import br.com.facilita.apuracao.model.BhApuracao;
import br.com.facilita.apuracao.port.ApuracaoStore;
import br.com.sankhya.studio.stereotypes.Component;

/**
 * Persistência JAPE de {@code BH_FACAPU}. A edição de {@code VALOR}/{@code DTVENC}
 * está habilitada; confirmação e nova auditoria permanecem fail-closed até T16.
 */
@Component
public final class BhApuracaoJapeStore implements ApuracaoStore {

    private static final DateTimeFormatter ISO_DATE = DateTimeFormatter.ISO_LOCAL_DATE;

    private final BhApuracaoRepository repository;

    @Inject
    protected BhApuracaoJapeStore(BhApuracaoRepository repository) {
        if (repository == null) {
            throw new IllegalArgumentException("O repositório da apuração é obrigatório.");
        }
        this.repository = repository;
    }

    @Override
    public Optional<ApuracaoSnapshot> findById(Integer nuApuracao) {
        if (nuApuracao == null || nuApuracao.intValue() <= 0) {
            return Optional.empty();
        }
        try {
            Optional<BhApuracao> found = repository.findByNuApuracao(nuApuracao);
            if (found == null || !found.isPresent()) {
                return Optional.empty();
            }
            return Optional.of(BhApuracaoSnapshotMapper.toSnapshot(found.get()));
        } catch (Exception exception) {
            throw new ApuracaoBusinessException(ErrorCode.INTEGRATION,
                    "Não foi possível consultar a apuração no Om.");
        }
    }

    @Override
    public ApuracaoSnapshot updateEditableFields(AtualizarApuracaoCommand command) {
        if (command == null || command.getNuApuracao() == null
                || command.getNuApuracao().intValue() <= 0) {
            throw new ApuracaoBusinessException(ErrorCode.VALIDATION,
                    "A apuração informada é inválida.", "nuApuracao", null);
        }
        BhApuracao entity = loadEntity(command.getNuApuracao());
        ApuracaoSnapshot current = BhApuracaoSnapshotMapper.toSnapshot(entity);
        if (!BhApuracaoObservedVersion.matches(command.getExpectedVersion(), current.getVersion())) {
            throw new ApuracaoBusinessException(ErrorCode.CONFLICT,
                    "A apuração foi alterada por outro usuário. Recarregue os dados.",
                    "version", null);
        }
        if (current.isConfirmed()) {
            throw new ApuracaoBusinessException(ErrorCode.CONFLICT,
                    "Não é permitido alterar uma apuração já confirmada.", "nuApuracao", null);
        }
        if (command.getValor() == null && isBlank(command.getDtVenc())) {
            throw new ApuracaoBusinessException(ErrorCode.VALIDATION,
                    "Informe ao menos o valor ou o vencimento para atualizar.", null, null);
        }
        if (command.getValor() != null) {
            entity.setValor(command.getValor());
        }
        if (!isBlank(command.getDtVenc())) {
            entity.setDtVenc(parseDate(command.getDtVenc()));
        }
        try {
            BhApuracao saved = repository.save(entity);
            return BhApuracaoSnapshotMapper.toSnapshot(saved);
        } catch (Exception exception) {
            throw new ApuracaoBusinessException(ErrorCode.INTEGRATION,
                    "Não foi possível gravar a apuração no Om.");
        }
    }

    @Override
    public ApuracaoSnapshot confirm(ConfirmarApuracaoCommand command) {
        throw writeBlocked();
    }

    @Override
    public ApuracaoSnapshot requestNewAudit(SolicitarNovaAuditoriaCommand command) {
        throw writeBlocked();
    }

    private BhApuracao loadEntity(Integer nuApuracao) {
        Optional<BhApuracao> found = repository.findByNuApuracao(nuApuracao);
        if (found == null || !found.isPresent()) {
            throw new ApuracaoBusinessException(ErrorCode.VALIDATION,
                    "A apuração informada não foi encontrada.", "nuApuracao", null);
        }
        return found.get();
    }

    private static LocalDate parseDate(String value) {
        try {
            return LocalDate.parse(value.trim(), ISO_DATE);
        } catch (DateTimeParseException exception) {
            throw new ApuracaoBusinessException(ErrorCode.VALIDATION,
                    "O vencimento informado é inválido.", "dtVenc", null);
        }
    }

    private static boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    private static ApuracaoBusinessException writeBlocked() {
        return new ApuracaoBusinessException(ErrorCode.INTEGRATION,
                "A operação de escrita aguarda homologação do contrato do Om.");
    }
}
