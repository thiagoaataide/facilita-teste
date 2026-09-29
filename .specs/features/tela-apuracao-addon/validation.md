# Tela de Apuração no add-on Validation

**Date**: 2026-09-29
**Spec**: `.specs/features/tela-apuracao-addon/spec.md`
**Design**: `.specs/features/tela-apuracao-addon/design.md`
**Diff range**: `a47e921..1eaaeb7` (inclusive)
**Verifier**: independent sub-agent (author ≠ verifier)
**Scope**: P1 only (TELA-01 through TELA-08). TELA-09 onward were not judged.

Commits in range:

| SHA | Subject |
| --- | --- |
| `a47e921` | feat(auth): libera leitura da apuracao para o usuario da sessao |
| `afd1e12` | feat(apuracao): lista o mes corrente so com pendentes |
| `b0e9417` | feat(tela): abre a grade, o detalhe e os anexos pelo menu |
| `1eaaeb7` | feat(anexo): inclui a listagem de TSIANX ja comprovada no Om |

---

## Task Completion

| Task | Status | Notes |
| --- | --- | --- |
| T1 Liberar leitura | ✅ Done | `LIST` / `DETAIL` / `LIST_ATTACHMENTS` return; `CONFIRM` stays `FORBIDDEN` |
| T2 Consultar a grade do mês | ✅ Done | Empty page and the seven grade fields are asserted |
| T3 Publicar a tela no menu | ✅ Done | Menu points at `ApuracaoTrabalho.xhtml5`. No automated HTML5 test |
| T4 Ligar a grade ao `listar` | ✅ Done | Payload sends current `YYYY-MM` and `somentePendentes: true` |
| T5 Abrir o detalhe | ✅ Done | `listarDetalhe` sends `item.nuApuracao` |
| T6 Ver anexos | ✅ Done | Screen renders `identifier` and `name`. Facade listing added in `1eaaeb7` |

No task in this list is blocked or partial. TELA-09 through TELA-14 stay Pending and are out of scope.

---

## Spec-Anchored Acceptance Criteria

HTML5 has no automated test (coverage matrix: evidence on Om after install). Those criteria are judged from source `file:line`. **Om evidence is not yet collected.** That is not scored as a failed unit test.

