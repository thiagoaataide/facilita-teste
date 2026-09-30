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

### AD-006 — Dashboard para experiência; Provider para comandos de escrita
- **Decision**: O dashboard HTML5 mantém apresentação, filtros e interação;
  toda mutação passa por uma interface pequena do `ApuracaoDashboardSP`, que
  resolve usuário, autorização, estado e transação no backend.
- **Reason**: A falha de `GridConfig` exige substituir a experiência da tela,
  não deslocar regras autoritativas para JavaScript. Um caminho de leitura
  server-side pode acelerar a recuperação sem duplicar as regras de escrita.
- **Trade-off**: Lista e detalhe podem permanecer temporariamente em JSP
  somente leitura se os parâmetros forem vinculados ou validados, os campos
  forem allowlisted e a autorização for comprovada; caso contrário, a leitura
  também deve passar pelo Provider. Anexos e workflow seguem como fase
  separada, salvo se o aceite funcional os tornar pré-requisitos.
- **Scope**: gadget HTML5 e
  `.specs/features/apuracao-dashboard-service-provider/`; nenhuma escrita
  manual no navegador nem chamada direta a serviços legados para mutar
  `BH_FACAPU`.
- **Date**: 2026-09-23
- **Status**: active

### AD-007 — Tela operacional no HTML5 do add-on

- **Decision**: A tela que executa ações da apuração vive em `vc/src/main/webapp/html5` deste add-on e chama `ServiceProxy.callService("facilita-apuracao-fatura-addon@ApuracaoDashboardSP.{metodo}", ...)`. O gadget de BI permanece só leitura até essa tela cobrir o mesmo uso.
- **Reason**: O broker de `/mge/service.sbr` não encontra o provedor do add-on. A tela HTML5 do módulo, aberta pelo menu, entrega o `ServiceProxy` e a sessão. `listarAnexos` da apuração `185045240` retornou o anexo `199605` de `TSIANX` em 2026-09-29.
- **Trade-off**: O `appKey` continua sendo a identidade da solução no Place, e não o prefixo da chamada. Enviar arquivo e abrir tarefa continuam fora até haver contrato homologado.
- **Scope**: menu `FACAPU`, telas HTML5 do add-on e todas as chamadas de `ApuracaoDashboardSP` feitas pela UI.
- **Date**: 2026-09-29
- **Status**: active

### AD-008 — Gravação da apuração pela entidade JAPE, sem tabela de idempotência

- **Decision**: Mutações em `BH_FACAPU` passam pela entidade JAPE `bhApuracao` (`repository.save`), para acionar os eventos de CRUD. A leitura para a tela continua por `@NativeQuery`. Antes de gravar, o store confere estado e versão (`VALOR|DTVENC`) na mesma transação. A `idempotencyKey` continua obrigatória no contrato, mas não é guardada; um reenvio depois do commit encontra o novo estado e recebe `CONFLICT`, sem segundo efeito.
- **Reason**: Listeners de CRUD, atuais ou futuros, precisam ver a gravação, como no `dao.save` do legado. Guardar a chave exigiria tabela nova, e o projeto não cria DDL.
- **Trade-off**: A conferência de versão é leitura seguida de gravação, não condição no `WHERE`. A carga da entidade parcial falhou no Om por `@Criteria`, e precisa ser comprovada antes. O Javadoc de `ApuracaoStore.confirm` deixa de pedir replay pela chave.
- **Scope**: `BhApuracaoJapeStore`, `ApuracaoStore` e as gravações de F4 e F5.
- **Date**: 2026-09-29
- **Status**: active

### AD-009 — Nome do anexo igual ao legado

- **Decision**: Depois de associar o arquivo, `NOMEARQUIVO` fica `{IDENTIFICADOR}_{ano}_{mês sem zero}_{CGC_CPF}_{tipo}{extensão a partir do primeiro ponto}` e `DESCRICAO` fica com o nome do parceiro operadora. `BH_TIPO` recebe o código (`FO`, `2V`, `FA`, `BO`, `NF`, `RE`).
- **Reason**: O cliente já vê esse nome, inclusive com CPF ou CNPJ. Outro formato gera questionamento.
- **Trade-off**: O add-on grava dado pessoal no nome do arquivo, como `atualizaTipoAnexo` já grava.
- **Scope**: anexar da tela do add-on. Abrir tarefa e exclusão na tela ficam de fora.
- **Date**: 2026-09-30
- **Status**: active

### AD-010 — Gadget grava valor e vencimento por botão de ação

- **Decision**: No dashboard HTML5, Salvar alterações chama `ActionButtonsSP.executeJava` do botão `77` (`bhApuracao`, "Alterar Vencimento e Valor - HTML5"), com `NUAPURACAO`, `VALOR` e `DTVENC` em texto. A gravação é JAPE na instância `bhApuracao`, para o `ApuracaoListener` do legado rodar. Status `1` e `2` são sucesso e abrem aviso; o rodapé fica para erro. Confirmar, nova auditoria, anexo e tarefa continuam fora desse botão.
- **Reason**: O gadget não alcança `ApuracaoDashboardSP` em `/mge/service.sbr`. O botão de ação da plataforma alcança, e o `save` da entidade dispara o listener.
- **Trade-off**: O id `77` é o cadastro dessa base. A fachada do add-on deixa de ser o caminho de escrita do gadget para valor e vencimento.
- **Scope**: `facilita/apuracao-faturas` no repositório HTML5 (`tdb_partida.jsp`, `javascript/script.js`).
- **Date**: 2026-09-30
- **Status**: active
- **Evidence**: apuração `189300545` gravou `DTVENC` `05/10/2026` e voltou para `03/10/2026`, com `VALOR` `114.95`. `NUMCONTRATO` 1, `OPERADORA` 3649, `CLIENTE` 1 e `CODVEND` 4 bateram com a configuração da conta na referência.

## Handoff

- **Feature**: gadget HTML5 `facilita/apuracao-faturas`, com a escrita de valor e vencimento no botão `77` (AD-010). A tela do add-on segue em `.specs/features/tela-apuracao-addon/`.
- **Phase / Task**: F1–F3 no Om (1.0.6). F4 implementada na 1.0.7, UAT com o cliente. F7 e F8: T13–T18 commitados; T19 sem URL do Om. 1.0.10 troca `@NotBlank` por `@NotNull` + `@Size(min = 1)`: o HV 5 do Om lança `HV000030` e a fachada devolve `INTERNAL`
- **Completed**: T1–T18. Confirmar e nova auditoria gravam pela entidade JAPE (`findByPK` + `save`); nova auditoria exige `TSIUSU.BH_NOVAAUDIT = 'S'`. Anexar prepara, sobe pelo `AnexoSistemaSP.salvar` e grava o nome do legado
- **In-progress**: T19 — `abrirAnexo` não devolve URL
- **Next step**: no gadget, repetir a estratégia do botão `77` para confirmar e nova auditoria. `abrirAnexo` da tela do add-on continua sem URL de `NUATTACH`.
- **Blockers**: a carga da entidade `BhApuracao` pelo JAPE ainda não foi comprovada no Om; fontes Java em UTF-8 quebram acentos
- **Uncommitted files**: listagem TSIANX anterior, `build.gradle` e esta spec, se ainda não commitados
- **Branch**: `main`
