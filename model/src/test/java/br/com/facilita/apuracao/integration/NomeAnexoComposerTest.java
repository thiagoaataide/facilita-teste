package br.com.facilita.apuracao.integration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.time.LocalDate;

import org.junit.jupiter.api.Test;

class NomeAnexoComposerTest {

    @Test
    void compoeComoOLegadoComMesSemZero() {
        String nome = NomeAnexoComposer.compor("LINK 20 MB", LocalDate.of(2026, 9, 10),
                "07196307000159", "FO", "fatura.pdf");

        assertEquals("LINK 20 MB_2026_9_07196307000159_FO.pdf", nome);
    }

    @Test
    void naoCompoeQuandoONomeNaoTemPonto() {
        assertNull(NomeAnexoComposer.compor("LINK 20 MB", LocalDate.of(2026, 9, 10),
                "07196307000159", "FO", "fatura"));
    }
}
