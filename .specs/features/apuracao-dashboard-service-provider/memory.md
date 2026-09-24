# Memory — Service Provider da Apuração de Faturas

## Confirmed

- O add-on será separado do gadget HTML5 e do checkout legado.
- As tabelas `BH_FACAPU`, `TSIANX` e `TWFITAR` já existem; não há autorização
  para DDL, migração ou sobrescrita.
- A tela pode ter layout diferente; a exigência é preservar as capacidades e
  a consistência das operações.
- O ponto de entrada implementado segue `@Controller(serviceName =
  "ApuracaoDashboardSP")`, compatível com o contrato já usado pelo dashboard.
- O `appKey` `0bace5b4-6687-4507-9093-a80a82a03bcb` foi confirmado no Portal do
  desenvolvedor para a solução **Apuração de Faturas**, do tipo Add-on. Ele é a
  identidade de deploy deste projeto e não deve ser confundido com a identidade
  da extensão legada.
- Decisão confirmada em 2026-09-23: o gadget mantém a experiência, filtros e
  interação; toda mutação passa pelo `ApuracaoDashboardSP`, que aplica no
  backend as regras, a autorização do usuário corrente e a transação.
- A listagem/detalhe podem usar temporariamente JSP somente leitura se os
  parâmetros forem vinculados ou validados, a projeção for allowlisted e a
  autorização for comprovada. Se qualquer condição falhar, a leitura deve ir
  para uma consulta autorizada do Provider. JavaScript não grava diretamente
  em `BH_FACAPU`.
- Anexos e workflow permanecem em uma fase separada; só bloqueiam o MVP se o
  aceite funcional confirmar que são indispensáveis para concluir a aprovação.

## Lessons

- O template veio com `autoDDL=true`, appKey de terceiro e arquivos de exemplo;
  esses valores não são uma configuração segura para este cliente.
- A fundação agora usa `autoDDL=false`, Java 8 explícito e quarentena reversível
  dos artefatos de exemplo. A validação resolveu o plugin Addon Studio em
  `2.18.0`; o `build.gradle` foi preservado sem novas alterações.
- A API/DOMÍNIO inicial foi implementada sem persistência: DTOs, envelope,
  exceção segura, portas e validações de atualização/confirmação/nova auditoria.
- A metadata real do ambiente de teste confirmou `BH_FACAPU`, instância
  `bhApuracao`, PK `NUAPURACAO` e tipos dos campos mapeados; o repositório nativo
  foi limitado à leitura por chave e não gera DDL.
- Foram adicionados gateways abstratos e casos de uso de anexos/workflow, sem
  chamadas legadas ou mutações; os adapters concretos aguardam autorização e
  contrato do Om.
- O `GridConfig.getSelectedColumns` é uma falha de compatibilidade do cliente
  legado, não uma justificativa para expor SQL ou chamadas de escrita no
  dashboard.
- Context7 não retornou uma fonte Sankhya confiável nesta pesquisa; referências
  oficiais devem continuar sendo usadas como fallback e registradas.
- A captura do Portal confirmou que o `appKey` usado no `build.gradle` pertence
  a este Add-on; a pendência de identidade deixou de ser o appKey e passou a
  ser apenas a confirmação de parceiro, package-base e contrato externo.
- T6 adicionou as restrições de contrato que faltavam nos requests (`NUAPURACAO`
  positivo, campos mutáveis obrigatórios e direção `ASC`/`DESC`) e um fallback
  seguro para mensagens técnicas antes da serialização do envelope.
- T7 separou a gravação numa unidade `@Transactional(REQUIRED)` da orquestração
  e da releitura do estado, garantindo que a resposta seja construída a partir
  de uma consulta posterior ao retorno/commit do executor. O Controller não
  abre uma transação externa para `atualizar`, evitando que a releitura ocorra
  antes do commit.
