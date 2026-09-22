# ADR-001 — Add-on isolado para a fachada transacional

**Status:** Aceito  
**Data:** 2026-09-21  
**Escopo:** `facilita-apuracao-fatura-addon`

## Contexto

O dashboard HTML5 precisa substituir uma tela legada cuja grade depende de uma
função JavaScript incompatível. A tela também possui mutações, anexos e
workflow. O fonte legado não é comprovadamente o artefato instalado e não pode
ser publicado como base de correção.

As tabelas de negócio já existem no ambiente do cliente. Um add-on novo que
execute AutoDD/AutoDDL ou leve os exemplos do template poderia criar objetos,
alterar metadata ou colidir com o dono real do schema.

## Decisão

Criar um add-on independente, com Service Provider SDK baseado em
`@Controller`, para encapsular todas as mutações e integrações. O add-on usará
as tabelas existentes somente com mapeamentos comprovados, `autoDDL=false` e
nenhuma migração de schema. O dashboard chamará o contrato do add-on usando um
appKey novo.

## Alternativas

| Alternativa | Decisão |
| --- | --- |
| Rebuild do legado | Rejeitada: fonte/artefato não comprovados. |
| Dashboard chamando serviços antigos | Rejeitada: contrato e autorização instáveis. |
| Dashboard somente leitura | Permitida como etapa, mas não entrega a paridade completa. |
| Add-on SDK isolado sem DDL | Escolhida: separa risco, transação e publicação. |

## Consequências

**Positivas:** contrato explícito, rollback do gadget sem tocar no legado,
controle de autorização/concorrência e instalação sem migração.

**Compromissos:** exige novo appKey, captura de metadata no Om, gateways para
anexos/workflow e homologação manual antes de habilitar escrita.

## Gatilhos de revisão

Revisar se o Om não permitir o endpoint `@Controller`, se os serviços oficiais
de anexos/workflow não puderem ser chamados pela sessão do usuário ou se a
instalação exigir alterações de schema. Nesses casos, bloquear mutações e
registrar nova decisão antes de seguir.
