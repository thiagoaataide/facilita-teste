package br.com.facilita.apuracao.domain;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;

import br.com.facilita.apuracao.repository.NomeAnexoRow;

/** Leitura da conta, do parceiro e do vencimento para o nome do anexo. */
public final class NomeAnexoDados {

    private final String identificador;
    private final String nomeParceiro;
    private final String documento;
    private final LocalDate vencimento;

    private NomeAnexoDados(String identificador, String nomeParceiro, String documento,
            LocalDate vencimento) {
        this.identificador = identificador;
        this.nomeParceiro = nomeParceiro;
        this.documento = documento;
        this.vencimento = vencimento;
    }

    public static NomeAnexoDados primeira(List<NomeAnexoRow> rows) {
        if (rows == null || rows.isEmpty() || rows.get(0) == null) {
            return null;
        }
        NomeAnexoRow row = rows.get(0);
        return new NomeAnexoDados(row.getIdentificador(), row.getNomeparc(), row.getCgccpf(),
                parseDate(row.getDtvenc()));
    }

    public String getIdentificador() {
        return identificador;
    }

    public String getNomeParceiro() {
        return nomeParceiro;
    }

    public String getDocumento() {
        return documento;
    }

    public LocalDate getVencimento() {
        return vencimento;
    }

    private static LocalDate parseDate(String value) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }
        try {
            return LocalDate.parse(value.trim());
        } catch (DateTimeParseException exception) {
            return null;
        }
    }
}