- Exceções de negócio originadas na autorização ou no executor de escrita devem
  ser associadas ao correlation ID da chamada, substituindo um ID aleatório
  gerado pela exceção quando ela chega sem contexto da requisição.
- T8 mantém idempotência no limite transacional: o adapter deve procurar o
  resultado pela mesma `idempotencyKey` antes de comparar `expectedVersion` e
  aplicar efeitos. Isso permite retry da mesma requisição mesmo após a versão
  avançar; chave nova em versão/estado conflitante deve retornar `CONFLICT` sem
  mutação. A operação real ainda não foi homologada e segue fail-closed.

## Pending

- Próxima sequência aprovada em 2026-09-23: homologar a leitura segura do
  gadget (HTML5 T22); provar a primeira gravação via Provider (T15); depois
  habilitar confirmação/nova auditoria (T16). Anexos/workflow (T10/T11) ficam
  em fase complementar, salvo decisão funcional em contrário.
- Confirmação externa do nome do Service Provider e alinhamento do prefixo no
  dashboard HTML5.
- Versão mínima do Om e versão resolvida do plugin Addon Studio.
- Campo de concorrência de `BH_FACAPU`.
- Semântica de confirmação atômica/idempotência do store ainda não homologada.
- Serviços oficiais de anexos e workflow, permissões e identificador da tarefa.
- Aprovação do contrato e do roteiro de homologação antes do primeiro build
  funcional/deploy.