| Criterion (WHEN X THEN Y) | Spec-defined outcome | `file:line` + assertion | Result |
| --- | --- | --- | --- |
| WHEN o usuário abre o menu **Apuração de Faturas Addon** THEN a tela SHALL carregar no HTML5 do add-on, com `ServiceProxy` disponível. | Menu title opens the add-on HTML5 page and `ServiceProxy` is injected | `datadictionary/MENU_FACAPU.xml:4` menu description; `datadictionary/MENU_FACAPU.xml:5` `url="/$ctx/ApuracaoTrabalho.xhtml5"`; `vc/src/main/webapp/html5/ApuracaoTrabalho/ApuracaoTrabalho.js:2` `ServiceProxy` injected; `ApuracaoTrabalho.js:5` prefix `facilita-apuracao-fatura-addon@ApuracaoDashboardSP`; `launcher/ApuracaoTrabalho.body:3` loads the script. No unit test. | ✅ Source matches; Om evidence not yet collected |
| WHEN a grade pede a lista THEN a tela SHALL chamar `listar` e SHALL mostrar sequência, conta, contrato, vencimento, valor, confirmado e se possui anexo. | Those seven columns, from `listar` | `BhApuracaoReadAdapterTest.java:46` `assertEquals(Integer.valueOf(185045240), item.getNuApuracao())`; `:47` `assertEquals("10", item.getCodConta())`; `:48` `assertEquals("20", item.getNumContrato())`; `:49` `assertEquals("2026-09-15", item.getDtVenc())`; `:50` `assertEquals(new BigDecimal("12.50"), item.getValor())`; `:51` `assertEquals("N", item.getConfirmado())`; `:52` `assertEquals("S", item.getPossuiAnexo())`. Screen: `ApuracaoTrabalho.js:16` calls `listar`; `ApuracaoTrabalho.html:8-14` column headers; `:19-25` bindings. | ✅ Fields asserted; screen Om evidence not yet collected |
| WHEN `listar` ou `listarDetalhe` é chamado por um usuário da sessão THEN a fachada SHALL responder sem `FORBIDDEN`. | Session user is not `FORBIDDEN` on `LIST` and `DETAIL`. Other actions stay `FORBIDDEN`. | `FailClosedAuthorizationPortTest.java:24-27` `requireAllowed(LIST, AuthorizationContext("10"))` returns; `:30-33` same for `DETAIL`; `:45` `assertEquals(ErrorCode.FORBIDDEN, exception.getCode())` for `CONFIRM`. Gate: `FailClosedAuthorizationPort.java:24-27`. Blank user is still `FORBIDDEN` in `ListarApuracoesBusiness.java:91-95` and `ListarDetalheBusiness.java:56-59` before the port. | ✅ PASS |
| WHEN não há apurações no filtro THEN a grade SHALL ficar vazia, com `ok: true`. | Empty grade **and** envelope `ok: true` | `BhApuracaoReadAdapterTest.java:33` `assertTrue(page.getItems().isEmpty())`; `:34` `assertEquals(0L, page.getTotal())`. The test does not assert `ok`. `ListarApuracoesBusiness.java:65` `return ApiResponse.success(...)` sets `ok` true at `ApiResponse.java:23`, and that line is not an assertion. | ❌ GAP — `ok: true` has no assertion |
| WHEN o usuário seleciona uma linha THEN a tela SHALL chamar `listarDetalhe` com o `NUAPURACAO` daquela linha. | `listarDetalhe` body `nuApuracao` is the row's `nuApuracao` | `ApuracaoTrabalho.js:39-40` `chamar("listarDetalhe", { request: { nuApuracao: item.nuApuracao } })`. No unit test. | ✅ Source matches; Om evidence not yet collected |
| WHEN o usuário pede os anexos da linha `185045240` THEN a fachada SHALL devolver o arquivo `199605` com o nome gravado em `TSIANX`. | Identifier `199605` and the `TSIANX` file name for apuração `185045240` | Mechanism only: `AnexoSistemaListMapper.java:16-17` key `{nu}_bhApuracao`; `AnexoSistemaRepository.java:14-16` selects `NUATTACH`, `NOMEARQUIVO`; `BlockedAnexoGateway.java:68-71` copies them; `AnexoSistemaListMapperTest.java:29-30` `assertEquals("9", identifier)` and `assertEquals("fatura.pdf", name)` — fixture is not `199605`. `AnexoSistemaListMapperTest.java:17` uses `185047303`, not `185045240`. | Om evidence not yet collected (not a failed unit test) |
| WHEN a linha não tem anexo THEN `listarAnexos` SHALL devolver `files` vazio e `ok: true`. | `files` empty **and** envelope `ok: true` | `AnexoSistemaListMapperTest.java:36` `assertEquals(0, response.getFiles().size())`. `ListarAnexosResponse` has no `ok`. `ListarAnexosBusiness.java:45` `return ApiResponse.success(...)` is not an assertion. Screen treats empty `files` as success only after `envelope.ok === true` (`ApuracaoTrabalho.js:65-68`, `ApuracaoTrabalho.html:35`). | ❌ GAP — `ok: true` has no assertion |
| WHEN a fachada recusa THEN a tela SHALL mostrar `code`, `message` e `correlationId` do envelope. | Those three envelope fields, no stack | `ApuracaoTrabalho.js:76-80` sets `code`, `message`, and `correlationId` from `envelope.error` and `envelope.correlationId`. No unit test. | ✅ Source matches; Om evidence not yet collected |

**Status**: ❌ Gaps present (AC4 and AC7). Om runtime evidence for the HTML5 criteria and for the pair `185045240` / `199605` is not yet collected.

