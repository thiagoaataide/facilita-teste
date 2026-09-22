package br.com.facilita.apuracao.api;

import java.util.ArrayList;
import java.util.List;

public class ListarAnexosResponse {

    private List<AnexoResponse> files = new ArrayList<AnexoResponse>();

    public ListarAnexosResponse() {
    }

    public List<AnexoResponse> getFiles() {
        return files;
    }

    public void setFiles(List<AnexoResponse> files) {
        this.files = files;
    }
}
