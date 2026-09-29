# Tela de Apuração no add-on — Tasks

## Execution Protocol (MANDATORY -- do not skip)

Implement these tasks with the `tlc-spec-driven` skill: **activate it by name and follow its Execute flow and Critical Rules.** Do not search for skill files by filesystem path. The skill is the source of truth for the full flow (per-task cycle, sub-agent delegation, adequacy review, Verifier, discrimination sensor).

**If the skill cannot be activated, STOP and tell the user — do not proceed without it.**

---

**Design**: `.specs/features/tela-apuracao-addon/design.md`
**Status**: Done

Escopo desta lista: F1–F3, requisitos TELA-01 a TELA-08. F4–F6 não têm tarefa aqui.

---

## Test Coverage Matrix

> Generated from codebase, project guidelines, and spec — confirm before Execute. Guidelines found: `AGENTS.md` (JUnit na camada tocada ou evidência no Om), `.cursorrules` seção 8, `model/build.gradle` (`useJUnitPlatform`).

| Code Layer | Required Test Type | Coverage Expectation | Location Pattern | Run Command |
| --- | --- | --- | --- | --- |
| Business / autorização | unit | Cada ação aberta ou fechada da spec; grade vazia e grade com linha | `model/src/test/java/br/com/facilita/apuracao/**/*Test.java` | `.\gradlew.bat :model:test` com `JAVA_HOME` no JDK 21 |
| Repository / SQL | none | A query allowlisted entra no adapter; o comportamento da página é testado no business com `ApuracaoQuery` falso | — | build gate |
| HTML5 do add-on | none | Evidência no Om depois do pacote instalado | `vc/src/main/webapp/html5/ApuracaoTrabalho/` | build gate |
| Entity | none | build gate | — | build gate |

## Parallelism Assessment

> Generated from codebase — confirm before Execute.

| Test Type | Parallel-Safe? | Isolation Model | Evidence |
| --- | --- | --- | --- |
| unit | Yes | Fakes locais, sem banco | `AtualizarApuracaoBusinessTest` instancia fakes em `@BeforeEach` |
| none | Yes | Sem teste compartilhado | HTML e entidade não rodam suíte |

## Gate Check Commands

> Generated from codebase — confirm before Execute.

| Gate Level | When to Use | Command |
| --- | --- | --- |
| Quick | Tarefa com teste de unidade | `$env:JAVA_HOME = "C:\Program Files\Java\jdk-21.0.12"; .\gradlew.bat :model:test` |
| Full | Igual ao quick; este módulo não tem e2e | o mesmo comando |
| Build | Tarefa sem teste de Java, ou fim de fase | o mesmo comando; a suíte existente não pode perder teste |

O JRE em `JAVA_HOME` quebra o Kotlin do plugin. O comando acima usa o JDK 21 desta máquina.

---

## Execution Plan

### Phase 1

```
T1
T3
```

T1 e T3 não dependem um do outro.

### Phase 2

```
T1 → T2
```

### Phase 3

```
T2 → T4
T3 → T4
```

### Phase 4

```
T4 → T5 → T6
```

---

## Task Breakdown

### T1: Liberar leitura na autorização

**What**: `LIST` e `DETAIL` passam para o usuário da sessão, como `LIST_ATTACHMENTS`. As outras ações continuam `FORBIDDEN`.
**Where**: `model/src/main/java/br/com/facilita/apuracao/security/FailClosedAuthorizationPort.java`
**Depends on**: None
**Reuses**: `FailClosedAuthorizationPortTest`
**Requirement**: TELA-03

**Tools**:

- MCP: NONE
- Skill: `test`

**Done when**:

- [x] `LIST`, `DETAIL` e `LIST_ATTACHMENTS` não lançam
- [x] `CONFIRM` continua `FORBIDDEN`
- [x] Gate: `.\gradlew.bat :model:test` com JDK 21
- [x] Test count: a suíte passa e os testes novos desta classe permanecem

**Tests**: unit
**Gate**: quick

**Commit**: `feat(auth): libera leitura da apuracao para o usuario da sessao`

---

### T2: Consultar a grade do mês

**What**: `find` deixa de lançar integração e devolve a página de `BH_FACAPU` do mês corrente, só pendentes, com o indicador de anexo vindo de `TSIANX`.
**Where**: `model/src/main/java/br/com/facilita/apuracao/repository/BhApuracaoReadAdapter.java`
**Depends on**: T1
**Reuses**: `BhApuracaoRepository`, filtro de `dados.jsp`, `ApuracaoFilter`
**Requirement**: TELA-02, TELA-04

**Tools**:

- MCP: NONE
- Skill: `repository`, `test`

**Done when**:

- [x] Página vazia devolve lista vazia, não `INTEGRATION`
- [x] Uma linha devolve sequência, conta, contrato, vencimento, valor, confirmado e possui anexo
- [x] A query não usa `SELECT *` e não concatena o texto do filtro
- [x] Gate: `.\gradlew.bat :model:test` com JDK 21
- [x] Test count: a suíte passa, com teste novo do caso vazio e do caso com linha

**Tests**: unit
**Gate**: quick

**Commit**: `feat(apuracao): lista o mes corrente so com pendentes`

---

### T3: Publicar a tela no menu [P]

