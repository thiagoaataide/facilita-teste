package br.com.facilita.apuracao.integration;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.google.inject.Inject;

import br.com.facilita.apuracao.api.AnexarRequest;
import br.com.facilita.apuracao.api.AnexoResponse;
import br.com.facilita.apuracao.api.ListarAnexosRequest;
import br.com.facilita.apuracao.api.ListarAnexosResponse;
import br.com.facilita.apuracao.api.error.ErrorCode;
import br.com.facilita.apuracao.error.ApuracaoBusinessException;
import br.com.facilita.apuracao.model.AnexoSistema;
import br.com.facilita.apuracao.port.AuthorizationContext;
import br.com.facilita.apuracao.repository.AnexoArquivoRow;
import br.com.facilita.apuracao.repository.AnexoSistemaRepository;
import br.com.sankhya.studio.stereotypes.Component;

/** Lista anexos da apuracao. O envio de arquivo continua bloqueado. */
@Component
public final class BlockedAnexoGateway implements AnexoGateway {

    private static final Logger LOGGER = Logger.getLogger(BlockedAnexoGateway.class.getName());
    private static final String INSTANCIA_APURACAO = "bhApuracao";

    private final AnexoSistemaRepository anexos;

    @Inject
    protected BlockedAnexoGateway(AnexoSistemaRepository anexos) {
        this.anexos = anexos;
    }

    @Override
    public AnexoResponse anexar(AnexarRequest request, AuthorizationContext context) {
        throw new ApuracaoBusinessException(ErrorCode.INTEGRATION,
                "O serviço oficial de anexos ainda não foi homologado no Om.");
    }

    @Override
    public ListarAnexosResponse listarAnexos(ListarAnexosRequest request,
            AuthorizationContext context) {
        String chave = AnexoSistemaListMapper.chave(request.getNuApuracao());
        List<AnexoArquivoRow> rows;
        try {
            rows = anexos.findArquivos(INSTANCIA_APURACAO, chave);
        } catch (RuntimeException exception) {
            LOGGER.log(Level.SEVERE, "Falha ao listar anexos da apuracao " + chave, exception);
            throw new ApuracaoBusinessException(ErrorCode.INTEGRATION,
                    "A consulta de anexos falhou.");
        }
        return AnexoSistemaListMapper.toResponse(toAnexos(rows));
    }

    private static List<AnexoSistema> toAnexos(List<AnexoArquivoRow> rows) {
        List<AnexoSistema> anexos = new ArrayList<AnexoSistema>();
        if (rows == null) {
            return anexos;
        }
        for (int i = 0; i < rows.size(); i++) {
            AnexoArquivoRow row = rows.get(i);
            if (row == null) {
                continue;
            }
            AnexoSistema anexo = new AnexoSistema();
            if (row.getNuattach() != null) {
                anexo.setNuAttach(Integer.valueOf(row.getNuattach().intValue()));
            }
            anexo.setNomeArquivo(row.getNomearquivo());
            anexos.add(anexo);
        }
        return anexos;
    }
}
