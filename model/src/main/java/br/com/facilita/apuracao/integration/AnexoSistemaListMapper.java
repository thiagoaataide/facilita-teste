package br.com.facilita.apuracao.integration;

import java.util.ArrayList;
import java.util.List;

import br.com.facilita.apuracao.api.AnexoResponse;
import br.com.facilita.apuracao.api.ListarAnexosResponse;
import br.com.facilita.apuracao.model.AnexoSistema;

/** Monta a lista publica a partir da chave legada da apuracao. */
public final class AnexoSistemaListMapper {

    private AnexoSistemaListMapper() {
    }

    public static String chave(Integer nuApuracao) {
        return nuApuracao.toString() + "_bhApuracao";
    }

    static ListarAnexosResponse toResponse(List<AnexoSistema> rows) {
        List<AnexoResponse> files = new ArrayList<AnexoResponse>();
        if (rows != null) {
            for (int i = 0; i < rows.size(); i++) {
                AnexoSistema row = rows.get(i);
                if (row == null) {
                    continue;
                }
                AnexoResponse file = new AnexoResponse();
                if (row.getNuAttach() != null) {
                    file.setIdentifier(row.getNuAttach().toString());
                }
                file.setName(row.getNomeArquivo());
                files.add(file);
            }
        }
        ListarAnexosResponse response = new ListarAnexosResponse();
        response.setFiles(files);
        return response;
    }
}
