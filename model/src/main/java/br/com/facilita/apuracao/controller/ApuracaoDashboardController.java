package br.com.facilita.apuracao.controller;

import javax.validation.Valid;

import com.google.inject.Inject;

import br.com.facilita.apuracao.api.AnexarRequest;
import br.com.facilita.apuracao.api.AnexoResponse;
import br.com.facilita.apuracao.api.ApiResponse;
import br.com.facilita.apuracao.api.ApuracaoResponse;
import br.com.facilita.apuracao.api.AtualizarApuracaoRequest;
import br.com.facilita.apuracao.api.ConfirmarApuracaoRequest;
import br.com.facilita.apuracao.api.GetTarefaRequest;
import br.com.facilita.apuracao.api.GetTarefaResponse;
import br.com.facilita.apuracao.api.ListarAnexosRequest;
import br.com.facilita.apuracao.api.ListarAnexosResponse;
import br.com.facilita.apuracao.api.ListarApuracoesRequest;
import br.com.facilita.apuracao.api.ListarApuracoesResponse;
import br.com.facilita.apuracao.api.ListarDetalheRequest;
import br.com.facilita.apuracao.api.SolicitarNovaAuditoriaRequest;
import br.com.facilita.apuracao.business.AnexarBusiness;
import br.com.facilita.apuracao.business.AtualizarApuracaoBusiness;
import br.com.facilita.apuracao.business.ConfirmarApuracaoBusiness;
import br.com.facilita.apuracao.business.GetTarefaBusiness;
import br.com.facilita.apuracao.business.ListarAnexosBusiness;
import br.com.facilita.apuracao.business.ListarApuracoesBusiness;
import br.com.facilita.apuracao.business.ListarDetalheBusiness;
import br.com.facilita.apuracao.business.SolicitarNovaAuditoriaBusiness;
import br.com.facilita.apuracao.port.AuthorizationContext;
import br.com.facilita.apuracao.security.OmAuthorizationContextResolver;
import br.com.sankhya.studio.annotations.Controller;
import br.com.sankhya.studio.annotations.enums.EJBTransactionType;
import br.com.sankhya.studio.persistence.Transactional;

/** Fachada pública do dashboard de apuração de faturas. */
@Controller(serviceName = "ApuracaoDashboardSP",
        transactionType = EJBTransactionType.NotSupported)
public class ApuracaoDashboardController {

    private final ListarApuracoesBusiness listarApuracoes;
    private final ListarDetalheBusiness listarDetalhe;
    private final AtualizarApuracaoBusiness atualizar;
    private final ConfirmarApuracaoBusiness confirmar;
    private final SolicitarNovaAuditoriaBusiness solicitarNovaAuditoria;
    private final AnexarBusiness anexar;
    private final ListarAnexosBusiness listarAnexos;
    private final GetTarefaBusiness getTarefa;
    private final OmAuthorizationContextResolver contextResolver;

    @Inject
    public ApuracaoDashboardController(ListarApuracoesBusiness listarApuracoes,
            ListarDetalheBusiness listarDetalhe, AtualizarApuracaoBusiness atualizar,
            ConfirmarApuracaoBusiness confirmar,
            SolicitarNovaAuditoriaBusiness solicitarNovaAuditoria, AnexarBusiness anexar,
            ListarAnexosBusiness listarAnexos, GetTarefaBusiness getTarefa,
            OmAuthorizationContextResolver contextResolver) {
        this.listarApuracoes = listarApuracoes;
        this.listarDetalhe = listarDetalhe;
        this.atualizar = atualizar;
        this.confirmar = confirmar;
        this.solicitarNovaAuditoria = solicitarNovaAuditoria;
        this.anexar = anexar;
        this.listarAnexos = listarAnexos;
        this.getTarefa = getTarefa;
        this.contextResolver = contextResolver;
    }

    /** Lista as apurações autorizadas e seus contadores. */
    public ApiResponse<ListarApuracoesResponse> listar(
            @Valid ListarApuracoesRequest request) {
        return listarApuracoes.execute(request, currentContext(), null);
    }

    /** Consulta o detalhe não sensível de uma apuração. */
    public ApiResponse<ApuracaoResponse> listarDetalhe(
            @Valid ListarDetalheRequest request) {
        return listarDetalhe.execute(request, currentContext(), null);
    }

    /** Atualiza somente os campos editáveis após validação de versão. */
    public ApiResponse<ApuracaoResponse> atualizar(
            @Valid AtualizarApuracaoRequest request) {
        return atualizar.execute(request, currentContext(), null);
    }

    /** Confirma uma apuração de forma idempotente. */
    @Transactional(Transactional.TxType.REQUIRED)
    public ApiResponse<ApuracaoResponse> confirmar(
            @Valid ConfirmarApuracaoRequest request) {
        return confirmar.execute(request, currentContext(), null);
    }

    /** Solicita nova auditoria para uma apuração elegível. */
    @Transactional(Transactional.TxType.REQUIRED)
    public ApiResponse<ApuracaoResponse> solicitarNovaAuditoria(
            @Valid SolicitarNovaAuditoriaRequest request) {
        return solicitarNovaAuditoria.execute(request, currentContext(), null);
    }

    /** Associa um upload temporário usando o gateway oficial de anexos. */
    @Transactional(Transactional.TxType.REQUIRED)
    public ApiResponse<AnexoResponse> anexar(@Valid AnexarRequest request) {
        return anexar.execute(request, currentContext(), null);
    }

    /** Lista os anexos autorizados de uma apuração. */
    public ApiResponse<ListarAnexosResponse> listarAnexos(
            @Valid ListarAnexosRequest request) {
        return listarAnexos.execute(request, currentContext(), null);
    }

    /** Obtém a tarefa pendente sem acessar diretamente a tabela de workflow. */
    public ApiResponse<GetTarefaResponse> getTarefa(@Valid GetTarefaRequest request) {
        return getTarefa.execute(request, currentContext(), null);
    }

    private AuthorizationContext currentContext() {
        return contextResolver.current();
    }
}
