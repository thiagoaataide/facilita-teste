package br.com.facilita.apuracao.business;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.google.inject.Inject;

import br.com.facilita.apuracao.api.AnexoResponse;
import br.com.facilita.apuracao.api.error.ErrorCode;
import br.com.facilita.apuracao.error.ApuracaoBusinessException;
import br.com.facilita.apuracao.integration.AnexoSistemaListMapper;
import br.com.facilita.apuracao.port.AuthorizationAction;
import br.com.facilita.apuracao.port.AuthorizationContext;
import br.com.facilita.apuracao.port.AuthorizationPort;
import br.com.facilita.apuracao.repository.AnexoEscopoRow;
import br.com.facilita.apuracao.repository.AnexoSistemaRepository;
import br.com.sankhya.studio.stereotypes.Component;

/**
 * Confere se o anexo pertence a apuracao.
 * A URL do visualizador fica vazia ate evidencia no Om.
 */
@Component
public final class AbrirAnexoBusiness {

    private static final Logger LOGGER = Logger.getLogger(AbrirAnexoBusiness.class.getName());
    private static final String INSTANCIA = "bhApuracao";

    private final AnexoSistemaRepository anexos;
    private final AuthorizationPort authorization;

    @Inject
    public AbrirAnexoBusiness(AnexoSistemaRepository anexos, AuthorizationPort authorization) {
        this.anexos = anexos;
        this.authorization = authorization;
    }

    public AnexoResponse abrir(Integer nuApuracao, Integer nuAttach, AuthorizationContext context) {
        authorization.requireAllowed(AuthorizationAction.LIST_ATTACHMENTS, context, null);
        List<AnexoEscopoRow> rows;
        try {
            rows = anexos.findNoRegistro(nuAttach, INSTANCIA, AnexoSistemaListMapper.chave(nuApuracao));
        } catch (RuntimeException exception) {
            LOGGER.log(Level.SEVERE, "Falha ao abrir o anexo " + nuAttach, exception);
            throw new ApuracaoBusinessException(ErrorCode.INTEGRATION,
                    "Nao foi possivel consultar o anexo no Om.");
        }
        if (rows == null || rows.isEmpty() || rows.get(0) == null) {
            throw new ApuracaoBusinessException(ErrorCode.VALIDATION,
                    "O anexo nao pertence a apuracao.", "nuAttach");
        }
        String chave = rows.get(0).getChavearquivo();
        if (chave == null || chave.trim().isEmpty()) {
            throw new ApuracaoBusinessException(ErrorCode.INTEGRATION,
                    "O anexo nao tem arquivo para abrir.");
        }
        AnexoResponse response = new AnexoResponse();
        response.setIdentifier(nuAttach.toString());
        response.setName(rows.get(0).getNomearquivo());
        return response;
    }
}