---

## Discrimination Sensor

Mutations were applied to temporary copies of the committed sources, tests were run, then the originals were copied back. `git diff` on tracked files was empty afterwards. The pre-existing untracked `vc/src/main/webapp/html5/ApuracaoChamada/` was not touched.

| Mutation | File:line | Description | Killed? |
| --- | --- | --- | --- |
| 1 | `FailClosedAuthorizationPort.java:24` | Removed `LIST` from the allow list | ✅ Killed (`permiteListarAGradeDoUsuarioDaSessao`, exit 1) |
| 2 | `FailClosedAuthorizationPort.java:25` | Removed `DETAIL` from the allow list | ✅ Killed (`permiteDetalheDoUsuarioDaSessao`, exit 1) |
| 3 | `FailClosedAuthorizationPort.java:26-29` | `CONFIRM` returned instead of `FORBIDDEN` | ✅ Killed (`mantemAsDemaisAcoesFechadas`, exit 1) |
| 4 | `BhApuracaoReadAdapter.java:121` | `possuiAnexo` hardcoded to `"N"` | ✅ Killed (`devolveSequenciaContaContratoVencimentoValorConfirmadoEAnexo`, exit 1) |
| 5 | `BhApuracaoReadAdapter.java:52-55` | Empty month page throws `INTEGRATION` | ✅ Killed (`devolvePaginaVaziaQuandoOMesNaoTemApuracao`, exit 1) |
| 6 | `AnexoSistemaListMapper.java:36-37` | Empty `files` gained a ghost entry | ✅ Killed (`listaVaziaQuandoNaoHaLinhas`, exit 1) |

**Sensor depth**: manual full pass on the auth gate (≥5), plus the grade and empty-attachment branches
**Result**: 6/6 killed — PASS ✅

The sensor does not cover `ok: true`. No test asserts that flag, so a mutant that wrapped an empty success as `ok: false` would not be in this set.

---

## Interactive UAT Results (if performed)

Feito no Om de produção da Facilita em 2026-09-29, versões 1.0.5 e 1.0.6, com o usuário da sessão.

| # | Test | Result | Details |
| --- | --- | --- | --- |
| 1 | Abrir **Apuração de Faturas Addon** pelo menu | ✅ Pass | A tela abriu e a grade listou o mês |
| 2 | Selecionar uma linha e ver o detalhe (TELA-05) | ✅ Pass | `185047620`, `185045004` e `185044927` devolveram o detalhe de `listarDetalhe` |
| 3 | Linha sem anexo (TELA-07) | ✅ Pass | `185047620`, `possuiAnexo: N`, mostrou "Nenhum anexo" |
| 4 | Linha confirmada com anexo, com filtro sem pendentes (TELA-06, TELA-15) | ✅ Pass | `185045004` mostrou `200981`; `185044927` mostrou `201757 ... FO.pdf` |
| 5 | Anexo `199605` da `185045240` | ✅ Pass | Comprovado antes pela mesma chamada `listarAnexos`; a linha é de setembro de 2026 e está confirmada (`CONFIRMADO = S`) |
| 6 | Grade vazia na tela (TELA-04) | ⏭️ Não visto no Om | Coberto pelo teste `BhApuracaoReadAdapterTest` |
| 7 | Erro no formato `code` / `message` / `correlationId` (TELA-08) | ⏭️ Não visto depois da correção | A 1.0.5 passou a ler `responseBody.error`; o erro anterior foi o que expôs a falha |

---

## Code Quality

