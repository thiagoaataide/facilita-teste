# Tela de Apuração no add-on

## Problem Statement

O gadget de BI não alcança o `ApuracaoDashboardSP`: o broker de `/mge/service.sbr` não tem o provedor. A tela HTML5 do add-on, aberta pelo menu, tem `ServiceProxy` e já listou o anexo da apuração `185045240`. Falta uma tela de trabalho que use essa chamada e as demais da fachada, sem recolocar regra de gravação no JavaScript.

## Goals

- [ ] O usuário abre **Apuração de Faturas Addon** e trabalha a apuração sem o gadget de BI.
- [ ] Cada ação da tela chama `facilita-apuracao-fatura-addon@ApuracaoDashboardSP.{metodo}` e mostra o envelope da fachada.
- [ ] Leitura de grade, detalhe e anexo usa o usuário da sessão. Confirmar e nova auditoria seguem a regra do legado.

## Roadmap

| Feature | Entrega | Depende de | Estado |
| --- | --- | --- | --- |
| F1 Casca | Página no add-on com grade, detalhe e botões; menu no lugar de **Chamada da fachada** | AD-007 | Done |
| F2 Grade e detalhe | `listar` e `listarDetalhe` para o usuário da sessão | F1 | Done |
| F3 Ver anexo | `listarAnexos` na linha selecionada, nome e identificador | F1; fachada já comprovada | Done |
| F4 Confirmar e nova auditoria | `confirmar` e `solicitarNovaAuditoria` com a regra do legado | F2 | Pending |
| F5 Valor e vencimento | `atualizar` com a versão já exigida pela fachada | F2 | Pending |
| F6 Arquivo e tarefa | `anexar` e `getTarefa` | contrato homologado; fora das entregas F1–F5 | Pending |

## Out of Scope

| Item | Motivo |
| --- | --- |
| Abrir ou baixar o PDF | A listagem comprovada devolve identificador e nome, não o arquivo |
| Enviar anexo e abrir tarefa (F6) | Upload e workflow ainda não têm contrato homologado na sessão do add-on |
| Substituir o gadget de BI agora | Ele permanece consulta até a tela do add-on cobrir o mesmo uso |
| Criar ou alterar DDL | Invariante do projeto: nenhum `CREATE`, `ALTER` ou `dbscript`. `BH_FACAPU`, `TSIANX` e `TWFITAR` já existem no legado. `autoDDL` permanece `false`. O menu `FACAPU` só registra a tela |
| Chamar a fachada por `/mge/service.sbr` | Esse broker não encontra o provedor |

---

## Assumptions & Open Questions

| Assumption / decision | Chosen default | Rationale | Confirmed? |
| --- | --- | --- | --- |
| Onde a tela vive | `vc/src/main/webapp/html5`, menu `FACAPU` | Único caminho em que o `ServiceProxy` respondeu | y |
| Prefixo da chamada | `facilita-apuracao-fatura-addon@ApuracaoDashboardSP.{metodo}` | Comprovado em 2026-09-29; o `appKey` não é o prefixo | y |
| Leitura (grade, detalhe, anexo) | Usuário da sessão basta | Igual ao legado para listar anexo; grade e detalhe seguem o mesmo critério | y |
| Nova auditoria | Só se `BH_NOVAAUDIT = 'S'` no usuário da sessão e a apuração estiver confirmada | Única permissão extra do legado | y |
| Confirmar | Apuração aberta e com valor | `ApuracaoModel.confirmarApuracao` | y |
| Ordem das entregas | F1, F2, F3, depois F4 e F5; F6 fica de fora | Acordo de 2026-09-29 | y |
| Tela de prova | **Chamada da fachada** sai do menu quando F1 publicar a tela nova | Evita dois pontos de entrada | y |
| Filtro inicial da grade | Mês corrente e somente pendentes, como o gadget | O gadget já opera assim; a fachada ainda não lista | y |

**Open questions:** nenhuma fora da tabela. Os itens com Confirmed `n` seguem o default até revisão explícita.

Dimensões fora do escopo desta tela: expiração de dado, limite de taxa e pagamento. Não há TTL nem cobrança. Concorrência vale só em F5, pela versão da fachada. Falha de integração aparece como envelope `ok: false`, com código e `correlationId`, sem stack na tela.

---

## User Stories

### P1: Casca, grade, detalhe e anexo ⭐ MVP

**User Story**: Como usuário logado no Om, quero ver as apurações do mês, abrir o detalhe e ver o anexo da linha, para trabalhar sem o gadget de BI.

**Why P1**: É a primeira tela que se demonstra sozinha. `listarAnexos` já respondeu no Om.

**Acceptance Criteria**:

