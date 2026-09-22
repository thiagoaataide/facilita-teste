package br.com.facilita.apuracao.integration;

import br.com.facilita.apuracao.api.GetTarefaRequest;
import br.com.facilita.apuracao.api.GetTarefaResponse;
import br.com.facilita.apuracao.port.AuthorizationContext;

/**
 * Porta para consulta do workflow nativo.
 *
 * <p>A implementação concreta deve usar o serviço oficial homologado e não
 * pode atualizar diretamente a tabela de tarefas.</p>
 */
public interface WorkflowGateway {

    GetTarefaResponse getTarefa(GetTarefaRequest request, AuthorizationContext context);
}
