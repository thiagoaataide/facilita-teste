package br.com.facilita.apuracao.domain;

import br.com.facilita.apuracao.api.ApuracaoResponse;

/** Mapeia a visão de domínio para o DTO público sem expor a entidade nativa. */
public final class ApuracaoResponseMapper {

    private ApuracaoResponseMapper() { }

    public static ApuracaoResponse toResponse(ApuracaoSnapshot snapshot) {
        if (snapshot == null) {
            return null;
        }
        ApuracaoResponse response = new ApuracaoResponse();
        response.setNuApuracao(snapshot.getNuApuracao());
        response.setCodConta(snapshot.getCodConta());
        response.setNumContrato(snapshot.getNumContrato());
        response.setNuNota(snapshot.getNuNota());
        response.setSequenciaCon(snapshot.getSequenciaCon());
        response.setOperadora(snapshot.getOperadora());
        response.setCliente(snapshot.getCliente());
        response.setCodVend(snapshot.getCodVend());
        response.setReferencia(snapshot.getReferencia());
        response.setReferenciaAdiada(snapshot.getReferenciaAdiada());
        response.setDtVenc(snapshot.getDtVenc());
        response.setValor(snapshot.getValor());
        response.setValorRef(snapshot.getValorRef());
        response.setConfirmado(snapshot.getConfirmado());
        response.setAuditoriaFinalizada(snapshot.getAuditoriaFinalizada());
        response.setEmailEnviado(snapshot.getEmailEnviado());
        response.setFaturamentoLiberado(snapshot.getFaturamentoLiberado());
        response.setNuFila(snapshot.getNuFila());
        response.setPlano(snapshot.getPlano());
        response.setIdInstPrn(snapshot.getIdInstPrn());
        response.setVersion(snapshot.getVersion());
        response.setPossuiAnexo(snapshot.getPossuiAnexo());
        return response;
    }
}
