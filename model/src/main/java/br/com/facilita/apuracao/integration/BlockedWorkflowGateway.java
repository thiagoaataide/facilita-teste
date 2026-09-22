package br.com.facilita.apuracao.integration;

import com.google.inject.Inject;

import br.com.facilita.apuracao.api.GetTarefaRequest;
import br.com.facilita.apuracao.api.GetTarefaResponse;
import br.com.facilita.apuracao.api.error.ErrorCode;
import br.com.facilita.apuracao.error.ApuracaoBusinessException;
import br.com.facilita.apuracao.port.AuthorizationContext;
import br.com.sankhya.studio.stereotypes.Component;

/** Bloqueia workflow até a consulta oficial e as permissões serem comprovadas. */
@Component
public final class BlockedWorkflowGateway implements WorkflowGateway {

    @Inject
    protected BlockedWorkflowGateway() {
    }

    @Override
    public GetTarefaResponse getTarefa(GetTarefaRequest request, AuthorizationContext context) {
        throw new ApuracaoBusinessException(ErrorCode.INTEGRATION,
                "O serviço oficial de workflow ainda não foi homologado no Om.");
    }
}