1. WHEN o usuário abre o menu **Apuração de Faturas Addon** THEN a tela SHALL carregar no HTML5 do add-on, com `ServiceProxy` disponível.
2. WHEN a grade pede a lista THEN a tela SHALL chamar `listar` e SHALL mostrar sequência, conta, contrato, vencimento, valor, confirmado e se possui anexo.
3. WHEN `listar` ou `listarDetalhe` é chamado por um usuário da sessão THEN a fachada SHALL responder sem `FORBIDDEN`.
4. WHEN não há apurações no filtro THEN a grade SHALL ficar vazia, com `ok: true`.
5. WHEN o usuário seleciona uma linha THEN a tela SHALL chamar `listarDetalhe` com o `NUAPURACAO` daquela linha.
6. WHEN o usuário pede os anexos da linha `185045240` THEN a fachada SHALL devolver o arquivo `199605` com o nome gravado em `TSIANX`.
7. WHEN a linha não tem anexo THEN `listarAnexos` SHALL devolver `files` vazio e `ok: true`.
8. WHEN a fachada recusa THEN a tela SHALL mostrar `code`, `message` e `correlationId` do envelope.
9. WHEN o usuário muda o mês, desmarca somente pendentes ou marca somente com anexo THEN a tela SHALL chamar `listar` com `mesReferencia`, `somentePendentes` e `possuiAnexo` iguais aos controles.

**Independent Test**: Instalar o pacote, abrir o menu, ver a grade do mês, selecionar `185045240` e ler o nome do PDF `199605`.

---

### P2: Confirmar, nova auditoria, valor e vencimento

**User Story**: Como usuário logado, quero confirmar, pedir nova auditoria e corrigir valor ou vencimento, para concluir o fluxo que o legado já fazia na sessão.

**Why P2**: A leitura precisa existir antes. A gravação já tem caso de uso na fachada e continua fechada pela autorização.

**Acceptance Criteria**:

1. WHEN a apuração está aberta e tem valor THEN `confirmar` SHALL marcá-la confirmada e a tela SHALL mostrar o detalhe reconsultado.
2. WHEN a apuração está confirmada e o usuário da sessão tem `BH_NOVAAUDIT = 'S'` THEN `solicitarNovaAuditoria` SHALL limpar confirmação, auditoria finalizada, e-mail enviado, faturamento liberado e `IDINSTPRN`.
3. WHEN a apuração está confirmada e `BH_NOVAAUDIT` não é `'S'` THEN a fachada SHALL recusar com `FORBIDDEN` e SHALL não alterar a linha.
4. WHEN o usuário grava valor ou vencimento com a versão exibida THEN `atualizar` SHALL persistir e devolver a linha reconsultada.
5. WHEN a versão enviada não é a vigente THEN `atualizar` SHALL recusar com `CONFLICT` e SHALL não gravar.

**Independent Test**: Confirmar uma apuração aberta com valor, repetir a nova auditoria com e sem `BH_NOVAAUDIT`, e gravar um vencimento com a versão corrente e com uma versão velha.

---

### P3: Enviar arquivo e abrir tarefa

**User Story**: Como usuário logado, quero anexar um arquivo e abrir a tarefa da apuração quando o contrato dessas operações existir no Om.

**Why P3**: Não há serviço de upload nem de workflow homologado na sessão do add-on. A tela não deve simular isso.

**Acceptance Criteria**:

1. WHEN o usuário aciona enviar arquivo ou abrir tarefa antes do contrato THEN a fachada SHALL responder `INTEGRATION` e SHALL não gravar `TSIANX` nem `TWFITAR`.

**Independent Test**: Os botões, se visíveis, mostram o envelope `INTEGRATION` e as tabelas permanecem iguais.

---

## Edge Cases

- WHEN `nuApuracao` não é inteiro positivo, ou passa de `2147483647` THEN a fachada SHALL responder `VALIDATION` e SHALL não consultar.
- WHEN a sessão não tem usuário THEN a fachada SHALL responder `FORBIDDEN`.
- WHEN a consulta de anexo falha THEN a fachada SHALL responder `INTEGRATION` com mensagem segura, e o detalhe técnico SHALL ficar só no log, com a chave da apuração.

---

## Requirement Traceability

| Requirement ID | Story | Phase | Status |
| --- | --- | --- | --- |
| TELA-01 | P1: abrir a tela pelo menu | F1 | Done |
| TELA-02 | P1: grade via `listar` | F2 | Done |
| TELA-03 | P1: leitura sem `FORBIDDEN` | F2 | Done |
| TELA-04 | P1: grade vazia | F2 | Done |
| TELA-05 | P1: detalhe da linha | F2 | Done |
| TELA-06 | P1: anexo conhecido | F3 | Done |
| TELA-07 | P1: anexo ausente | F3 | Done |
| TELA-08 | P1: erro visível | F1 | Done |
| TELA-15 | P1: filtros de mês, pendentes e anexo | F2 | Done |
| TELA-09 | P2: confirmar | F4 | Pending |
| TELA-10 | P2: nova auditoria permitida | F4 | Pending |
| TELA-11 | P2: nova auditoria recusada | F4 | Pending |
| TELA-12 | P2: atualizar com versão vigente | F5 | Pending |
| TELA-13 | P2: conflito de versão | F5 | Pending |
| TELA-14 | P3: arquivo e tarefa bloqueados | F6 | Pending |

**Coverage:** 14 requisitos. TELA-01 a TELA-08 mapeados em `tasks.md`. TELA-09 a TELA-14 ficam para F4–F6.

---

## Success Criteria

- [x] No Om, a grade do mês abre pelo menu do add-on e a linha selecionada mostra o anexo de `TSIANX` (evidência em `validation.md`).
- [ ] Confirmar, nova auditoria e alterar valor/vencimento só ocorrem pelos métodos da fachada, com a regra do legado.
- [ ] Enviar arquivo e abrir tarefa não gravam nada enquanto F6 estiver pendente.
