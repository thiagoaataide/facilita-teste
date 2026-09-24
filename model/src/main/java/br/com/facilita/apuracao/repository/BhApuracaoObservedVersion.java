package br.com.facilita.apuracao.repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

import br.com.facilita.apuracao.model.BhApuracao;

/**
 * Token de versão observável para edição enquanto {@code AD_DHALTER} não estiver
 * comprovado em {@code BH_FACAPU} no Om. Deve permanecer alinhado ao gadget HTML5.
 */
final class BhApuracaoObservedVersion {

    private static final DateTimeFormatter ISO_DATE = DateTimeFormatter.ISO_LOCAL_DATE;

    private BhApuracaoObservedVersion() {
    }

    static String fromEntity(BhApuracao entity) {
        if (entity == null) {
            return null;
        }
        return format(entity.getValor(), entity.getDtVenc());
    }

    static String format(BigDecimal valor, LocalDate dtVenc) {
        String valueToken = valor == null ? "" : valor.stripTrailingZeros().toPlainString();
        String dueDateToken = dtVenc == null ? "" : ISO_DATE.format(dtVenc);
        return valueToken + "|" + dueDateToken;
    }

    static String format(BigDecimal valor, String dtVenc) {
        if (dtVenc == null || dtVenc.trim().isEmpty()) {
            return format(valor, (LocalDate) null);
        }
        try {
            return format(valor, LocalDate.parse(dtVenc.trim(), ISO_DATE));
        } catch (DateTimeParseException exception) {
            return format(valor, (LocalDate) null);
        }
    }

    static boolean matches(String expected, String current) {
        if (expected == null || expected.trim().isEmpty()) {
            return false;
        }
        if (current == null || current.trim().isEmpty()) {
            return false;
        }
        return expected.trim().equals(current.trim());
    }
}
