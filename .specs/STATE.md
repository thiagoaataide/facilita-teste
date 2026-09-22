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
- **Phase / Task**: Tasks / T1-T3 — identidade do Add-on esclarecida
- **Completed**: configuração segura, appKey oficial confirmado no Portal,
  fachada `ApuracaoDashboardSP` e artefato `.exts` gerado
- **In-progress**: `.specs/features/apuracao-dashboard-service-provider/tasks.md`
  — atualizar evidências do parceiro, package-base e contrato externo
- **Next step**: confirmar parceiro/package-base e nome externo do Service
  Provider; depois retomar a homologação de filtros, concorrência, anexos e
  workflow no Om
- **Blockers**: `TSIANX` e `TWFITAR` retornaram `Não autorizado`; contrato de
  listagem e campo de concorrência de `BH_FACAPU` ainda não comprovados
- **Uncommitted files**: `tasks.md`, `memory.md`, `STATE.md`
- **Branch**: `main`
