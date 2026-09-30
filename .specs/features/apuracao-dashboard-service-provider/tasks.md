# Service Provider da Apuração de Faturas — Tasks

## Execution Protocol

Executar com a skill `tlc-spec-driven`: ler `STATE.md`, `spec.md` e
`design.md`, realizar uma tarefa atômica por vez, atualizar este arquivo e
registrar a evidência em `memory.md`. Não executar `gerarAddon` nem
`deployAddon`: quem gera e instala o pacote é o desenvolvedor.

**Design:** `design.md`  
**Status:** In Progress — baseline funcional do legado reconciliado em spec,
design e contrato; fachada pública permanece fail-closed. T7/T8 têm casos de
uso e fronteiras transacionais, mas os adapters de escrita não foram
homologados. T5 só entra no caminho crítico de leitura se HTML5 T22 reprovar a
consulta JSP; T9 ainda precisa corrigir a autorização da sessão. T10/T11 são
complementares, e a identidade externa segue gate de publicação.

**Faseamento aprovado em 2026-09-23:** recuperar primeiro uma consulta
somente leitura segura; habilitar comandos do Provider em fatias verticais;
deixar anexos/workflow como fase separada, a menos que o aceite funcional os
torne pré-requisitos. A segurança da consulta JSP é um gate, não uma suposição.

> Execução parcial: T1, evidência local, DTOs/envelope, infraestrutura de erro,
> portas de integração, casos de uso sem persistência, leitura nativa por
> `NUAPURACAO` e a fachada `ApuracaoDashboardSP` foram concluídos. A
> paginação completa, os adapters de escrita, anexos/workflow continuam
> bloqueados pelas evidências do Om.

## Test Coverage Matrix

> Provisória. O template não contém testes de domínio nem dependência de banco
> configurada; confirmar os tipos de teste antes de Execute.

| Code Layer | Required Test Type | Coverage Expectation | Location Pattern | Run Command |
| --- | --- | --- | --- | --- |
| Business/use case | unit | todos os branches dos SP-04 a SP-10 e edge cases da spec, com dependências mockadas | `model/src/test/java/**/apuracao/business/**` | `./gradlew :model:test` após configurar JUnit/Mockito |
| Controller | integration/manual | todas as ações: sucesso, validação, forbidden, conflict e integração | `model/src/test/java/**/apuracao/controller/**` + Om de homologação | `./gradlew :model:test` + roteiro manual |
| Repository/JAPE | integration/manual | consultas, filtros, projeções e falhas de metadata | `model/src/test/java/**/apuracao/repository/**` + Om | ambiente Sankhya de homologação |
| Entity/config/schema | none | somente compilação e inspeção de artefato; zero DDL para schema existente | `build.gradle`, `datadictionary/`, `dbscripts/` | `./gradlew clean build` + inspeção |

## Parallelism Assessment

| Test Type | Parallel-Safe? | Isolation Model | Evidence |
| --- | --- | --- | --- |
| Unit mockado | Yes | mocks e dados por teste | ainda não há testes; regra planejada |
| Repository/Om | No | base compartilhada e sessão do cliente | homologação manual necessária |
| Controller/Om | No | permissões e workflow compartilhados | homologação manual necessária |

## Gate Check Commands

| Gate Level | When to Use | Command |
| --- | --- | --- |
| Quick | tarefas de DTO/business com testes mockados | `./gradlew :model:test` |
| Full | controller, repository ou integração | `./gradlew :model:test` + smoke test no Om |
| Build | fundação/configuração e fim de fase | `./gradlew clean build` + inspeção de DDL/metadata |

> Os comandos acima são derivados do `model/build.gradle` e do README do
> template. Se o plugin ou o ambiente local exigir outro task, atualizar este
> arquivo antes de executar.

## Execution Plan

```text
Fase 0 — Fundação e evidências
T1 → T2 → T3 → T4 → T6

Fase 1 — Recuperar leitura sem aguardar mutações
HTML5 T22; usar o caminho JSP somente se parâmetros/projeção/autorização
forem comprovados. Se não forem, concluir a consulta segura do Provider (T5).

Fase 2 — Primeira mutação vertical
T5 (leitura por chave) + T6 + T7 → T15 → T12 (ação atualizar) → HTML5 T12

Fase 3 — Comandos de transição e UAT do MVP
T8 + T9 + T15 → T16 → T12/T13 → T14 → HTML5 T14/T21

Fase 4 — Capacidades complementares
T10 (anexos) e T11 (workflow), cada uma com autorização e contrato
homologados; integrar e executar UAT complementar depois do MVP.
```

