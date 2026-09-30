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
| F3 Listar anexo | `listarAnexos` na linha selecionada, nome e identificador | F1; fachada já comprovada | Done |
| F4 Confirmar e nova auditoria | `confirmar` e `solicitarNovaAuditoria` com a regra do legado | F2 | Implemented (UAT com o cliente) |
| F5 Valor e vencimento | `atualizar` com a versão já exigida pela fachada | F2 | Pending |
| F7 Ver arquivo | Abrir o anexo escolhido na lista da lateral | F3 | Desenhado (T13, T14, T19) |
| F8 Anexar | Enviar um arquivo e um tipo, nome igual ao legado | F3; AD-009 | Desenhado (T13, T15–T18) |
| F6 Abrir tarefa | `getTarefa` e a tela nativa de workflow | contrato de workflow | Pending |

## Out of Scope

| Item | Motivo |
| --- | --- |
| Excluir anexo nesta tela | Quem testa apaga a linha no Om depois; a tela não ganha botão de exclusão |
| Abrir tarefa (F6) | Workflow continua sem contrato homologado |
| Substituir o gadget de BI agora | Os botões entram na tela do menu. O gadget permanece consulta |
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
| Ordem das entregas | F1–F4 no código. A spec seguinte é F7 e F8. F5 e abrir tarefa ficam depois | A F4 permanece na tela; o UAT dela é com o cliente | y |
| Tela de prova | **Chamada da fachada** sai do menu quando F1 publicar a tela nova | Evita dois pontos de entrada | y |
| Filtro inicial da grade | Mês corrente e somente pendentes, como o gadget | O gadget já opera assim; a fachada ainda não lista | y |
| Onde ficam Ver arquivo e Anexar | Na lateral direita da tela do menu, com detalhe, confirmar, nova auditoria e a lista de nomes | Pedido de 2026-09-30. O gadget só descreve as ações | y |
| Qual arquivo o Ver abre | O item escolhido na lista da lateral. Sem escolha, Ver fica desabilitado | A lista já devolve identificador e nome; ela vira o seletor | y |
| Quantos arquivos por envio | Um por operação. Outro envio cria outro anexo na mesma apuração | O legado também guarda mais de um, cada um numa associação | y |
| O que o Anexar grava | Um arquivo, `BH_TIPO` em `FO`, `2V`, `FA`, `BO`, `NF` ou `RE`, chave `{NUAPURACAO}_bhApuracao`. `POSSUIANEXO` só vira `S` depois da associação e da troca do nome | O legado marca `POSSUIANEXO` antes e pode deixar o indicador mentindo. O desenho de F8 inverte essa ordem | y |
| Nome gravado em `TSIANX` | Igual ao legado: `NOMEARQUIVO` = `{IDENTIFICADOR}_{ano}_{mês}_{CGC_CPF}_{tipo}{extensão}` e `DESCRICAO` = nome do parceiro | Pedido de 2026-09-30 para não divergir do que o cliente já vê | y |
| Limpeza do teste | A exclusão é manual no Om, fora desta tela | O teste em produção precisa ser reversível sem um método novo | y |

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

### P3: Ver o arquivo e anexar

**User Story**: Como usuário logado, quero abrir o anexo da apuração e enviar um arquivo de teste, para conferir o arquivo de verdade e apagar essa linha depois no Om.

**Why P3**: A lista de nomes já funciona. Falta mostrar o conteúdo e gravar um anexo novo. Abrir tarefa continua fora.

**Acceptance Criteria**:

1. WHEN a linha tem anexos e o usuário escolhe um item da lista na lateral direita e pede para ver THEN a tela SHALL abrir esse arquivo e o usuário SHALL ver o conteúdo.
2. WHEN não há anexo, ou nenhum item da lista está escolhido THEN o controle de ver SHALL ficar desabilitado e a fachada SHALL não ser chamada.
3. WHEN o usuário envia um arquivo e um tipo `FO`, `2V`, `FA`, `BO`, `NF` ou `RE` THEN `anexar` SHALL gravar esse único anexo em `TSIANX` nessa chave, com `BH_TIPO` igual ao código, `NOMEARQUIVO` no formato `{IDENTIFICADOR}_{ano do vencimento}_{mês do vencimento}_{CGC_CPF}_{tipo}{extensão}` e `DESCRICAO` igual ao nome do parceiro. A lista da lateral SHALL mostrar o identificador e esse nome. Outro envio SHALL criar outro anexo.
4. WHEN a associação conclui THEN `POSSUIANEXO` SHALL ficar `S`. WHEN a associação falha THEN `POSSUIANEXO` SHALL permanecer como estava e a tela SHALL mostrar `code`, `message` e `correlationId`.
5. WHEN o tipo está vazio ou fora desses seis códigos, ou não há arquivo THEN a fachada SHALL responder `VALIDATION` e SHALL não gravar `TSIANX`.
6. WHEN o usuário da sessão pede ver ou anexar THEN a fachada SHALL responder sem `FORBIDDEN`.

