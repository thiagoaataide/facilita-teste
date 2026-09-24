package br.com.facilita.apuracao.repository;

import java.math.BigDecimal;

import br.com.facilita.apuracao.domain.ApuracaoSnapshot;
import br.com.facilita.apuracao.model.BhApuracao;

/** Projeção comum entre leitura e gravação da apuração nativa. */
public final class BhApuracaoSnapshotMapper {

    private BhApuracaoSnapshotMapper() {
    }

    public static ApuracaoSnapshot toSnapshot(BhApuracao entity) {
        if (entity == null) {
            return null;
        }
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
                .version(BhApuracaoObservedVersion.fromEntity(entity))
                .build();
    }

    private static String toString(Object value) {
        return value == null ? null : String.valueOf(value);
    }

    private static String toFlag(Boolean value) {
        return value == null ? null : (value.booleanValue() ? "S" : "N");
    }
}
