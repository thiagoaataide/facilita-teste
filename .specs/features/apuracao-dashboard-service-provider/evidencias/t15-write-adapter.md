# Evidência — T15 adapter de gravação (`atualizar`)

**Data:** 2026-09-23  
**Ambiente:** implementação local + consulta somente leitura no Om (Facilita Telecom / teste)

## Decisão de concorrência provisória

- `ALL_TAB_COLUMNS` em `BH_FACAPU` **não** lista `AD_DHALTER` nem `DHALTER` (29 colunas
  capturadas em 2026-09-23).
- O gadget referenciava `APU.AD_DHALTER` nas JSPs; a coluna foi removida da projeção para
  não quebrar a consulta read-first (HTML5 T22).
- Até homologar um campo oficial de versão, o token observado é
  `{valorPlain}|{dtVencIso}` (`BhApuracaoObservedVersion`), espelhado no JavaScript
  (`observedEditionVersion` em `script.js`).

## Implementação no add-on

- `BhApuracaoJapeStore` implementa `ApuracaoStore` com:
  - `findById` e `updateEditableFields` via `BhApuracaoRepository.save`;
  - gravação restrita a `VALOR`/`DTVENC` e bloqueio se `CONFIRMADO = S`;
  - `confirm` e `requestNewAudit` permanecem fail-closed (T16).
- `BlockedApuracaoStore` removido; leitura por chave deixa de falhar no store.
- `BhApuracaoReadAdapter` reutiliza `BhApuracaoSnapshotMapper` e popula `version`.

## Pendências de homologação (Om)

- [ ] Usuário permitido/negado com `FailClosedAuthorizationPort` substituído por política real.
- [ ] Prova de gravação transacional e releitura após commit no ambiente do cliente.
- [ ] Confirmar se o token `{valor}|{dtVenc}` é aceitável ou substituir por campo comprovado.
- [ ] Testes de conflito com duas sessões editando a mesma apuração.
