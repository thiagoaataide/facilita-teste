package br.com.facilita.apuracao.business;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.Test;

import br.com.facilita.apuracao.api.AnexoResponse;
import br.com.facilita.apuracao.api.error.ErrorCode;
import br.com.facilita.apuracao.domain.ApuracaoSnapshot;
import br.com.facilita.apuracao.error.ApuracaoBusinessException;
import br.com.facilita.apuracao.model.BhApuracao;
import br.com.facilita.apuracao.port.AuthorizationAction;
import br.com.facilita.apuracao.port.AuthorizationContext;
import br.com.facilita.apuracao.port.AuthorizationPort;
import br.com.facilita.apuracao.repository.AnexoEscopoRow;
import br.com.facilita.apuracao.repository.AnexoSistemaRepository;
import br.com.facilita.apuracao.repository.BhApuracaoRepository;
import br.com.facilita.apuracao.repository.NomeAnexoRow;

class ConcluirAnexoBusinessTest {

    private static final Integer NU = Integer.valueOf(185045004);
    private static final Integer ATTACH = Integer.valueOf(200981);

    @Test
    void gravaNomeDoLegadoTipoEPossuiAnexo() {
        Fake fake = new Fake();
        fake.dados = linha("LINK 20 MB", "Operadora X", "07196307000159", "2026-09-10");
        fake.arquivo = arquivo("fatura.pdf", "chave");
        fake.entidade = new BhApuracao();

        AnexoResponse response = business(fake).concluir(NU, ATTACH, "FO", contexto());

        assertEquals("LINK 20 MB_2026_9_07196307000159_FO.pdf", response.getName());
        assertEquals("200981", response.getIdentifier());
        assertEquals("FO", fake.tipoGravado);
        assertEquals("LINK 20 MB_2026_9_07196307000159_FO.pdf", fake.nomeGravado);
        assertEquals("Operadora X", fake.descricaoGravada);
        assertEquals(Boolean.TRUE, fake.entidadeSalva.getPossuiAnexo());
    }

    @Test
    void anexoDeOutraApuracaoNaoGrava() {
        Fake fake = new Fake();
        fake.dados = linha("LINK 20 MB", "Operadora X", "07196307000159", "2026-09-10");

        ApuracaoBusinessException exception = assertThrows(ApuracaoBusinessException.class,
                new org.junit.jupiter.api.function.Executable() {
                    @Override
                    public void execute() {
                        business(fake).concluir(NU, ATTACH, "FO", contexto());
                    }
                });

        assertEquals(ErrorCode.VALIDATION, exception.getCode());
        assertNull(fake.tipoGravado);
        assertNull(fake.entidadeSalva);
    }

    @Test
    void semVencimentoNaoGrava() {
        Fake fake = dadosSem("2026-09-10", "Operadora X");
        fake.dados = linha("LINK 20 MB", "Operadora X", "07196307000159", null);
        assertValidation(fake);
    }

    @Test
    void semParceiroNaoGrava() {
        Fake fake = new Fake();
        fake.dados = linha("LINK 20 MB", null, "07196307000159", "2026-09-10");
        assertValidation(fake);
    }

    private static void assertValidation(final Fake fake) {
        fake.arquivo = arquivo("fatura.pdf", "chave");
        fake.entidade = new BhApuracao();
        ApuracaoBusinessException exception = assertThrows(ApuracaoBusinessException.class,
                new org.junit.jupiter.api.function.Executable() {
                    @Override
                    public void execute() {
                        business(fake).concluir(NU, ATTACH, "FO", contexto());
                    }
                });
        assertEquals(ErrorCode.VALIDATION, exception.getCode());
        assertNull(fake.tipoGravado);
        assertNull(fake.entidadeSalva);
    }

    private static Fake dadosSem(String vencimento, String parceiro) {
        return new Fake();
    }

    private static ConcluirAnexoBusiness business(Fake fake) {
        return new ConcluirAnexoBusiness(fake.apuracoes(), fake.anexos(), permitir());
    }

    private static AuthorizationContext contexto() {
        return new AuthorizationContext("10");
    }

    private static AuthorizationPort permitir() {
        return new AuthorizationPort() {
            @Override
            public void requireAllowed(AuthorizationAction action, AuthorizationContext context,
                    ApuracaoSnapshot apuracao) {
            }
        };
    }

    private static NomeAnexoRow linha(final String identificador, final String nome,
            final String documento, final String vencimento) {
        return (NomeAnexoRow) Proxy.newProxyInstance(NomeAnexoRow.class.getClassLoader(),
                new Class<?>[] { NomeAnexoRow.class }, new InvocationHandler() {
                    @Override
                    public Object invoke(Object proxy, Method method, Object[] args) {
                        String name = method.getName();
                        if ("getIdentificador".equals(name)) {
                            return identificador;
                        }
                        if ("getNomeparc".equals(name)) {
                            return nome;
                        }
                        if ("getCgccpf".equals(name)) {
                            return documento;
                        }
                        if ("getDtvenc".equals(name)) {
                            return vencimento;
                        }
                        return null;
                    }
                });
    }

    private static AnexoEscopoRow arquivo(final String nome, final String chave) {
        return (AnexoEscopoRow) Proxy.newProxyInstance(AnexoEscopoRow.class.getClassLoader(),
                new Class<?>[] { AnexoEscopoRow.class }, new InvocationHandler() {
                    @Override
                    public Object invoke(Object proxy, Method method, Object[] args) {
                        if ("getNuattach".equals(method.getName())) {
                            return new BigDecimal(ATTACH.intValue());
                        }
                        if ("getNomearquivo".equals(method.getName())) {
                            return nome;
                        }
                        if ("getChavearquivo".equals(method.getName())) {
                            return chave;
                        }
                        return null;
                    }
                });
    }

    private static final class Fake {
        NomeAnexoRow dados;
        AnexoEscopoRow arquivo;
        BhApuracao entidade;
        BhApuracao entidadeSalva;
        String tipoGravado;
        String nomeGravado;
        String descricaoGravada;

        BhApuracaoRepository apuracoes() {
            return (BhApuracaoRepository) Proxy.newProxyInstance(
                    BhApuracaoRepository.class.getClassLoader(),
                    new Class<?>[] { BhApuracaoRepository.class },
                    new InvocationHandler() {
                        @Override
                        public Object invoke(Object proxy, Method method, Object[] args) {
                            if ("findNomeAnexo".equals(method.getName())) {
                                return dados == null
                                        ? Collections.emptyList()
                                        : Collections.singletonList(dados);
                            }
                            if ("findByPK".equals(method.getName())) {
                                return entidade;
                            }
                            if ("save".equals(method.getName())) {
                                entidadeSalva = (BhApuracao) args[0];
                                return entidadeSalva;
                            }
                            return null;
                        }
                    });
        }

        AnexoSistemaRepository anexos() {
            return (AnexoSistemaRepository) Proxy.newProxyInstance(
                    AnexoSistemaRepository.class.getClassLoader(),
                    new Class<?>[] { AnexoSistemaRepository.class },
                    new InvocationHandler() {
                        @Override
                        public Object invoke(Object proxy, Method method, Object[] args) {
                            if ("findNoRegistro".equals(method.getName())) {
                                return arquivo == null
                                        ? Collections.<AnexoEscopoRow>emptyList()
                                        : Collections.singletonList(arquivo);
                            }
                            if ("atualizarNome".equals(method.getName())) {
                                tipoGravado = (String) args[0];
                                nomeGravado = (String) args[1];
                                descricaoGravada = (String) args[2];
                            }
                            return null;
                        }
                    });
        }
    }
}
