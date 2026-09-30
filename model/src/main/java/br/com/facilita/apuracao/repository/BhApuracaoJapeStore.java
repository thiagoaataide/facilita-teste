package br.com.facilita.apuracao.repository;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

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
 * Leitura por SQL nativo; gravacao pela entidade JAPE para acionar os eventos de CRUD (AD-008).
 */
@Component
public final class BhApuracaoJapeStore implements ApuracaoStore {

    private static final Logger LOGGER = Logger.getLogger(BhApuracaoJapeStore.class.getName());
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
        List<DetalheApuracaoRow> rows;
        try {
            rows = repository.findDetalhe(nuApuracao);
        } catch (RuntimeException exception) {
            LOGGER.log(Level.SEVERE, "Falha ao consultar a apuracao " + nuApuracao, exception);
            throw new ApuracaoBusinessException(ErrorCode.INTEGRATION,
                    "Nao foi possivel consultar a apuracao no Om.");
        }
        if (rows == null || rows.isEmpty() || rows.get(0) == null) {
            return Optional.empty();
        }
        return Optional.of(BhApuracaoSnapshotMapper.toSnapshot(rows.get(0)));
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
        BhApuracao entity = loadEntity(requireId(command == null ? null : command.getNuApuracao()));
        ApuracaoSnapshot current = BhApuracaoSnapshotMapper.toSnapshot(entity);
        if (current.isConfirmed() || current.isAuditFinalized()) {
            throw new ApuracaoBusinessException(ErrorCode.CONFLICT,
                    "A apuracao ja esta confirmada ou com auditoria finalizada.", "nuApuracao", null);
        }
        requireVersion(command.getExpectedVersion(), current);
        if (entity.getValor() == null) {
            throw new ApuracaoBusinessException(ErrorCode.VALIDATION,
                    "Para confirmar a apuracao o valor deve ser preenchido.", "valor", null);
        }
        entity.setConfirmado(Boolean.TRUE);
        return save(entity);
    }

    @Override
    public ApuracaoSnapshot requestNewAudit(SolicitarNovaAuditoriaCommand command) {
        BhApuracao entity = loadEntity(requireId(command == null ? null : command.getNuApuracao()));
        ApuracaoSnapshot current = BhApuracaoSnapshotMapper.toSnapshot(entity);
        if (!current.isConfirmed()) {
            throw new ApuracaoBusinessException(ErrorCode.CONFLICT,
                    "Somente uma apuracao confirmada pode solicitar nova auditoria.",
                    "nuApuracao", null);
        }
        requireVersion(command.getExpectedVersion(), current);
        entity.setConfirmado(Boolean.FALSE);
        entity.setAuditoriaFinalizada(Boolean.FALSE);
        entity.setEmailEnviado(Boolean.FALSE);
        entity.setFaturamentoLiberado(Boolean.FALSE);
        entity.setIdInstPrn(null);
        return save(entity);
    }

    private BhApuracao loadEntity(Integer nuApuracao) {
        BhApuracao found;
        try {
            found = repository.findByPK(nuApuracao);
        } catch (Exception exception) {
            LOGGER.log(Level.SEVERE, "Falha ao carregar a entidade da apuracao " + nuApuracao,
                    exception);
            throw new ApuracaoBusinessException(ErrorCode.INTEGRATION,
                    "Nao foi possivel consultar a apuracao no Om.");
        }
        if (found == null) {
            throw new ApuracaoBusinessException(ErrorCode.VALIDATION,
                    "A apuracao informada nao foi encontrada.", "nuApuracao", null);
        }
        return found;
    }

    private ApuracaoSnapshot save(BhApuracao entity) {
        try {
            return BhApuracaoSnapshotMapper.toSnapshot(repository.save(entity));
        } catch (Exception exception) {
            LOGGER.log(Level.SEVERE, "Falha ao gravar a apuracao " + entity.getNuApuracao(),
                    exception);
            throw new ApuracaoBusinessException(ErrorCode.INTEGRATION,
                    "Nao foi possivel gravar a apuracao no Om.");
        }
    }

    private static Integer requireId(Integer nuApuracao) {
        if (nuApuracao == null || nuApuracao.intValue() <= 0) {
            throw new ApuracaoBusinessException(ErrorCode.VALIDATION,
                    "A apuracao informada e invalida.", "nuApuracao", null);
        }
        return nuApuracao;
    }

    private static void requireVersion(String expected, ApuracaoSnapshot current) {
        if (!BhApuracaoObservedVersion.matches(expected, current.getVersion())) {
            throw new ApuracaoBusinessException(ErrorCode.CONFLICT,
                    "A apuracao foi alterada por outro usuario. Recarregue os dados.",
                    "version", null);
        }
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
}
