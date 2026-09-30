package br.com.facilita.apuracao.integration;

import java.time.LocalDate;

/** Nome de TSIANX no formato de AnexosModel.atualizarTipoAnexo. */
public final class NomeAnexoComposer {

    private NomeAnexoComposer() {
    }

    public static String compor(String identificador, LocalDate vencimento, String documento,
            String tipo, String nomeOriginal) {
        if (identificador == null || vencimento == null || documento == null || tipo == null
                || nomeOriginal == null) {
            return null;
        }
        int ponto = nomeOriginal.indexOf('.');
        if (ponto < 0) {
            return null;
        }
        return identificador + "_" + vencimento.getYear() + "_" + vencimento.getMonthValue()
                + "_" + documento + "_" + tipo + nomeOriginal.substring(ponto);
    }
}