## Task Breakdown

### T1 — Tornar a configuração segura para schema existente

**What:** verificar a configuração existente para `autoDDL=false`, identidade
do appKey e plugin >= 2.0.18 sem alterar o `build.gradle` mantido pelo usuário.

**Where:** `build.gradle`, `settings.gradle`  
**Depends on:** none  
**Requirement:** SP-01, SP-11  
**Tests:** none  
**Gate:** Build  
**Done when:**

- [x] `autoDDL=false` está explícito.
- [x] O `appKey` oficial da solução **Apuração de Faturas** foi confirmado no
  Portal do desenvolvedor como pertencente a este Add-on e coincide com o
  `addon.appKey` usado no build; ele não é a identidade da extensão legada.
- [x] `parceiroNome` está definido no `build.gradle` para esta solução.
- [x] a resolução usada na validação foi `2.18.0`; a declaração do projeto foi
  preservada conforme orientação do mantenedor e continua sem alteração.
- [x] `:model:compileJava` e `:model:test` passaram; a inspeção dos artefatos
  não encontrou DDL das três tabelas. `:model:test` está `NO-SOURCE` porque o
  build atual não declara dependências de testes.

### T2 — Capturar metadata e permissões do ambiente

**What:** produzir inventário somente leitura de entidades, chaves, tipos,
campos editáveis, permissões, versão/concorrência, anexos e workflow.

**Where:** `.specs/features/apuracao-dashboard-service-provider/evidencias/`  
**Depends on:** T1  
**Requirement:** SP-02 a SP-08  
**Tests:** integration/manual  
**Gate:** Full  
**Done when:**

- [x] metadata de `BH_FACAPU` e instância `bhApuracao` está anexada sem valores
  de negócio em `evidencias/om-teste-metadata.md`.
- [ ] request/response reais de mutações, anexos e workflow estão anexados sem
  segredos.
- [x] toda lacuna conhecida está marcada como resolvida ou bloqueadora.
- [x] nenhum comando de escrita foi executado durante a captura.

### T3 — Fixar identidade do componente e contrato externo

**What:** registrar novo appKey, package-base, parceiro e nome final do serviço
no Portal/Om e alinhar o contrato HTML5.

**Where:** `build.gradle`, `settings.gradle`, `.specs/.../contracts.md`  
**Depends on:** T2  
**Requirement:** SP-01, SP-12  
**Tests:** none  
**Gate:** Build  
**Done when:**

- [x] appKey deste Add-on está aprovado e não coincide com a extensão
  legada, conforme evidência do Portal.
- [x] `group`, `rootProject.name`, package-base e parceiro estão definidos no
  `build.gradle`, `settings.gradle` e código do Add-on.
- [ ] o nome externo `<appKey>@ApuracaoDashboardSP` foi confirmado.
- [ ] o adaptador HTML5 aponta para a identidade aprovada.
- [ ] o gadget atual ainda usa `facilitatelecom@ApuracaoDashboardSP`; alinhar
  com o appKey próprio do Add-on antes de homologar a integração ponta a ponta.

### T4 — Criar entidades JAPE nativas sem geração de DDL

**What:** implementar somente os mapeamentos comprovados de `BH_FACAPU` (e,
se necessário, projeções nativas de `TSIANX`/`TWFITAR`).

**Where:** `model/src/main/java/<package-base>/apuracao/model/`  
**Depends on:** T3  
**Requirement:** SP-02, SP-04, SP-05, SP-06, SP-08, SP-11  
**Tests:** none  
**Gate:** Build  
**Done when:**

- [x] os campos mapeados têm metadata capturada no T2.
- [x] não há entidade nova nem XML/DDL para tabela existente.
- [x] `@JapeEntity` usa tabela nativa e não importa JPA.
- [ ] projeções de anexos/workflow permanecem bloqueadas por autorização de
  leitura.

### T5 — Criar repository de leitura e projeções

**What:** implementar consultas parametrizadas para filtros, detalhe, anexos e
tarefa, com limite/paginação do contrato.

**Where:** `model/src/main/java/<package-base>/apuracao/repository/`  
**Depends on:** T4  
**Requirement:** SP-02, SP-03, SP-08  
**Tests:** integration/manual  
**Gate:** Full  
**Done when:**

