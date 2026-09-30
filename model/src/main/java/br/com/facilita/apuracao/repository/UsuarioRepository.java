package br.com.facilita.apuracao.repository;

import java.math.BigDecimal;

import br.com.facilita.apuracao.model.Usuario;
import br.com.sankhya.sdk.data.repository.JapeRepository;
import br.com.sankhya.studio.persistence.NativeQuery;
import br.com.sankhya.studio.persistence.Parameter;
import br.com.sankhya.studio.stereotypes.Repository;

/** Leitura da permissao de nova auditoria do usuario. Sem gravacao. */
@Repository
public interface UsuarioRepository extends JapeRepository<BigDecimal, Usuario> {

    @NativeQuery(value = "SELECT NVL(BH_NOVAAUDIT, 'N') AS BH_NOVAAUDIT FROM TSIUSU "
            + "WHERE CODUSU = :codUsu", method = NativeQuery.ResultSetMethods.GET_STRING)
    String findNovaAuditoria(@Parameter(name = "codUsu") BigDecimal codUsu);
}
