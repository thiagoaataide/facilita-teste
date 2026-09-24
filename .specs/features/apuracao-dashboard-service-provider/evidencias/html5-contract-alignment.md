# Evidência local — divergências do contrato HTML5

**Data:** 2026-09-23  
**Escopo:** inspeção estática somente leitura do gadget novo e do Add-on. Não
houve chamadas a serviços nem gravações no Sankhya Om.

## Payload observado no gadget

Fonte: `C:\projetos\sankhya-html5\facilita\apuracao-faturas\javascript\script.js`.

| Item | Evidência | Valor observado |
| --- | --- | --- |
| Prefixo do provider | `script.js:13` | `facilitatelecom@ApuracaoDashboardSP` |
| Envelope enviado | `script.js:417-419` | `{ request: payload }` |
| Versão em edição/confirmação/anexo | `script.js` (`observedEditionVersion`) | `{valor}|{dtVenc}` quando `adDhalter` ausente |
| Origem do campo no detalhe | JSP + `readDetail` | `AD_DHALTER` removido das queries — coluna ausente em `BH_FACAPU` no Om de teste |
| Pedido de confirmação/nova auditoria | `script.js:521-533` | envia `nuApuracao`, `version` e `idempotencyKey`; não envia `motivo` |
| Chave temporária do upload | `script.js:565` | `APURACAO_DASHBOARD_<NUAPURACAO>_<timestamp>` |
| Reuso da chave na associação | `script.js:577-583` | encaminha a mesma `sessionKey` para `anexar` |

## Comparação com o Add-on

- O Portal confirma um appKey próprio para o Add-on, mas o gadget ainda usa o
  prefixo legado `facilitatelecom@...`; confirmar e alinhar o nome publicado.
- `AnexarBusiness` valida `ANEXO_SISTEMA_bhApuracao_<NUAPURACAO>`, convenção
  observada na tela legada. A chave atual do gadget não passa nessa validação;
  é uma incompatibilidade determinística antes de invocar `AnexoGateway`.
- `ApuracaoSnapshot` define `version`, mas `BhApuracaoReadAdapter.toSnapshot`
  não popula o campo. O gadget envia `adDhalter`; campo e semântica de
  concorrência ainda não foram comprovados no Om.
- O DTO T9 do Add-on aceita `motivo`, mas o fluxo legado e o payload atual do
  gadget não o usam. Não tratá-lo como requisito nem implementar efeitos com
  base nesse campo sem decisão funcional.

Essas diferenças impedem homologação ponta a ponta. Não se deve relaxar a
validação da chave nem tratar `adDhalter` como versão válida por conveniência;
cliente, contrato e adapter devem ser alinhados após confirmação do mecanismo
suportado no ambiente.
