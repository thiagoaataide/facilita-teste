package br.com.facilita.apuracao.repository;

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

/** Adapter de leitura por chave; a listagem paginada aguarda contrato de filtro. */
@Component
public final class BhApuracaoReadAdapter implements ApuracaoQuery {

    private final BhApuracaoRepository repository;

    @Inject
    protected BhApuracaoReadAdapter(BhApuracaoRepository repository) {
        this.repository = repository;
    }

    @Override
    public ApuracaoPage find(ApuracaoFilter filter, AuthorizationContext context) {
        throw new ApuracaoBusinessException(ErrorCode.INTEGRATION,
                "A listagem paginada aguarda a homologação dos filtros no Om.");
    }

    @Override
    public Optional<ApuracaoSnapshot> findById(Integer nuApuracao,
            AuthorizationContext context) {
        try {
            Optional<BhApuracao> found = repository.findByNuApuracao(nuApuracao);
            if (found == null || !found.isPresent()) {
                return Optional.empty();
            }
            return Optional.of(toSnapshot(found.get()));
        } catch (Exception exception) {
            throw new ApuracaoBusinessException(ErrorCode.INTEGRATION,
                    "Não foi possível consultar a apuração no Om.");
        }
    }

    private static ApuracaoSnapshot toSnapshot(BhApuracao entity) {
        return ApuracaoSnapshot.builder()
                .nuApuracao(entity.getNuApuracao())
                .codConta(toString(entity.getCodConta()))
                .numContrato(toString(entity.getNumContrato()))
                .nuNota(toString(entity.getNuNota()))
                .sequenciaCon(toString(entity.getSequenciaCon()))
                .operadora(toString(entity.getOperadora()))
                .cliente(toString(entity.getCliente()))
                .codVend(toString(entity.getCodVend()))
                .referencia(toString(entity.getReferencia()))
                .referenciaAdiada(toString(entity.getReferenciaAdiada()))
                .dtVenc(toString(entity.getDtVenc()))
                .valor(entity.getValor())
                .valorRef(entity.getValorRef())
                .confirmado(toFlag(entity.getConfirmado()))
                .auditoriaFinalizada(toFlag(entity.getAuditoriaFinalizada()))
                .emailEnviado(toFlag(entity.getEmailEnviado()))
                .faturamentoLiberado(toFlag(entity.getFaturamentoLiberado()))
                .nuFila(toString(entity.getNuFila()))
                .plano(toString(entity.getPlano()))
                .idInstPrn(toString(entity.getIdInstPrn()))
                .possuiAnexo(toFlag(entity.getPossuiAnexo()))
                .build();
    }

    private static String toString(Object value) {
        return value == null ? null : String.valueOf(value);
    }

    private static String toFlag(Boolean value) {
        return value == null ? null : (value.booleanValue() ? "S" : "N");
    }
}