- [x] não há concatenação de SQL nem `SELECT *` no repositório por chave.
- [ ] filtros, ordenação, contadores e paginação foram comparados com amostras
  do Om; a implementação continua bloqueada até o contrato de listagem.
- [x] ausência de dado por `NUAPURACAO` retorna `Optional.empty()`.
- [x] o baseline de filtros foi registrado: mês corrente/pendentes como estado
  inicial legado, busca por número/conta/valor/referência/vencimento e
  existência de anexo pela chave `NUAPURACAO || '_bhApuracao'` em `TSIANX`.
- [ ] implementar esses filtros somente após comprovar autorização de leitura,
  projeção e paginação; a referência não autoriza consulta direta não aprovada
  nem torna `POSSUIANEXO` equivalente à existência em `TSIANX`.

### T6 — Definir DTOs, validações e envelope de erro

**What:** criar requests/responses, `@Valid`, códigos de erro, correlation ID e
mapper sem expor entidades JAPE.

**Where:** `model/src/main/java/<package-base>/apuracao/api/` e `error/`  
**Depends on:** T3, T4  
**Requirement:** SP-03, SP-09, SP-10  
**Tests:** unit  
**Gate:** Quick  
**Done when:**

- [x] todos os requests rejeitam campos e formatos inválidos; constraints cobrem
  identificador positivo, version/idempotency, anexos, paginação, direção e
  campos mutáveis.
- [x] o envelope nunca serializa sessão, SQL ou stack trace; mensagens de
  negócio técnicas são substituídas por fallback seguro.
- [x] testes unitários cobrem validação, herança dos requests, correlation ID,
  serialização dos cinco códigos de erro e sanitização do envelope.

**Evidência:** `ApiContractValidationTest` — 12 testes aprovados por
`./gradlew :model:test` com JDK 21 (target Java 8).

### T7 — Implementar atualização de valor/vencimento

**What:** criar caso de uso transacional de `atualizar`, incluindo autorização,
versão observada e reconsulta após commit.

**Where:** `.../apuracao/business/AtualizarApuracaoBusiness.java`  
**Depends on:** T5, T6  
**Requirement:** SP-04  
**Tests:** unit  
**Gate:** Quick  
**Done when:** todos os branches de valor/data inválidos, registro inexistente e
conflito têm teste e retornam o código correto.

- [x] validação, usuário, autorização e versão observada são verificados antes
  de chamar a fronteira de escrita.
- [x] a gravação ocorre numa unidade `@Transactional(REQUIRED)` independente
  da orquestração, seguida de uma nova leitura após o retorno/commit.
- [x] testes cobrem valor negativo, vencimento vazio/maior que 10 caracteres,
  ausência dos campos mutáveis, registro inexistente, autorização negada,
  conflito de versão e conflito levantado durante a gravação.
- [x] conflito/erros de negócio da gravação preservam o correlation ID da
  requisição.

**Evidência:** `AtualizarApuracaoBusinessTest` — 12 testes; `:model:test` passou
com 24 testes no total. A persistência real continua fail-closed porque o campo
de concorrência e a gravação condicional ainda não foram homologados no Om.

### T8 — Implementar confirmação idempotente

**What:** criar caso de uso transacional de `confirmar`, exigindo valor,
permissão e estado elegível.

**Where:** `.../apuracao/business/ConfirmarApuracaoBusiness.java`  
**Depends on:** T5, T6  
**Requirement:** SP-05  
**Tests:** unit  
**Gate:** Quick  
**Done when:** confirmação repetida não duplica efeitos; sem valor e conflito
retornam erro sem alteração.

- [x] exige autorização no caso de uso; para novas chaves, valida valor e
  elegibilidade atomicamente na fronteira transacional, antes de qualquer efeito.
- [x] a chave idempotente é encaminhada sem prevalidar a versão observada fora
  da transação; o adapter deve reconhecer replay antes de validar versão e
  estado, e para chave nova rejeitar conflito sem mutar.
- [x] a confirmação roda em unidade `@Transactional(REQUIRED)` separada da
  orquestração e a resposta é construída por releitura após o commit.
- [x] testes cobrem replay sem efeito duplicado, falta de valor, conflito de
  versão, chave nova em apuração já confirmada, auditoria finalizada, registro
  ausente e autorização negada.