| Principle | Status |
| --- | --- |
| Minimum code | ✅ TSIANX listing is the read path for TELA-06/07. `anexar` still throws `INTEGRATION`. |
| Surgical changes | ✅ Diff stays on auth, grade read, menu, screen, and anexo listing. `build.gradle` only bumps `version` `1.0.1` → `1.0.4`. |
| No scope creep | ✅ P2/P3 actions stay closed (`CONFIRM` test; `BlockedAnexoGateway.anexar`). |
| Matches patterns | ✅ Java 8, Guice `@Inject`, `@JapeEntity` / `@Repository`, existing envelope types. |
| Spec-anchored outcome check (asserted values match spec) | ❌ AC4 and AC7 assert the empty collection and do not assert `ok: true`. |
| Per-layer Coverage Expectation met (domain 1:1 ACs; routes happy+edge+error) | ❌ Envelope `ok: true` is missing. HTML5 expectation is Om evidence, which is not yet collected. |
| Every test maps to a spec requirement — no unclaimed tests | ✅ Auth tests → TELA-03 / T1; grade tests → TELA-02 / TELA-04 / T2 done-when (invalid month, no `SELECT *`, named parameters at `BhApuracaoReadAdapterTest.java:56-75`); mapper tests → TELA-06/07 mechanism. |
| Documented guidelines followed: `AGENTS.md`, `.cursorrules` section 8. `coding-principles.md` is not in the skill package. | ✅ |

---

## Edge Cases

- [x] `nuApuracao` not a positive integer: `ApuracaoIdRequest.java:6-9` `@NotNull` `@Min(1)`; `ListarDetalheBusiness.java:41-44` and `ListarAnexosBusiness.java:48-53` throw `VALIDATION` before the query. Asserted by pre-existing `ApiContractValidationTest.java:30-36` and `:49-52` (those `@Test` counts did not drop).
- [ ] `nuApuracao` greater than `2147483647`: the field is `Integer` (`ApuracaoIdRequest.java:10`). There is no `VALIDATION` branch for a value above `Integer.MAX_VALUE`. A binder failure falls through `ApuracaoControllerAdvice.java:42-49` as `INTERNAL`. This code was not changed in `a47e921..1eaaeb7`.
- [x] Session without a user: `ListarApuracoesBusiness.java:91-95`, `ListarDetalheBusiness.java:56-59`, and `ListarAnexosBusiness.java:56-59` throw `FORBIDDEN` when `userId` is blank. `OmAuthorizationContextResolver.java:29` returns a null user id when the Om session is missing. The new port does not re-check the user; the business check still runs first.
- [x] Attachment query failure: `BlockedAnexoGateway.java:49-52` logs the apuração key and throws `INTEGRATION` with `"A consulta de anexos falhou."` (no stack in the payload). No unit test covers this branch.

---

## Gate Check

- **Gate command**: `$env:JAVA_HOME = "C:\Program Files\Java\jdk-21.0.12"; .\gradlew.bat :model:test --offline`
- **Result**: exit 0. `:model:test` was UP-TO-DATE. Report `model/buildGradle/reports/tests/test/index.html`: 44 tests, 0 failures, 0 ignored.
- **Test count before feature** (`f844aa0`, parent of `a47e921`): 34 `@Test` methods (12 + 12 + 8 + 2).
- **Test count after feature** (`1eaaeb7`): 44 `@Test` methods. Report agrees: 44 tests.
- **Delta**: +10 (4 auth, 3 grade, 3 anexo mapper).
- **Skipped tests**: none.
- **Failures**: none.
- **Integrity**: existing classes kept the same `@Test` counts. No assertion in those classes was weakened by this range.

---

## Fix Plans (if issues found)

### Fix 1: Assert `ok: true` for an empty `listar`

- **Root cause**: AC4's precise envelope flag is never asserted. `BhApuracaoReadAdapterTest` stops at an empty `ApuracaoPage`.
- **Fix task**: Add a unit test on `ListarApuracoesBusiness` with a fake `ApuracaoQuery` that returns an empty page and a session `AuthorizationContext`. Assert `response.isOk()` is true and `data.items` is empty.
- **Where**: `model/src/test/java/br/com/facilita/apuracao/business/` (new test or an existing listar test). Do not change production code unless the assertion fails.
- **Verify**: the new test fails if `execute` returns a failure envelope for an empty page; `:model:test` passes after the assertion exists.
- **Done when**: AC4 cites `assertTrue(response.isOk())` (or equivalent) next to an empty item list.
- **Priority**: Major

