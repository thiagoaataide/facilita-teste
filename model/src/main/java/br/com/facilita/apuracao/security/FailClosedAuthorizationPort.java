package br.com.facilita.apuracao.security;

import java.math.BigDecimal;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.google.inject.Inject;

import br.com.facilita.apuracao.api.error.ErrorCode;
import br.com.facilita.apuracao.domain.ApuracaoSnapshot;
import br.com.facilita.apuracao.error.ApuracaoBusinessException;
import br.com.facilita.apuracao.port.AuthorizationAction;
import br.com.facilita.apuracao.port.AuthorizationContext;
import br.com.facilita.apuracao.port.AuthorizationPort;
import br.com.facilita.apuracao.repository.UsuarioRepository;
import br.com.sankhya.studio.stereotypes.Component;

/**
 * Leitura e confirmacao usam o usuario da sessao, como no legado. Nova auditoria
 * exige TSIUSU.BH_NOVAAUDIT = 'S'. Demais acoes e qualquer falha seguem fechadas.
 */
@Component
public final class FailClosedAuthorizationPort implements AuthorizationPort {

    private static final Logger LOGGER =
            Logger.getLogger(FailClosedAuthorizationPort.class.getName());

    private final UsuarioRepository usuarios;

    @Inject
    protected FailClosedAuthorizationPort(UsuarioRepository usuarios) {
        this.usuarios = usuarios;
    }

    @Override
    public void requireAllowed(AuthorizationAction action, AuthorizationContext context,
            ApuracaoSnapshot apuracao) {
        if (AuthorizationAction.LIST == action
                || AuthorizationAction.DETAIL == action
                || AuthorizationAction.LIST_ATTACHMENTS == action
                || AuthorizationAction.CONFIRM == action) {
            return;
        }
        if (AuthorizationAction.REQUEST_NEW_AUDIT == action) {
            if (podeSolicitarNovaAuditoria(context)) {
                return;
            }
            throw new ApuracaoBusinessException(ErrorCode.FORBIDDEN,
                    "Usuario nao possui permissao para solicitar nova auditoria.");
        }
        throw new ApuracaoBusinessException(ErrorCode.FORBIDDEN,
                "A permissão desta operação ainda não foi homologada no Om.");
    }

    private boolean podeSolicitarNovaAuditoria(AuthorizationContext context) {
        if (context == null || context.getUserId() == null) {
            return false;
        }
        try {
            BigDecimal codUsu = new BigDecimal(context.getUserId().trim());
            return "S".equalsIgnoreCase(usuarios.findNovaAuditoria(codUsu));
        } catch (RuntimeException exception) {
            LOGGER.log(Level.SEVERE, "Falha ao ler BH_NOVAAUDIT do usuario "
                    + context.getUserId(), exception);
            return false;
        }
    }
}
