package br.com.facilita.apuracao.business;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.google.inject.Inject;

import br.com.facilita.apuracao.api.AnexoResponse;
import br.com.facilita.apuracao.api.error.ErrorCode;
import br.com.facilita.apuracao.domain.NomeAnexoDados;
import br.com.facilita.apuracao.error.ApuracaoBusinessException;
import br.com.facilita.apuracao.integration.AnexoSistemaListMapper;
import br.com.facilita.apuracao.integration.NomeAnexoComposer;
import br.com.facilita.apuracao.model.BhApuracao;
import br.com.facilita.apuracao.port.AuthorizationAction;
import br.com.facilita.apuracao.port.AuthorizationContext;
import br.com.facilita.apuracao.port.AuthorizationPort;
import br.com.facilita.apuracao.repository.AnexoEscopoRow;
import br.com.facilita.apuracao.repository.AnexoSistemaRepository;
import br.com.facilita.apuracao.repository.BhApuracaoRepository;
import br.com.facilita.apuracao.repository.NomeAnexoRow;
import br.com.sankhya.studio.stereotypes.Component;

/** Aplica o nome do legado e marca o anexo so depois da associacao. */
@Component
public final class ConcluirAnexoBusiness {

    private static final Logger LOGGER = Logger.getLogger(ConcluirAnexoBusiness.class.getName());
    private static final String INSTANCIA = "bhApuracao";
    private static final Set<String> TIPOS = Collections.unmodifiableSet(new HashSet<String>(
            Arrays.asList("FO", "2V", "FA", "BO", "NF", "RE")));

    private final BhApuracaoRepository apuracoes;
    private final AnexoSistemaRepository anexos;
    private final AuthorizationPort authorization;

    @Inject
    public ConcluirAnexoBusiness(BhApuracaoRepository apuracoes, AnexoSistemaRepository anexos,
            AuthorizationPort authorization) {
        this.apuracoes = apuracoes;
        this.anexos = anexos;
        this.authorization = authorization;
    }

    public void preparar(Integer nuApuracao, String nomeOriginal, String tipo,
            AuthorizationContext context) {
        authorization.requireAllowed(AuthorizationAction.ATTACH, context, null);
        exigirTipo(tipo);
        if (NomeAnexoComposer.compor("x", java.time.LocalDate.of(2000, 1, 1), "0", tipo,
                nomeOriginal) == null) {
            throw new ApuracaoBusinessException(ErrorCode.VALIDATION,
                    "O nome do arquivo nao tem extensao.", "nameAttach");
        }
        exigirDados(carregarDados(nuApuracao));
    }

    public AnexoResponse concluir(Integer nuApuracao, Integer nuAttach, String tipo,
            AuthorizationContext context) {
        authorization.requireAllowed(AuthorizationAction.ATTACH, context, null);
        exigirTipo(tipo);
        NomeAnexoDados dados = exigirDados(carregarDados(nuApuracao));
        AnexoEscopoRow arquivo = carregarArquivo(nuApuracao, nuAttach);
        String nome = NomeAnexoComposer.compor(dados.getIdentificador(), dados.getVencimento(),
                dados.getDocumento(), tipo, arquivo.getNomearquivo());
        if (nome == null) {
            throw new ApuracaoBusinessException(ErrorCode.VALIDATION,
                    "O nome do arquivo nao tem extensao.", "nameAttach");
        }
        try {
            anexos.atualizarNome(tipo, nome, dados.getNomeParceiro(), nuAttach, INSTANCIA,
                    AnexoSistemaListMapper.chave(nuApuracao));
        } catch (RuntimeException exception) {
            LOGGER.log(Level.SEVERE, "Falha ao gravar o nome do anexo " + nuAttach, exception);
            throw new ApuracaoBusinessException(ErrorCode.INTEGRATION,
                    "Nao foi possivel gravar o nome do anexo.");
        }
        marcarPossuiAnexo(nuApuracao);
        AnexoResponse response = new AnexoResponse();
        response.setIdentifier(nuAttach.toString());
        response.setName(nome);
        return response;
    }

