package br.com.facilita.apuracao.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.time.LocalDate;

import org.junit.jupiter.api.Test;

class BhApuracaoObservedVersionTest {

    @Test
    void formatsValorAndDueDateWithStablePlainString() {
        String version = BhApuracaoObservedVersion.format(
                new BigDecimal("10.5000"), LocalDate.of(2026, 10, 15));
        assertEquals("10.5|2026-10-15", version);
    }

    @Test
    void matchesTrimmedExpectedVersion() {
        assertTrue(BhApuracaoObservedVersion.matches(" 10|2026-01-01 ", "10|2026-01-01"));
        assertFalse(BhApuracaoObservedVersion.matches("", "10|2026-01-01"));
        assertFalse(BhApuracaoObservedVersion.matches("10|2026-01-02", "10|2026-01-01"));
    }
}
