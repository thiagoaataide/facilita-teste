package br.com.facilita.apuracao.error;

import java.util.UUID;
import java.util.regex.Pattern;

/**
 * Cria e normaliza identificadores usados para correlacionar uma chamada e
 * seus registros técnicos sem aceitar conteúdo de log controlado pelo cliente.
 */
public final class CorrelationIds {

    private static final Pattern SAFE_ID = Pattern.compile("[A-Za-z0-9][A-Za-z0-9._-]{0,63}");

    private CorrelationIds() {
    }

    /**
     * Gera um identificador opaco e não sensível para uma nova chamada.
     *
     * @return UUID em formato textual
     */
    public static String newId() {
        return UUID.randomUUID().toString();
    }

    /**
     * Preserva somente identificadores curtos e seguros; nos demais casos
     * gera um novo valor para evitar CRLF, espaços ou dados arbitrários em
     * logs e envelopes de erro.
     *
     * @param candidate valor recebido da borda, possivelmente nulo
     * @return candidato normalizado ou um novo identificador
     */
    public static String normalize(String candidate) {
        if (candidate == null) {
            return newId();
        }

        String normalized = candidate.trim();
        return SAFE_ID.matcher(normalized).matches() ? normalized : newId();
    }
}