**Evidência:** `ConfirmarApuracaoBusinessTest` — 8 testes; `:model:test` passou
com 32 testes no total. O store real segue fail-closed até a operação atômica
de idempotência/concorrência ser homologada no Om.

### T9 — Implementar nova auditoria

**Status:** Parcial — o caso de uso delega ao executor transacional e relê após o
commit; a checagem `allowsNewAudit()` no snapshot foi removida e a permissão
fica em `AuthorizationPort`. A gravação em `BhApuracaoJapeStore.requestNewAudit`
permanece fail-closed até T16.

**What:** criar o caso de uso transacional de `solicitarNovaAuditoria`, usando
somente campos e permissão comprovados.

`motivo` permanece fora do contrato externo: não aparece no fluxo legado nem
no payload atual do gadget; o campo DTO existente não autoriza lógica nova.

**Where:** `.../apuracao/business/SolicitarNovaAuditoriaBusiness.java`  
**Depends on:** T2, T5, T6  
**Requirement:** SP-06  
**Tests:** integration/manual  
**Gate:** Full
**Done when:** a política resolve `BH_NOVAAUDIT` do usuário corrente; o reset
limita-se a `IDINSTPRN`, `CONFIRMADO`, `AUDITORIAFINALIZADA`, `EMAILENVIADO` e
`FATURAMENTOLIBERADO`; permissão, versão e gravação condicional são
homologadas no Om. Até lá, o comando permanece bloqueado.

- [x] comportamento de referência e lista exata de campos reiniciados foram
  registrados em spec/design/evidência local.
- [x] remover a leitura de `allowsNewAudit()` do snapshot e exigir a regra na
  autorização do usuário corrente.
- [ ] confirmar o fluxo no Om após adapter T16 e política de autorização.
- [ ] homologar reset atômico e confirmar que valor, vencimento e indicador de
  anexo são preservados.

### T10 — Implementar gateway de anexos

**What:** criar `AnexoGateway` para `anexar` e `listarAnexos`, usando o serviço
oficial homologado, limite/tipo/MIME e chave de idempotência.

**Where:** `.../apuracao/integration/AnexoGateway.java`  
**Depends on:** T2, T5, T6  
**Requirement:** SP-07  
**Tests:** integration/manual  
**Gate:** Full
**Done when:** upload e associação estão comprovados, não há mutação direta não
homologada de `TSIANX` e há compensação para falha entre as etapas.

**Status:** Parcial — a porta e os casos de uso existem; tipos aceitos e escopo
da chave de upload foram adaptados da referência local legada. O gadget atual
envia outro formato de chave; o gateway concreto permanece fail-closed.

- [x] A validação aceita somente os tipos do seletor legado (`FO`, `2V`, `FA`,
  `BO`, `NF`, `RE`) e exige a chave de sessão vinculada à apuração.
- [x] A incompatibilidade entre `ANEXO_SISTEMA_bhApuracao_<id>` e a chave
  `APURACAO_DASHBOARD_<id>_<timestamp>` enviada pelo gadget foi registrada.
- [ ] Upload, associação, listagem autorizada, limite/MIME/antivírus,
  idempotência e compensação continuam pendentes de homologação no Om.
- [ ] harmonizar o formato da chave nos dois lados e comprovar que o serviço
  suporta a mesma chave usada no `sessionUpload.mge` antes de ativar o gateway.

**Evidência local:** `evidencias/legacy-anexo-flow.md`. O fluxo legado é uma
referência comportamental, não comprova que seu serviço ou suas permissões
possam ser reutilizados pelo Add-on.

**Divergência do cliente:** `evidencias/html5-contract-alignment.md` registra
as linhas do gadget que usam outro formato de chave de upload.

> Em 2026-09-23, foi capturada somente a metadata de colunas de `TSIANX` via
> `ALL_TAB_COLUMNS`. A documentação pública descreve upload e associação
> genéricos e `CRUDServiceProvider.loadRecords` como consulta genérica, mas
> ainda faltam entidade/permissões e chamada sob a sessão do Add-on, além de
> MIME/limite por arquivo, idempotência e compensação homologados.
> `ALL_CONSTRAINTS` segue sem autorização; não foi executada mutação.

### T11 — Implementar gateway de workflow

**What:** criar `WorkflowGateway` para `getTarefa`, retornando o identificador
correto para abertura da tarefa nativa.

