package br.com.facilita.apuracao.integration;

import br.com.facilita.apuracao.api.AnexarRequest;
import br.com.facilita.apuracao.api.AnexoResponse;
import br.com.facilita.apuracao.api.ListarAnexosRequest;
import br.com.facilita.apuracao.api.ListarAnexosResponse;
import br.com.facilita.apuracao.port.AuthorizationContext;

/**
 * Porta para o serviço homologado de anexos do ambiente Sankhya.
 *
 * <p>A implementação concreta só deve ser criada depois da captura do
 * contrato de upload, associação, MIME, antivírus e compensação no Om.</p>
 */
public interface AnexoGateway {

    AnexoResponse anexar(AnexarRequest request, AuthorizationContext context);

    ListarAnexosResponse listarAnexos(ListarAnexosRequest request,
            AuthorizationContext context);
}