    private NomeAnexoDados carregarDados(Integer nuApuracao) {
        List<NomeAnexoRow> rows;
        try {
            rows = apuracoes.findNomeAnexo(nuApuracao);
        } catch (RuntimeException exception) {
            LOGGER.log(Level.SEVERE, "Falha ao ler dados do nome da apuracao " + nuApuracao,
                    exception);
            throw new ApuracaoBusinessException(ErrorCode.INTEGRATION,
                    "Nao foi possivel consultar a apuracao no Om.");
        }
        NomeAnexoDados dados = NomeAnexoDados.primeira(rows);
        if (dados == null) {
            throw new ApuracaoBusinessException(ErrorCode.VALIDATION,
                    "A apuracao informada nao foi encontrada.", "nuApuracao");
        }
        return dados;
    }

    private static NomeAnexoDados exigirDados(NomeAnexoDados dados) {
        if (dados.getVencimento() == null) {
            throw new ApuracaoBusinessException(ErrorCode.VALIDATION,
                    "A apuracao nao tem vencimento.", "dtVenc");
        }
        if (emBranco(dados.getNomeParceiro())) {
            throw new ApuracaoBusinessException(ErrorCode.VALIDATION,
                    "A conta nao tem parceiro.", "nomeParceiro");
        }
        if (emBranco(dados.getIdentificador()) || emBranco(dados.getDocumento())) {
            throw new ApuracaoBusinessException(ErrorCode.VALIDATION,
                    "A conta nao tem os dados do nome do anexo.", "identificador");
        }
        return dados;
    }

    private AnexoEscopoRow carregarArquivo(Integer nuApuracao, Integer nuAttach) {
        List<AnexoEscopoRow> rows;
        try {
            rows = anexos.findNoRegistro(nuAttach, INSTANCIA, AnexoSistemaListMapper.chave(nuApuracao));
        } catch (RuntimeException exception) {
            LOGGER.log(Level.SEVERE, "Falha ao localizar o anexo " + nuAttach, exception);
            throw new ApuracaoBusinessException(ErrorCode.INTEGRATION,
                    "Nao foi possivel consultar o anexo no Om.");
        }
        if (rows == null || rows.isEmpty() || rows.get(0) == null) {
            throw new ApuracaoBusinessException(ErrorCode.VALIDATION,
                    "O anexo nao pertence a apuracao.", "nuAttach");
        }
        return rows.get(0);
    }

    private void marcarPossuiAnexo(Integer nuApuracao) {
        BhApuracao entity;
        try {
            entity = apuracoes.findByPK(nuApuracao);
        } catch (Exception exception) {
            LOGGER.log(Level.SEVERE, "Falha ao carregar a apuracao " + nuApuracao, exception);
            throw new ApuracaoBusinessException(ErrorCode.INTEGRATION,
                    "Nao foi possivel consultar a apuracao no Om.");
        }
        if (entity == null) {
            throw new ApuracaoBusinessException(ErrorCode.VALIDATION,
                    "A apuracao informada nao foi encontrada.", "nuApuracao");
        }
        entity.setPossuiAnexo(Boolean.TRUE);
        try {
            apuracoes.save(entity);
        } catch (Exception exception) {
            LOGGER.log(Level.SEVERE, "Falha ao marcar anexo na apuracao " + nuApuracao, exception);
            throw new ApuracaoBusinessException(ErrorCode.INTEGRATION,
                    "Nao foi possivel gravar a apuracao no Om.");
        }
    }

    private static void exigirTipo(String tipo) {
        if (tipo == null || !TIPOS.contains(tipo)) {
            throw new ApuracaoBusinessException(ErrorCode.VALIDATION,
                    "O tipo do anexo nao e aceito.", "tipo");
        }
    }

    private static boolean emBranco(String value) {
        return value == null || value.trim().isEmpty();
    }
}