**Where:** `.../apuracao/integration/WorkflowGateway.java`  
**Depends on:** T2, T5, T6  
**Requirement:** SP-08  
**Tests:** integration/manual  
**Gate:** Full
**Done when:** tarefa existente, ausência de tarefa e usuário sem permissão têm
respostas comprovadas; não há update direto em `TWFITAR`.

> A porta e o caso de uso foram criados; o gateway concreto permanece bloqueado
> porque `TWFITAR` retornou `Não autorizado` no ambiente de teste.

> O legado filtra tarefa não concluída por `IDINSTPRN`, mas escolhe
> `MIN(IDINSTTAR)`; essa regra não é adotada sem validação para o caso de várias
> tarefas. A UI precisa dos identificadores de processo e tarefa.

### T12 — Publicar o Controller SP

**Status:** Parcial — controller existe, mas a última compilação de `:model`
emitiu aviso de que `service-providers.xml` foi criado manualmente. A evidência
anterior de geração automática precisa ser repetida em build limpo antes de
considerar o provider descoberto pelo Om.

**What:** publicar incrementalmente `@Controller(serviceName =
"ApuracaoDashboardSP")` com Advice de erro; expor somente ações já
implementadas e homologadas, começando por `atualizar`.

**Where:** `.../apuracao/controller/`  
**Depends on:** T15
**Requirement:** SP-09, SP-12  
**Tests:** integration/manual  
**Gate:** Full  
**Done when:** cada método publicado mapeia para uma ação homologada, usa DTO,
injeta dependências por construtor, responde envelope estável no Om e o provider
aparece no `service-providers.xml` gerado automaticamente em artefato atual,
sem dependência de arquivo gerado/manual persistente. Operações bloqueadas de
anexo/workflow não são apresentadas como funcionais no MVP.

### T13 — Adicionar testes e inspeção anti-DDL

**What:** completar testes unitários mockados, inspeção do artefato e checklist
de instalação/upgrade sem DDL.

**Where:** `model/src/test/` e `.specs/.../evidencias/`  
**Depends on:** T7, T8, T12  
**Requirement:** SP-01, SP-09, SP-11  
**Tests:** unit + build  
**Gate:** Build  
**Done when:** `:model:test` passa, o artefato não contém DDL das tabelas
existentes e o smoke test registra resultado.

### T14 — Homologar comandos principais com o dashboard

**What:** executar o roteiro manual com o gadget HTML5 para edição, confirmação
e nova auditoria; testar usuários permitidos/negados, concorrência e releitura.
Anexos e workflow têm gate complementar em T10/T11 e não atrasam esta fatia,
salvo se forem pré-requisito do aceite funcional.

**Where:** Om de homologação + `.specs/features/.../evidencias/`  
**Depends on:** T12, T13, T16
**Requirement:** SP-04 a SP-06, SP-09 a SP-12
**Tests:** integration/manual  
**Gate:** Full  
**Done when:** edição, confirmação e nova auditoria têm evidência de
sucesso/recusa e releitura; o usuário aprova o comportamento e há plano de
reversão. O aceite registra que anexos/workflow não bloqueiam o MVP ou os
promove explicitamente para o gate.

- [ ] alinhar prefixo do provider, versão (`adDhalter` → `version`) e chave do
  upload entre gadget e Add-on, além da homologação funcional normal.

### T15 — Implementar adapter seguro para a primeira gravação

**What:** substituir o bloqueio de persistência por um adapter homologado para
`atualizar`, começando por `VALOR`/`DTVENC` e sem criar schema.

**Where:** `model/src/main/java/<package-base>/apuracao/repository/` e
`evidencias/`
**Depends on:** T2, T4, T5, T6, T7
**Requirement:** SP-04, SP-09, SP-10, SP-11
**Tests:** unit + integration/manual
**Gate:** Full
**Status:** Parcial — `BhApuracaoJapeStore` grava `VALOR`/`DTVENC` via JAPE;
token provisório `{valor}|{dtVenc}` documentado em `evidencias/t15-write-adapter.md`;
confirmação/reauditoria seguem fail-closed.

**Done when:**

- [ ] o Om comprova usuário permitido/negado, autorização, estado gravável e
  estratégia de concorrência suportada; `adDhalter` não é assumido como versão
  sem evidência.
- [x] a gravação altera somente os campos permitidos em transação e falha sem
  mutação diante de conflito de versão ou apuração confirmada (código).
