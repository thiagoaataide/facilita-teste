package br.com.facilita.apuracao.repository;

import java.math.BigDecimal;
import java.time.DateTimeException;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import com.google.inject.Inject;

import br.com.facilita.apuracao.domain.ApuracaoFilter;
import br.com.facilita.apuracao.domain.ApuracaoPage;
import br.com.facilita.apuracao.domain.ApuracaoSnapshot;
import br.com.facilita.apuracao.error.ApuracaoBusinessException;
import br.com.facilita.apuracao.api.error.ErrorCode;
import br.com.facilita.apuracao.model.BhApuracao;
import br.com.facilita.apuracao.port.ApuracaoQuery;
import br.com.facilita.apuracao.port.AuthorizationContext;
import br.com.sankhya.studio.stereotypes.Component;

/** Leitura de BH_FACAPU por chave e pela grade do mes. */
@Component
public final class BhApuracaoReadAdapter implements ApuracaoQuery {

    private final BhApuracaoRepository repository;

    @Inject
    protected BhApuracaoReadAdapter(BhApuracaoRepository repository) {
        this.repository = repository;
    }

    @Override
    public ApuracaoPage find(ApuracaoFilter filter, AuthorizationContext context) {
        if (filter == null) {
            throw new ApuracaoBusinessException(ErrorCode.VALIDATION,
                    "A solicitacao de consulta e obrigatoria.");
        }
        YearMonth month = monthOf(filter.getMesReferencia());
        String inicio = month.atDay(1).toString();
        String fim = month.plusMonths(1).atDay(1).toString();
        String somentePendentes = filter.isSomentePendentes() ? "S" : "N";
        String possuiAnexo = Boolean.TRUE.equals(filter.getPossuiAnexo()) ? "S" : "N";
        List<GradeApuracaoRow> rows;
        try {
            rows = repository.findGrade(inicio, fim, somentePendentes, possuiAnexo);
        } catch (RuntimeException exception) {
            throw new ApuracaoBusinessException(ErrorCode.INTEGRATION,
                    "Nao foi possivel consultar a apuracao no Om.");
        }
        if (rows == null) {
            rows = Collections.emptyList();
        }
        return toPage(rows, filter.getPagina(), filter.getTamanhoPagina());
    }

    @Override
    public Optional<ApuracaoSnapshot> findById(Integer nuApuracao,
            AuthorizationContext context) {
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

    private static YearMonth monthOf(String mesReferencia) {
        if (mesReferencia == null || mesReferencia.trim().isEmpty()) {
            return YearMonth.now();
        }
        String value = mesReferencia.trim();
        if (!value.matches("\\d{4}-\\d{2}")) {
            throw new ApuracaoBusinessException(ErrorCode.VALIDATION,
                    "O mes de referencia informado e invalido.", "mesReferencia");
        }
        try {
            return YearMonth.parse(value);
        } catch (DateTimeException exception) {
            throw new ApuracaoBusinessException(ErrorCode.VALIDATION,
                    "O mes de referencia informado e invalido.", "mesReferencia");
        }
    }

    private static ApuracaoPage toPage(List<GradeApuracaoRow> rows, int pagina, int tamanho) {
        int size = tamanho < 1 ? 1 : tamanho;
        int from = pagina < 0 ? 0 : pagina * size;
        int to = Math.min(from + size, rows.size());
        List<ApuracaoSnapshot> items = new ArrayList<ApuracaoSnapshot>();
        if (from < rows.size()) {
            for (int index = from; index < to; index++) {
                items.add(toSnapshot(rows.get(index)));
            }
        }
        long pending = 0L;
        long withAttachment = 0L;
        for (GradeApuracaoRow row : rows) {
            if (!"S".equalsIgnoreCase(row.getConfirmado())) {
                pending++;
            }
            if ("S".equalsIgnoreCase(row.getPossuianexo())) {
                withAttachment++;
            }
        }
        return new ApuracaoPage(items, rows.size(), pending, withAttachment, to < rows.size());
    }

    private static ApuracaoSnapshot toSnapshot(GradeApuracaoRow row) {
        return ApuracaoSnapshot.builder()
                .nuApuracao(toInteger(row.getNuapuracao()))
                .codConta(toText(row.getCodconta()))
                .numContrato(toText(row.getNumcontrato()))
                .dtVenc(row.getDtvenc())
                .valor(row.getValor())
                .confirmado(row.getConfirmado())
                .possuiAnexo(row.getPossuianexo())
                .build();
    }

    private static Integer toInteger(BigDecimal value) {
        return value == null ? null : Integer.valueOf(value.intValue());
    }

    private static String toText(BigDecimal value) {
        return value == null ? null : value.toPlainString();
    }
}
