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

## Handoff

- **Feature**: `.specs/features/apuracao-dashboard-service-provider/`
- **Phase / Task**: Tasks / T8 — confirmação idempotente e releitura pós-commit concluídas no caso de uso
- **Completed**: configuração segura, appKey oficial confirmado no Portal,
  fachada `ApuracaoDashboardSP`, artefato `.exts` gerado e contrato seguro de
  requests/erros validado; T7 e T8 adicionaram 20 testes de caso de uso (32
  testes no gate do módulo)
- **In-progress**: `.specs/features/apuracao-dashboard-service-provider/tasks.md`
  — T7/T8 concluídas no caso de uso; mutações persistentes seguem fail-closed
- **Next step**: homologar a regra `BH_NOVAAUDIT` para desbloquear T9 e seguir
  com o contrato externo, filtros, versão/concorrência, anexos e workflow
- **Blockers**: `TSIANX` e `TWFITAR` retornaram `Não autorizado`; contrato de
  listagem, regra `BH_NOVAAUDIT`, campo de concorrência/gravação condicional de
  `BH_FACAPU`, idempotência atômica e formato exato de `DTVENC` ainda não
  comprovados
- **Uncommitted files**: none
- **Branch**: `main`
