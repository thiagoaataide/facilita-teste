package br.com.facilita.apuracao.integration;

import com.google.inject.Inject;

import br.com.facilita.apuracao.api.AnexarRequest;
import br.com.facilita.apuracao.api.AnexoResponse;
import br.com.facilita.apuracao.api.ListarAnexosRequest;
import br.com.facilita.apuracao.api.ListarAnexosResponse;
import br.com.facilita.apuracao.api.error.ErrorCode;
import br.com.facilita.apuracao.error.ApuracaoBusinessException;
import br.com.facilita.apuracao.port.AuthorizationContext;
import br.com.sankhya.studio.stereotypes.Component;

/** Bloqueia anexos até o serviço oficial e suas permissões serem comprovados. */
@Component
public final class BlockedAnexoGateway implements AnexoGateway {

    @Inject
    protected BlockedAnexoGateway() {
    }

    @Override
    public AnexoResponse anexar(AnexarRequest request, AuthorizationContext context) {
        throw blocked();
    }

    @Override
    public ListarAnexosResponse listarAnexos(ListarAnexosRequest request,
            AuthorizationContext context) {
        throw blocked();
    }

    private static ApuracaoBusinessException blocked() {
        return new ApuracaoBusinessException(ErrorCode.INTEGRATION,
                "O serviço oficial de anexos ainda não foi homologado no Om.");
    }
}
