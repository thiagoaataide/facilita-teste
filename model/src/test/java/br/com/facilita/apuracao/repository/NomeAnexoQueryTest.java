package br.com.facilita.apuracao.repository;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.reflect.Method;

import org.junit.jupiter.api.Test;

import br.com.sankhya.studio.persistence.NativeQuery;

class NomeAnexoQueryTest {

    @Test
    void consultaAllowlistedSemSelectEstrela() throws Exception {
        Method method = BhApuracaoRepository.class.getMethod("findNomeAnexo", Integer.class);
        String sql = method.getAnnotation(NativeQuery.class).value().toUpperCase();

        assertFalse(sql.contains("SELECT *"));
        assertTrue(sql.contains("BH_FACAPU"));
        assertTrue(sql.contains("BH_FACCON"));
        assertTrue(sql.contains("TGFPAR"));
        assertTrue(sql.contains("CON.IDENTIFICADOR"));
        assertTrue(sql.contains("OPE.NOMEPARC"));
        assertTrue(sql.contains("TIT.CGC_CPF"));
        assertTrue(sql.contains("APU.DTVENC"));
    }
}