### Fix 2: Assert `ok: true` for `listarAnexos` with no files

- **Root cause**: AC7's `files` size is asserted on `AnexoSistemaListMapper`. The envelope `ok` flag lives on `ApiResponse` and has no test.
- **Fix task**: Add a unit test on `ListarAnexosBusiness` whose gateway returns `files` empty. Assert `response.isOk()` is true and `data.files` is empty.
- **Where**: `model/src/test/java/br/com/facilita/apuracao/business/`.
- **Verify**: the test fails if an empty file list is turned into an error envelope.
- **Done when**: AC7 cites that assertion.
- **Priority**: Major

### Fix 3: Reject `nuApuracao` above `2147483647` as `VALIDATION`

- **Root cause**: pre-existing `Integer` id. Overflow never becomes `VALIDATION`. Not introduced by this commit range.
- **Fix task**: Only if product still wants this edge inside P1: reject the out-of-range id as `VALIDATION` before any repository call, and assert the code plus that the query port was not called.
- **Where**: `ApuracaoIdRequest` / the id-based business guards, plus `ApiContractValidationTest` or the business test.
- **Verify**: a value above `2147483647` yields `VALIDATION` and does not call the query.
- **Done when**: the edge checklist item is true with a `file:line` assertion.
- **Priority**: Minor (pre-existing; outside `a47e921..1eaaeb7`)

Om collection (menu, grade, detail, error text, file `199605`) is not a fix task. It is evidence still to be gathered on the installed package.

---

## Requirement Traceability Update

Recommended statuses. `spec.md` was not edited.

| Requirement | Previous Status | New Status |
| --- | --- | --- |
| TELA-01 | Done | Source verified; Om evidence not yet collected |
| TELA-02 | Done | ✅ Verified in the grade adapter; screen Om evidence not yet collected |
| TELA-03 | Done | ✅ Verified |
| TELA-04 | Done | ❌ Needs Fix — assert `ok: true` |
| TELA-05 | Done | Source verified; Om evidence not yet collected |
| TELA-06 | Done | Mapper verified; Om pair `185045240` / `199605` not yet collected |
| TELA-07 | Done | ❌ Needs Fix — assert `ok: true` |
| TELA-08 | Done | Source verified; Om evidence not yet collected |
| TELA-09 .. TELA-14 | Pending | Out of scope |

---

## Summary

**Overall**: ❌ Not Ready

**Spec-anchored check**: 6/8 P1 criteria match the spec in source or in a test assertion. 2 gaps (AC4, AC7) where `ok: true` is not asserted. 0 spec-precision gaps. Om evidence is not yet collected for the HTML5 criteria and for attachment `199605`.

**Sensor**: 6/6 mutations killed

**Gate**: 44 passed, 0 failed, 0 skipped

**What works**: Session user can pass `LIST` and `DETAIL` without `FORBIDDEN`, and `CONFIRM` stays closed. The month grade maps sequence, account, contract, due date, value, confirmed, and attachment flag, and an empty month returns an empty page instead of `INTEGRATION`. The menu opens `ApuracaoTrabalho`, which calls `listar`, `listarDetalhe`, and `listarAnexos` on `facilita-apuracao-fatura-addon@ApuracaoDashboardSP` and renders `code`, `message`, and `correlationId` from a refused envelope. Attachment rows map `NUATTACH` and `NOMEARQUIVO`; an empty row list stays an empty `files` collection.

**Issues found**: AC4 and AC7 never assert envelope `ok: true`. The overflow edge for `nuApuracao` is still `INTERNAL` on binder failure, and it predates this range.

**Next steps**: Add the two business-level assertions (Fix 1 and Fix 2), then re-verify. Collect Om evidence separately; do not treat its absence as a unit-test failure.
