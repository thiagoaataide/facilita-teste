# Service Provider da Apuração de Faturas — Tasks

## Execution Protocol

Executar com a skill `tlc-spec-driven`: ler `STATE.md`, `spec.md` e
`design.md`, realizar uma tarefa atômica por vez, atualizar este arquivo e
registrar a evidência em `memory.md`. Nenhum `deployAddon` é permitido sem
autorização explícita.

**Design:** `design.md`  
**Status:** In Progress — fachada pública e provider HTTP implementados em
modo fail-closed; T7 concluiu a orquestração de atualização e sua fronteira
transacional, mas a gravação real, paginação, anexos, workflow e contrato
externo aguardam homologação no Om.

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
Phase 1 — Fundação e segurança (sequencial)
T1 → T2 → T3

Phase 2 — Contrato e modelo (sequencial)
T3 → T4 → T5 → T6

Phase 3 — Casos de uso e integrações
T6 ──┬→ T7 ────────────────┐
     └→ T8 ────────────────┤
T2,T5,T6 → T9 → T10 → T11 ─┤
                            ↓
                           T12

Phase 4 — Homologação e publicação (sequencial)
T12 → T13 → T14
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

### T9 — Implementar nova auditoria

**What:** criar o caso de uso transacional de `solicitarNovaAuditoria`, usando
somente campos e permissão comprovados.

**Where:** `.../apuracao/business/SolicitarNovaAuditoriaBusiness.java`  
**Depends on:** T2, T5, T6  
**Requirement:** SP-06  
**Tests:** integration/manual  
**Gate:** Full
**Done when:** a regra `BH_NOVAAUDIT`, campos reiniciados, autorização e
transação têm evidência no Om; sem evidência, o comando permanece bloqueado.

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

> A porta e os casos de uso foram criados; o gateway concreto permanece
> bloqueado porque `TSIANX` retornou `Não autorizado` no ambiente de teste.

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

### T12 — Publicar o Controller SP

**What:** implementar `@Controller(serviceName = "ApuracaoDashboardSP")` com
as ações do contrato e Advice de erro.

**Where:** `.../apuracao/controller/`  
**Depends on:** T7, T8, T9, T10, T11  
**Requirement:** SP-09, SP-12  
**Tests:** integration/manual  
**Gate:** Full  
**Done when:** cada método público mapeia para uma ação documentada, usa DTO,
injeta dependências por construtor, responde envelope estável no Om e o provider
aparece no `service-providers.xml` gerado.

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

### T14 — Homologar integração com o dashboard

**What:** executar o roteiro manual com o gadget HTML5, permissões permitidas/
negadas, concorrência, anexos e workflow; atualizar contrato e memory.

**Where:** Om de homologação + `.specs/features/.../evidencias/`  
**Depends on:** T12, T13  
**Requirement:** SP-02 a SP-12  
**Tests:** integration/manual  
**Gate:** Full  
**Done when:** critérios de aceite da spec estão marcados com evidência, o
usuário aprova o comportamento e há plano de reversão; só então considerar
publicação.

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
| T3 | T2 | T2 → T3 | parcial — identidade local concluída; contrato externo e dashboard pendentes |
| T4 | T3 | T3 → T4 | parcial — mapeamento nativo de `BH_FACAPU` |
| T5 | T4 | T4 → T5 | parcial — leitura por chave; filtros/paginação pendentes |
| T6 | T3, T4 | T3/T4 → T6 | concluída — DTOs, validações, envelope seguro e 12 testes unitários |
| T7 | T5, T6 | T6 → T7 | concluída no caso de uso — adapter de escrita/concorrência pendente de homologação |
| T8 | T5, T6 | T6 → T8 | bloqueada — store/concorrência pendentes |
| T9 | T2, T5, T6 | T2/T5/T6 → T9 | bloqueada — regra `BH_NOVAAUDIT` não comprovada |
| T10 | T2, T5, T6 | T2/T5/T6 → T10 | bloqueada — serviço de anexos não autorizado |
| T11 | T2, T5, T6 | T2/T5/T6 → T11 | bloqueada — workflow não autorizado |
| T12 | T7, T8, T9, T10, T11 | T7/T8/T9/T10/T11 → T12 | bloqueada — dependências não ligadas |
| T13 | T7, T8, T12 | T12 → T13 | parcial — compile/test e anti-DDL validados |
| T14 | T12, T13 | T12/T13 → T14 | bloqueada — requer endpoint e homologação |

### Test co-location

| Task | Layer | Matrix | Task | Status |
| --- | --- | --- | --- | --- |
| T6 | DTO/validation | unit | unit | ✅ |
| T7 | business | unit | unit | ✅ |
| T8 | business | unit | unit | ✅ |
| T5/T9/T10/T11/T12/T14 | repository/integration/controller | integration/manual | integration/manual | ✅ |
| T1/T3/T4 | config/entity | none/build | none/build | ✅ |
| T13 | tests/config | unit + build | unit + build | ✅ |
