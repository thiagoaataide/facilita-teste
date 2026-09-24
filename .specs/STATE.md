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

## Handoff

- **Feature**: `.specs/features/apuracao-dashboard-service-provider/`
- **Phase / Task**: Tasks / HTML5 T22 (UAT manual) → T15 homologação Om → T16
- **T9 status**: parcial — `allowsNewAudit()` removido do caso de uso; permissão
  fica só em `AuthorizationPort`; gravação de nova auditoria segue fail-closed
  até T16
- **T15 status**: parcial — `BhApuracaoJapeStore` habilita `atualizar` via JAPE;
  token provisório `{valor}|{dtVenc}`; confirmação/reauditoria ainda bloqueadas
- **Completed**: configuração segura, appKey oficial confirmado no Portal,
  fachada `ApuracaoDashboardSP`, artefato `.exts` gerado, DTOs/envelope, T7/T8,
  executor transacional T9 existente, metadata somente leitura e análise do
  baseline legado; spec/design/contracts/tasks alinhados aos comportamentos
  observados e às incompatibilidades atuais do gadget
- **In-progress**: T22 UAT no Om; homologar T15 (autorização real + gravação);
  T16 confirmação/reauditoria; T10/T11 complementares
- **Next step**: solicitante executar checklist `evidencias/t22-read-gate-addon.md`;
  homologar `atualizar` no Om com política de autorização; implementar adapters
  T16; harmonizar prefixo do provider; revalidar `service-providers.xml` em build
  com JDK
- **Blockers**: `FailClosedAuthorizationPort` impede mutações até política Om;
  a última compilação avisou que `service-providers.xml` foi criado manualmente;
  build local falhou por JRE 8 sem JDK nesta máquina;
  `ALL_TAB_COLUMNS` retornou somente metadata de `TSIANX`, enquanto
  `ALL_CONSTRAINTS` e leituras anteriores de dados da tabela retornaram
  `Não autorizado`; `TWFITAR` também segue sem autorização. A documentação
  pública descreve upload/associação genéricos e `CRUDServiceProvider.loadRecords`,
  mas o acesso à entidade/permissões, a chamada sob a sessão atual do Om,
  compensação, MIME/limite por arquivo e idempotência para `bhApuracao` seguem
  sem comprovação
- **Uncommitted files**: alterações T9 pré-existentes em `.specs/STATE.md`,
  `.specs/features/apuracao-dashboard-service-provider/{memory.md,tasks.md,contracts.md,evidencias/om-teste-metadata.md}` e
  `model/src/main/java/br/com/facilita/apuracao/{business/SolicitarNovaAuditoriaBusiness.java,controller/ApuracaoDashboardController.java}`;
  arquivos novos T9 `TransactionalApuracaoNewAuditExecutor.java` e
  `port/ApuracaoNewAuditExecutor.java`; T10 em `AnexarBusiness.java`,
  `AnexarRequest.java` e documentos/evidência do fluxo legado
- **Branch**: `main`
