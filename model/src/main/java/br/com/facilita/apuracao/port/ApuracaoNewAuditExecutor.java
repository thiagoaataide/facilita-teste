package br.com.facilita.apuracao.port;

import br.com.facilita.apuracao.domain.SolicitarNovaAuditoriaCommand;

/** Executa a solicitação de nova auditoria em uma transação própria. */
public interface ApuracaoNewAuditExecutor {

    void requestNewAudit(SolicitarNovaAuditoriaCommand command);
}