- Em 2026-09-23, uma consulta explícita ao perfil **Facilita Telecom**/**teste**
  obteve somente metadata de colunas de `TSIANX` via `ALL_TAB_COLUMNS`; a consulta
  a `ALL_CONSTRAINTS` retornou `status=3: Não autorizado`. Consultas anteriores
  de dados de `TSIANX` e `TWFITAR` também retornaram `status=3`.
- `sankhya_status` não indicou perfil padrão nem sessão viva, embora a consulta
  explícita de metadata tenha funcionado. A documentação pública oficial descreve
  upload via `sessionUpload.mge` e associação via `AnexoSistemaSP.salvar`, com
  API key e Bearer token; também documenta `CRUDServiceProvider.loadRecords`
  como consulta genérica. A entidade/permissões e a invocação na sessão atual,
  compensação, MIME/limite por arquivo e idempotência para `bhApuracao` não foram
  comprovadas; T10 segue bloqueada.

### T10 — Compatibilidade segura identificada no legado (2026-09-23)

- A referência somente leitura é o repositório `facilitatelecoment`, commit
  `68f758737abad0b1620cc46e0ffd8f41bd99775c`; detalhes e arquivos/linhas estão
  em `evidencias/legacy-anexo-flow.md`.
- A validação do novo caso de uso agora permite somente os tipos oferecidos
  pelo seletor legado e exige a chave
  `ANEXO_SISTEMA_bhApuracao_<NUAPURACAO>`. A API continua representando um
  arquivo por chamada.
- O filtro de associação legado é `PKREGISTRO='<NUAPURACAO>_bhApuracao'`, com
  mais recente primeiro. A listagem ampla por `NOMEINSTANCIA` não foi portada.
- Não foi copiada a gravação antecipada de `POSSUIANEXO`, a atualização de
  `AnexoSistema`/`TSIANX`, a renomeação com CPF/CNPJ ou o acesso ao filesystem.
  O gateway concreto segue fail-closed: serviço sob a sessão do Add-on,
  autorização, conteúdo/MIME/limite, antivírus, idempotência e compensação
  permanecem sem homologação.

### Reconciliação do baseline legado (2026-09-23)

- Spec, design e contrato agora distinguem comportamento observável no legado
  de mecanismo/permissão que exige homologação no Om. As evidências de fonte
  estão em `evidencias/local-reference.md`, `evidencias/legacy-anexo-flow.md` e
  `evidencias/html5-contract-alignment.md`.
- `BH_NOVAAUDIT` é lido do usuário atual. O T9 deixou de consultar
  `ApuracaoSnapshot.allowsNewAudit()`; a permissão fica em `AuthorizationPort`
  (adapter T16 ainda fail-closed). O reset legado observado limpa `IDINSTPRN`,
  `CONFIRMADO`,
  `AUDITORIAFINALIZADA`, `EMAILENVIADO` e `FATURAMENTOLIBERADO`, preservando
  valor, vencimento e anexo.
- O HTML5 novo envia `APURACAO_DASHBOARD_<NUAPURACAO>_<timestamp>` como chave de
  sessão; T10 exige `ANEXO_SISTEMA_bhApuracao_<NUAPURACAO>`. O mismatch bloqueia
  a requisição antes do gateway e requer alinhamento cliente/backend.
- Há mais duas divergências do contrato atual: o gadget usa
  `facilitatelecom@ApuracaoDashboardSP`, não o appKey registrado para este
  Add-on; e envia `adDhalter` como `version`, enquanto `BhApuracaoReadAdapter`
  não popula `ApuracaoSnapshot.version`.
- `SolicitarNovaAuditoriaBusiness.java` foi regravado em UTF-8 com a correção T9.
- T15: `BhApuracaoJapeStore` substitui `BlockedApuracaoStore` para leitura e
  `atualizar`; token `{valor}|{dtVenc}` alinhado ao HTML5 (`observedEditionVersion`).
- HTML5 T22: removido `AD_DHALTER` das JSPs; checklist em
  `evidencias/t22-read-gate-addon.md`.
- A compilação de `:model` em 2026-09-23 passou, mas `:model:generateFiles`
  avisou que `service-providers.xml` foi criado manualmente. A evidência antiga
  de T12 precisa ser revalidada por geração atual antes de afirmar descoberta
  automática do provider.

## Execution status

- T1: concluída — configuração segura, quarentena e identidade do Add-on
  definidas; appKey oficial confirmado no Portal.
- T3: identidade local concluída; contrato externo e apontamento do dashboard
  continuam pendentes.
- T2: metadata de `BH_FACAPU` capturada em `evidencias/om-teste-metadata.md`;
  permissões, concorrência, anexos e workflow continuam pendentes.
- T6: concluída — 12 testes unitários cobrem validações, requests herdados,
  correlation ID, códigos de erro e bloqueio de SQL/sessão/stack trace no
  envelope. Dependências de teste foram adicionadas apenas ao `model`.
- T7: caso de uso, autorização/versão observada, fronteira transacional,
  releitura pós-commit e 12 testes unitários concluídos; a gravação real segue
  fail-closed até homologar versão/concorrência e o formato exato de `DTVENC`.
- T8: caso de uso, autorização/eligibilidade, contrato idempotente e fronteira
  transacional com releitura pós-commit concluídos; 8 testes unitários. O store
  real segue fail-closed até homologar idempotência/concorrência no Om.
- T4: mapeamento nativo de `BH_FACAPU` concluído para os campos comprovados.
- T5: consulta por `NUAPURACAO` concluída; baseline de filtros/search/anexo
  documentado, mas listagem/paginação/contadores aguardam autorização e
  homologação da projeção no Om.
- T9: caso de uso corrigido (sem `allowsNewAudit` no snapshot); gravação
  fail-closed até T16.
- T15: adapter JAPE de `atualizar` implementado; homologação Om e autorização
  real pendentes (`evidencias/t15-write-adapter.md`).
- T10: porta e casos de uso existem; tipos aceitos e escopo da chave temporária
  foram adaptados do legado, e somente a metadata de colunas de `TSIANX` foi
  capturada no Om. A chave do gadget novo difere da chave legada validada; sem
  harmonização, serviço na sessão atual, listagem autorizada e compensação, o
  gateway segue fail-closed.
- T11: casos de uso e porta existem, mas o adapter concreto segue bloqueado
  até permissões e contrato do workflow no Om.
- Em 2026-09-22, sankhya_list_profiles falhou porque o CLI do 1Password não
  conseguiu conectar ao aplicativo desktop; nenhuma consulta ou mutação no Om
  foi executada nesta tentativa.
- T12: controller/fachada `ApuracaoDashboardSP` existe; a evidência de provider
  gerado automaticamente precisa ser revalidada porque a última compilação
  avisou que o XML era manual. As operações não homologadas seguem fail-closed.
- Validação local: `:model:test` passou com 12 testes usando JDK 21 (target Java
  8); a inspeção do artefato não encontrou DDL/metadata de criação para
  `BH_FACAPU`, `TSIANX` ou `TWFITAR`.

## Fase atual — 2026-09-24 (UAT HTML5 T22 + deploy add-on produção)

### Onde estamos

- **HTML5 T22 (leitura):** blocos A–G do checklist homologados na base Facilita
  (produção); exportação CSV, layout, paginação e detalhe JSP aprovados. **H**
  (anexo/tarefa via fachada) em andamento.
- **Add-on:** `facilita-apuracao-fatura-addon` **1.0.1** instalado na mesma base
  do gadget (Administração do Servidor, origem Place, 24/09/2026). `appKey` do
  Portal = `build.gradle` = `0bace5b4-6687-4507-9093-a80a82a03bcb`.
- **Integração SP (H.1 — Ver anexo):** o broker responde, mas retornou
  `HttpServiceBroker: Nenhum provedor ...` quando o gadget chamou
  `ApuracaoDashboardSP.listarAnexos` **sem** qualificar com o appKey. Tentativa
  com path do EAR (`/facilita-apuracao-fatura-addon/service.sbr`) gerou redirect
  de login (sessão do BI não vale nesse contexto). **Correção no HTML5:** chamada
  via `/mge/service.sbr` e `serviceName` =
  `{appKey}@ApuracaoDashboardSP.listarAnexos` (config em `tdb_partida.jsp` +
  `resolveFacadeServiceName` em `script.js`). **Aguardando republicação do
  gadget + reteste em Network.**

### Próximas ações — fazer o provedor responder no anexo (ordem sugerida)

1. **Republicar o componente BI** com o HTML5 atualizado; Ctrl+F5; em Network
   confirmar `serviceName=0bace5b4-6687-4507-9093-a80a82a03bcb@ApuracaoDashboardSP.listarAnexos`.
2. Se **ainda** “nenhum provedor”: reiniciar o servidor após instalação do
   add-on; revisar log WildFly na hora do clique (falha de DI impede registro do
   `@Controller`); testar outro método do mesmo SP (`getTarefa`) para isolar
   roteamento vs. operação.
3. Se o **SP responde** (JSON com envelope, não HTML de login): para T22/H.1,
   aceitar **fail-closed** do `BlockedAnexoGateway` / erro de permissão como
   “chegou na fachada”; marcar checklist H.1 e seguir H.2–H.4 na UI.
4. **Antes de abrir PDF de anexo de verdade (T10/T13):** harmonizar chave de
   upload HTML5 (`APURACAO_DASHBOARD_...`) vs. validação backend
   (`ANEXO_SISTEMA_bhApuracao_<NUAPURACAO>`); homologar `AnexoSistemaSP` /
   listagem na sessão do add-on; implementar adapter concreto substituindo
   `BlockedAnexoGateway`.
5. Atualizar `evidencias/html5-contract-alignment.md` e `contracts.md` quando H.1
   fechar (prefixo `appKey@`, não `facilitatelecom@`).

### Lembrete

- Add-on instalado **não** substitui o prefixo na URL: o broker roteia pelo
  **nome qualificado** do serviço. `appKey` é ID da solução no Portal, não token
  de autorização; a sessão do Om autentica a chamada em `/mge/service.sbr`.