- [ ] releitura posterior ao commit homologada no Om; testes de integração.
- [x] evidência local registrada; nenhum DDL ou tabela auxiliar criado.
- [ ] se a política de autorização permanecer fail-closed, homologação não pode
  ser considerada concluída.

### T16 — Habilitar confirmação e nova auditoria como comandos separados

**What:** conectar os casos de uso T8/T9 a adapters transacionais comprovados
e publicar suas ações no Controller somente após homologação, sem fazer o
navegador reproduzir regras de estado ou autorização.

**Where:** `model/src/main/java/<package-base>/apuracao/business/`,
`repository/` e `security/`
**Depends on:** T2, T5, T6, T8, T9, T15
**Requirement:** SP-05, SP-06, SP-09, SP-10, SP-11
**Tests:** unit + integration/manual
**Gate:** Full
**Done when:**

- [ ] confirmação exige valor e estado elegível; replay, conflito e falha não
  duplicam efeitos nem deixam estado parcial.
- [ ] nova auditoria lê `BH_NOVAAUDIT` do usuário corrente e reinicia somente
  os campos observados no legado, preservando valor, vencimento e anexo.
- [ ] cada comando passa por autorização e uma gravação transacional; a UI
  recebe o estado reconsultado após commit.
- [ ] requests/responses e usuários permitidos/negados são homologados no Om.
- [ ] se a semântica de concorrência/idempotência não puder ser sustentada sem
  schema novo, a operação permanece fail-closed até decisão explícita.

## Validação do plano

### Granularidade

Cada tarefa entrega uma configuração, evidência, camada, gateway ou caso de uso
verificável. As integrações de nova auditoria, anexos e workflow foram
separadas porque têm contratos e falhas distintas.

### Cross-check de dependências

| Task | Depends on | Diagrama | Status |
| --- | --- | --- | --- |
| T1 | none | início | concluída — identidade e configuração do Add-on definidas |
| T2 | T1 | T1 → T2 | parcial — permissões externas pendentes |
| T3 | T2 | T2 → T3 | parcial — identidade local concluída; prefixo externo do dashboard difere |
| T4 | T3 | T3 → T4 | parcial — mapeamento nativo de `BH_FACAPU` |
| T5 | T4 | T4 → T5 | parcial — leitura por chave; baseline funcional mapeado, query/paginação/autorização pendentes |
| T6 | T3, T4 | T3/T4 → T6 | concluída — DTOs, validações, envelope seguro e 12 testes unitários |
| T7 | T5, T6 | T6 → T7 | concluída no caso de uso — adapter de escrita/concorrência pendente de homologação |
| T8 | T5, T6 | T6 → T8 | concluída no caso de uso/contrato — adapter de idempotência/concorrência pendente de homologação |
| T9 | T2, T5, T6 | T2/T5/T6 → T9 | parcial — baseline conhecido; permissão está modelada no snapshot e escrita/autorização não homologadas |
| T10 | T2, T5, T6 | T2/T5/T6 → T10 | parcial — tipos/chave legada registrados; chave do gadget é incompatível e gateway permanece bloqueado |
| T11 | T2, T5, T6 | T2/T5/T6 → T11 | bloqueada — regra de tarefa múltipla e leitura/autorização no Om não homologadas |
| T12 | T15 | T15 → T12 | parcial — publicar somente comandos homologados; integrações de anexo/workflow ficam fora do MVP |
| T13 | T7, T8, T12 | T12 → T13 | parcial — compile/test e anti-DDL validados |
| T14 | T12, T13, T16 | T12/T13/T16 → T14 | bloqueada — homologação ponta a ponta dos comandos principais |
| T15 | T2, T4, T5, T6, T7 | T7 → T15 | nova — adapter de escrita deve ser comprovado no Om |
| T16 | T2, T5, T6, T8, T9, T15 | T15 → T16 | nova — confirmar/reauditar ficam atrás de adapters autorizados |

### Test co-location

| Task | Layer | Matrix | Task | Status |
| --- | --- | --- | --- | --- |
| T6 | DTO/validation | unit | unit | ✅ |
| T7 | business | unit | unit | ✅ |
| T8 | business | unit | unit | ✅ |
| T5/T9/T10/T11/T12/T14 | repository/integration/controller | integration/manual | integration/manual | pendente de homologação |
| T1/T3/T4 | config/entity | none/build | none/build | ✅ |
| T13 | tests/config | unit + build | unit + build | ✅ |
