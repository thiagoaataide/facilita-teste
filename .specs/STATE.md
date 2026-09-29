# Estado do projeto — Add-on de Apuração de Faturas

## Decisions

### AD-001 — Add-on isolado da UI e do legado

- **Status:** active
- O Service Provider será implementado neste repositório, separado do gadget
  HTML5 e do checkout legado `facilitatelecoment`.
- A UI chama o contrato do add-on; não grava diretamente em serviços legados.

### AD-002 — Schema existente é somente dependência

- **Status:** active
- `BH_FACAPU`, `TSIANX` e `TWFITAR` não serão criadas nem alteradas pelo add-on.
- O build deve desabilitar AutoDDL para a entrega e não deve produzir
  dicionário/DDL dessas tabelas.

### AD-003 — Controller SDK como fronteira pública

- **Status:** active
- O ponto de entrada usa `@Controller(serviceName =
  "ApuracaoDashboardSP")`, DTOs, `@Valid` e `@Transactional` onde
  houver mutação.
- O prefixo externo `<appKey>@...` só será fixado depois do novo appKey ser
  registrado.

### AD-004 — Context7 antes da documentação oficial

- **Status:** active
- Toda decisão sobre SDK/Gradle/API começa com `resolve-library-id` e
  `query-docs` no Context7. Se não houver correspondência confiável, a fonte
  oficial Sankhya é o fallback e a lacuna é registrada.

### AD-005 — AppKey oficial do Add-on separado da extensão legada

- **Decision**: O `appKey` confirmado no Portal para a solução **Apuração de
  Faturas** identifica este projeto Addon Studio e será usado no pacote de
  deploy; a extensão legada mantém sua própria identidade.
- **Reason**: Evitar misturar o fluxo de publicação do novo Add-on com o
  padrão de extensão legado.
- **Trade-off**: O contrato externo do Service Provider ainda precisa ser
  confirmado separadamente, mesmo com o appKey já validado.
- **Scope**: `build.gradle`, artefato `.exts`, dashboard HTML5 e specs desta
  feature.
- **Date**: 2026-09-22
- **Status**: active

### AD-006 — Dashboard para experiência; Provider para comandos de escrita
- **Decision**: O dashboard HTML5 mantém apresentação, filtros e interação;
  toda mutação passa por uma interface pequena do `ApuracaoDashboardSP`, que
  resolve usuário, autorização, estado e transação no backend.
- **Reason**: A falha de `GridConfig` exige substituir a experiência da tela,
  não deslocar regras autoritativas para JavaScript. Um caminho de leitura
  server-side pode acelerar a recuperação sem duplicar as regras de escrita.
- **Trade-off**: Lista e detalhe podem permanecer temporariamente em JSP
  somente leitura se os parâmetros forem vinculados ou validados, os campos
  forem allowlisted e a autorização for comprovada; caso contrário, a leitura
  também deve passar pelo Provider. Anexos e workflow seguem como fase
  separada, salvo se o aceite funcional os tornar pré-requisitos.
- **Scope**: gadget HTML5 e
  `.specs/features/apuracao-dashboard-service-provider/`; nenhuma escrita
  manual no navegador nem chamada direta a serviços legados para mutar
  `BH_FACAPU`.
- **Date**: 2026-09-23
- **Status**: active

### AD-007 — Tela operacional no HTML5 do add-on

- **Decision**: A tela que executa ações da apuração vive em `vc/src/main/webapp/html5` deste add-on e chama `ServiceProxy.callService("facilita-apuracao-fatura-addon@ApuracaoDashboardSP.{metodo}", ...)`. O gadget de BI permanece só leitura até essa tela cobrir o mesmo uso.
- **Reason**: O broker de `/mge/service.sbr` não encontra o provedor do add-on. A tela HTML5 do módulo, aberta pelo menu, entrega o `ServiceProxy` e a sessão. `listarAnexos` da apuração `185045240` retornou o anexo `199605` de `TSIANX` em 2026-09-29.
- **Trade-off**: O `appKey` continua sendo a identidade da solução no Place, e não o prefixo da chamada. Enviar arquivo e abrir tarefa continuam fora até haver contrato homologado.
- **Scope**: menu `FACAPU`, telas HTML5 do add-on e todas as chamadas de `ApuracaoDashboardSP` feitas pela UI.
- **Date**: 2026-09-29
- **Status**: active

### AD-008 — Gravação da apuração por UPDATE nativo condicional, sem tabela de idempotência

- **Decision**: `confirmar` e `solicitarNovaAuditoria` gravam `BH_FACAPU` com `@Modifying @NativeQuery`, com o estado esperado e a versão (`VALOR|DTVENC`) no `WHERE`, e releem pelo SQL nativo do detalhe. A `idempotencyKey` continua obrigatória no contrato, mas não é guardada. Um reenvio depois do commit recebe `CONFLICT`, sem segundo efeito.
- **Reason**: A carga da entidade parcial por `@Criteria` falhou no Om, e o SQL nativo foi o caminho comprovado. Guardar a chave exigiria tabela nova, e o projeto não cria DDL.
- **Trade-off**: O UPDATE nativo não dispara eventos JAPE da instância `bhApuracao`; o fonte legado não mostra listener nessa tabela. O Javadoc de `ApuracaoStore.confirm` deixa de pedir replay pela chave.
- **Scope**: `BhApuracaoJapeStore`, `BhApuracaoRepository`, `ApuracaoStore` e F4 de `tela-apuracao-addon`. F5 decide à parte como gravar valor e vencimento.
- **Date**: 2026-09-29
- **Status**: proposed

## Handoff

- **Feature**: `.specs/features/tela-apuracao-addon/`
- **Phase / Task**: F1–F3 concluídas e comprovadas no Om (versão 1.0.6)
- **Completed**: grade com filtros de mês, pendentes e anexo; detalhe por SQL nativo; anexos de `TSIANX`
- **In-progress**: nenhuma
- **Next step**: aprovar o desenho da F4, a AD-008 e as tarefas T7–T12; depois executar a partir de T7 e T8
- **Blockers**: as gravações ainda leem a linha por `@Criteria` na entidade parcial, que falhou no detalhe; fontes Java em UTF-8 quebram acentos no Om
- **Uncommitted files**: listagem TSIANX anterior, `build.gradle` e esta spec, se ainda não commitados
- **Branch**: `main`
