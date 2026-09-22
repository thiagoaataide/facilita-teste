package br.com.facilita.apuracao.repository;

import java.util.Optional;

import com.google.inject.Inject;

import br.com.facilita.apuracao.api.error.ErrorCode;
import br.com.facilita.apuracao.domain.ApuracaoSnapshot;
import br.com.facilita.apuracao.domain.AtualizarApuracaoCommand;
import br.com.facilita.apuracao.domain.ConfirmarApuracaoCommand;
import br.com.facilita.apuracao.domain.SolicitarNovaAuditoriaCommand;
import br.com.facilita.apuracao.error.ApuracaoBusinessException;
import br.com.facilita.apuracao.port.ApuracaoStore;
import br.com.sankhya.studio.stereotypes.Component;

/** Impede gravações até versão, permissões e regras do Om serem comprovadas. */
@Component
public final class BlockedApuracaoStore implements ApuracaoStore {

    @Inject
    protected BlockedApuracaoStore() {
    }

    @Override
    public Optional<ApuracaoSnapshot> findById(Integer nuApuracao) {
        throw blocked();
    }

    @Override
    public ApuracaoSnapshot updateEditableFields(AtualizarApuracaoCommand command) {
        throw blocked();
    }

    @Override
    public ApuracaoSnapshot confirm(ConfirmarApuracaoCommand command) {
        throw blocked();
    }

    @Override
    public ApuracaoSnapshot requestNewAudit(SolicitarNovaAuditoriaCommand command) {
        throw blocked();
    }

    private static ApuracaoBusinessException blocked() {
        return new ApuracaoBusinessException(ErrorCode.INTEGRATION,
                "A operação de escrita aguarda homologação do contrato do Om.");
    }
}
