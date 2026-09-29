package br.com.facilita.apuracao.integration;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Arrays;
import java.util.Collections;

import org.junit.jupiter.api.Test;

import br.com.facilita.apuracao.api.ListarAnexosResponse;
import br.com.facilita.apuracao.model.AnexoSistema;

class AnexoSistemaListMapperTest {

    @Test
    void montaAChaveLegadaDaApuracao() {
        assertEquals("185047303_bhApuracao", AnexoSistemaListMapper.chave(Integer.valueOf(185047303)));
    }

    @Test
    void devolveIdentificadorENome() {
        AnexoSistema row = new AnexoSistema();
        row.setNuAttach(Integer.valueOf(9));
        row.setNomeArquivo("fatura.pdf");

        ListarAnexosResponse response = AnexoSistemaListMapper.toResponse(Arrays.asList(row));

        assertEquals(1, response.getFiles().size());
        assertEquals("9", response.getFiles().get(0).getIdentifier());
        assertEquals("fatura.pdf", response.getFiles().get(0).getName());
    }

    @Test
    void listaVaziaQuandoNaoHaLinhas() {
        ListarAnexosResponse response = AnexoSistemaListMapper.toResponse(Collections.<AnexoSistema>emptyList());
        assertEquals(0, response.getFiles().size());
    }
}