**Independent Test**: Numa apuração com mais de um anexo, escolher um na lista da lateral e abrir esse arquivo. Enviar um arquivo pequeno com tipo `FO`, ver na lista o nome no formato do legado (`identificador_ano_mês_CPF ou CNPJ_FO.extensão`), abrir esse arquivo e apagar a linha no Om. Um segundo envio cria outra linha.

---

### P3: Abrir tarefa

**User Story**: Como usuário logado, quero abrir a tarefa pendente do fluxo quando esse contrato existir no Om.

**Why P3**: `getTarefa` continua fechado. A tela não deve abrir o workflow enquanto isso.

**Acceptance Criteria**:

1. WHEN o usuário aciona abrir tarefa antes do contrato THEN a fachada SHALL responder `INTEGRATION` e SHALL não gravar `TWFITAR`.

**Independent Test**: A chamada, se existir, devolve `INTEGRATION` e `TWFITAR` permanece igual.

---

## Edge Cases

- WHEN `nuApuracao` não é inteiro positivo, ou passa de `2147483647` THEN a fachada SHALL responder `VALIDATION` e SHALL não consultar.
- WHEN a sessão não tem usuário THEN a fachada SHALL responder `FORBIDDEN`.
- WHEN a consulta de anexo falha THEN a fachada SHALL responder `INTEGRATION` com mensagem segura, e o detalhe técnico SHALL ficar só no log, com a chave da apuração.
- WHEN `confirmar` recebe uma apuração sem valor THEN a fachada SHALL responder `VALIDATION` e SHALL não gravar.
- WHEN `confirmar` recebe uma apuração já confirmada ou com auditoria finalizada THEN a fachada SHALL responder `CONFLICT` e SHALL não gravar. Confirmar não reabre a apuração; isso é só `solicitarNovaAuditoria`.
- WHEN a versão enviada em `confirmar` ou `solicitarNovaAuditoria` não é a vigente THEN a fachada SHALL responder `CONFLICT` e SHALL não gravar.
- WHEN `anexar` é disparado de novo, com outra chave de idempotência THEN a fachada SHALL criar outro anexo. O legado também guarda mais de um arquivo na mesma apuração.
- WHEN a apuração não tem vencimento, ou a conta não tem parceiro THEN `anexar` SHALL falhar como o legado (`atualizaTipoAnexo`) e SHALL não concluir a troca do nome.
- WHEN a abertura do arquivo falha THEN a tela SHALL mostrar `code`, `message` e `correlationId`, e SHALL não indicar sucesso.

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
| TELA-09 | P2: confirmar | F4 | Implemented (UAT pendente) |
| TELA-10 | P2: nova auditoria permitida | F4 | Implemented (UAT pendente) |
| TELA-11 | P2: nova auditoria recusada | F4 | Implemented (UAT pendente) |
| TELA-12 | P2: atualizar com versão vigente | F5 | Pending |
| TELA-13 | P2: conflito de versão | F5 | Pending |
| TELA-14 | P3: abrir tarefa bloqueada | F6 | Pending |
| TELA-16 | P3: abrir o arquivo escolhido na lista | F7 | In Tasks |
| TELA-17 | P3: anexar um arquivo e um tipo | F8 | In Tasks |

**Coverage:** 16 requisitos. TELA-01 a TELA-08 e TELA-15 mapeados em `tasks.md`. TELA-09 a TELA-11 implementados, UAT com o cliente. TELA-12, TELA-13, TELA-14, TELA-16 e TELA-17 sem tarefa ainda.

---

## Success Criteria

- [x] No Om, a grade do mês abre pelo menu do add-on e a linha selecionada mostra o anexo de `TSIANX` (evidência em `validation.md`).
- [ ] Confirmar, nova auditoria e alterar valor/vencimento só ocorrem pelos métodos da fachada, com a regra do legado.
- [ ] Na lateral direita, ver abre o anexo escolhido na lista, e cada envio grava um arquivo que aparece nessa lista e pode ser apagado no Om.
- [ ] Abrir tarefa não grava `TWFITAR` enquanto F6 estiver pendente.