**What**: Criar `ApuracaoTrabalho` no formato do Martins e apontar o menu `FACAPU` para ela. Erro da fachada mostra `code`, `message` e `correlationId`.
**Where**: `vc/src/main/webapp/html5/ApuracaoTrabalho/`, `datadictionary/MENU_FACAPU.xml`
**Depends on**: None
**Reuses**: `ApuracaoChamada`, `Martins.js`
**Requirement**: TELA-01, TELA-08

**Tools**:

- MCP: NONE
- Skill: NONE

**Done when**:

- [x] O item do menu abre `ApuracaoTrabalho.xhtml5`
- [x] **Chamada da fachada** não permanece no menu
- [x] A tela usa `ServiceProxy.callService` com `facilita-apuracao-fatura-addon@ApuracaoDashboardSP`
- [x] Gate: `.\gradlew.bat :model:test` com JDK 21 continua passando

**Tests**: none
**Gate**: build

**Commit**: `feat(tela): abre a apuracao pelo menu do add-on`

---

### T4: Ligar a grade ao listar

**What**: Ao abrir, a tela chama `listar` com o mês corrente e somente pendentes, e desenha as colunas da spec.
**Where**: `vc/src/main/webapp/html5/ApuracaoTrabalho/ApuracaoTrabalho.js`
**Depends on**: T2, T3
**Reuses**: chamada já usada em `ApuracaoChamada.js`
**Requirement**: TELA-02, TELA-04

**Tools**:

- MCP: NONE
- Skill: NONE

**Done when**:

- [x] O payload de `listar` manda `somentePendentes: true` e o mês `YYYY-MM`
- [x] Lista vazia não mostra linha
- [x] Gate: `.\gradlew.bat :model:test` com JDK 21 continua passando

**Tests**: none
**Gate**: build

**Commit**: `feat(tela): mostra a grade do mes na abertura`

---

### T5: Abrir o detalhe da linha

**What**: Selecionar uma linha chama `listarDetalhe` com o `NUAPURACAO` da linha.
**Where**: `vc/src/main/webapp/html5/ApuracaoTrabalho/ApuracaoTrabalho.js`
**Depends on**: T4
**Reuses**: `ListarDetalheBusiness`
**Requirement**: TELA-05

**Tools**:

- MCP: NONE
- Skill: NONE

**Done when**:

- [x] O `nuApuracao` enviado é o da linha, não outro campo
- [x] O detalhe exibido é o `data` do envelope
- [x] Gate: `.\gradlew.bat :model:test` com JDK 21 continua passando

**Tests**: none
**Gate**: build

**Commit**: `feat(tela): abre o detalhe da apuracao selecionada`

---

### T6: Ver anexos da linha

**What**: A linha selecionada chama `listarAnexos` e mostra identificador e nome. Lista vazia fica vazia, com `ok: true`.
**Where**: `vc/src/main/webapp/html5/ApuracaoTrabalho/ApuracaoTrabalho.js`
**Depends on**: T5
**Reuses**: `BlockedAnexoGateway` já comprovado com `185045240` / `199605`
**Requirement**: TELA-06, TELA-07

**Tools**:

- MCP: NONE
- Skill: NONE

**Done when**:

- [x] A chamada é `...ApuracaoDashboardSP.listarAnexos`
- [x] A tela mostra `files[].identifier` e `files[].name`
- [x] `files` vazio não vira erro
- [x] Gate: `.\gradlew.bat :model:test` com JDK 21 continua passando

**Tests**: none
**Gate**: build

**Commit**: `feat(tela): lista os anexos da apuracao selecionada`

---

## Parallel Execution Map

```
Phase 1:
  T1 [P]
  T3 [P]

Phase 2:
  T1 → T2

Phase 3:
  T2 → T4
  T3 → T4

Phase 4:
  T4 → T5 → T6
```

## Task Granularity Check

| Task | Scope | Status |
| --- | --- | --- |
| T1: Liberar leitura | 1 classe + teste | ✅ Granular |
| T2: Consultar a grade | 1 leitura + teste | ✅ Granular |
| T3: Publicar a tela | 1 tela + 1 item de menu | ✅ Granular |
| T4: Ligar a grade | 1 função de abertura | ✅ Granular |
| T5: Abrir o detalhe | 1 função | ✅ Granular |
| T6: Ver anexos | 1 função | ✅ Granular |

## Diagram-Definition Cross-Check

| Task | Depends On (task body) | Diagram Shows | Status |
| --- | --- | --- | --- |
| T1 | None | Phase 1, sem seta de entrada | ✅ Match |
| T2 | T1 | T1 → T2 | ✅ Match |
| T3 | None | Phase 1, sem seta de entrada | ✅ Match |
| T4 | T2, T3 | T2 → T4 e T3 → T4 | ✅ Match |
| T5 | T4 | T4 → T5 | ✅ Match |
| T6 | T5 | T5 → T6 | ✅ Match |

## Test Co-location Validation

| Task | Code Layer Created/Modified | Matrix Requires | Task Says | Status |
| --- | --- | --- | --- | --- |
| T1 | Business / autorização | unit | unit | ✅ OK |
| T2 | Business / autorização | unit | unit | ✅ OK |
| T3 | HTML5 do add-on | none | none | ✅ OK |
| T4 | HTML5 do add-on | none | none | ✅ OK |
| T5 | HTML5 do add-on | none | none | ✅ OK |
| T6 | HTML5 do add-on | none | none | ✅ OK |
