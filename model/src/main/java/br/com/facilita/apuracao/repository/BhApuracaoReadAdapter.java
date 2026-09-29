package br.com.facilita.apuracao.repository;

import java.math.BigDecimal;
import java.time.DateTimeException;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.google.inject.Inject;

import br.com.facilita.apuracao.domain.ApuracaoFilter;
import br.com.facilita.apuracao.domain.ApuracaoPage;
import br.com.facilita.apuracao.domain.ApuracaoSnapshot;
import br.com.facilita.apuracao.error.ApuracaoBusinessException;
import br.com.facilita.apuracao.api.error.ErrorCode;
import br.com.facilita.apuracao.port.ApuracaoQuery;
import br.com.facilita.apuracao.port.AuthorizationContext;
import br.com.sankhya.studio.stereotypes.Component;

/** Leitura de BH_FACAPU por chave e pela grade do mes. */
@Component
public final class BhApuracaoReadAdapter implements ApuracaoQuery {

    private static final Logger LOGGER = Logger.getLogger(BhApuracaoReadAdapter.class.getName());

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
            LOGGER.log(Level.SEVERE, "Falha ao listar a grade de " + inicio, exception);
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
        return Optional.of(toSnapshot(rows.get(0)));
    }

    private static ApuracaoSnapshot toSnapshot(DetalheApuracaoRow row) {
        return ApuracaoSnapshot.builder()
                .nuApuracao(toInteger(row.getNuapuracao()))
                .codConta(row.getCodconta())
                .numContrato(row.getNumcontrato())
                .nuNota(row.getNunota())
                .sequenciaCon(row.getSequenciacon())
                .operadora(row.getOperadora())
                .cliente(row.getCliente())
                .codVend(row.getCodvend())
                .referencia(row.getReferencia())
                .referenciaAdiada(row.getReferenciaadiada())
                .dtVenc(row.getDtvenc())
                .valor(row.getValor())
                .valorRef(row.getValorref())
                .confirmado(row.getConfirmado())
                .auditoriaFinalizada(row.getAuditoriafinalizada())
                .emailEnviado(row.getEmailenviado())
                .faturamentoLiberado(row.getFaturamentoliberado())
                .nuFila(row.getNufila())
                .plano(row.getPlano())
                .idInstPrn(row.getIdinstprn())
                .possuiAnexo(row.getPossuianexo())
                .version(BhApuracaoObservedVersion.format(row.getValor(), row.getDtvenc()))
                .build();
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
