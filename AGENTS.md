# AGENTS.md — Add-on de Apuração de Faturas

## Escopo e fronteiras

Este repositório (`C:\projetos\facilita-apuracao-fatura-addon`) contém somente
o novo add-on backend da Facilita para expor um Service Provider da Apuração de
Faturas. O gadget HTML5 vive em
`C:\projetos\sankhya-html5` e o checkout legado em
`C:\projetos\facilitatelecoment` é apenas referência funcional. Nunca buildar,
copiar ou publicar o checkout legado para resolver esta demanda.

O objetivo do add-on é fornecer uma fachada estável para consulta, edição,
confirmação, nova auditoria, anexos e abertura de tarefa. A fachada deve usar a
sessão e as permissões do Sankhya Om e preservar as tabelas já existentes.

## Fontes de referência cruzada

Use as fontes abaixo conforme o tipo de mudança. Elas são referências de
comportamento e contrato; o código novo continua pertencendo a este
repositório e deve seguir o SDK Addon Studio 2.x.

### Backend legado — referência funcional

Quando uma tarefa envolver regra de apuração, campos, transições, anexos ou
workflow, consulte primeiro:

- `C:\projetos\facilitatelecoment\src\main\kotlin\br\com\sankhya\facilitatelecom\apuracao\model\ApuracaoModel.kt`
- `C:\projetos\facilitatelecoment\src\main\kotlin\br\com\sankhya\facilitatelecom\apuracao\controller\ApuracaoController.kt`
- `C:\projetos\facilitatelecoment\src\main\kotlin\br\com\sankhya\facilitatelecom\dao\bhApuracaoDAO.kt`
- `C:\projetos\facilitatelecoment\src\main\kotlin\br\com\sankhya\facilitatelecom\dao\AnexoSistemaDAO.kt`
- `C:\projetos\facilitatelecoment\src\main\java\br\com\sankhya\facilitatelecom\apuracao\service\Apuracao.java`
- `C:\projetos\facilitatelecoment\src\main\java\br\com\sankhya\facilitatelecom\anexos\services\BHAnexoService.java`
- `C:\projetos\facilitatelecoment\src\main\resources\datadictionary\Apuracao.xml`
- `C:\projetos\facilitatelecoment\src\main\resources\datadictionary\AnexoSistema.xml`

Use esses arquivos para descobrir nomes de campos, estados, regras e serviços
legados (`ApuracaoSP`, `BHAnexoServiceSP` e relacionados). Registre na spec a
evidência que foi confirmada no Om; o fonte legado sozinho não autoriza
reimplementar uma mutação.

O padrão legado usa APIs como `DynamicVO`, `JapeFactory.dao` e controllers
antigos. Essas APIs servem apenas para entender o comportamento histórico. A
implementação deste add-on deve traduzir a regra para `@JapeEntity`,
`JapeRepository`, `@Component` e `@Controller` do SDK atual.

### Novo HTML5 — referência de contrato e estrutura da UI

Quando uma tarefa envolver payload, filtro, ação do usuário, estado visual ou
integração com o Service Provider, consulte o projeto:
`C:\projetos\sankhya-html5\facilita\apuracao-faturas\`.

Arquivos principais:

- `tdb_dashboard.xml`: registro do gadget e entry point;
- `tdb_partida.jsp`: montagem da página e configuração da fachada;
- `dados.jsp` e `detalhe_payload.jsp`: consulta e detalhe somente leitura;
- `javascript\script.js`: filtros, seleção, chamadas da fachada, mutações,
  anexos, workflow, estados e exportação;
- `css\style.css`: layout, responsividade e estados visuais;
- `api\openapi.yaml`: contrato de consulta publicado;
- `api\postman\`: exemplos de chamadas e payloads.

Para decisões de produto e critérios de aceite, leia os documentos da feature
HTML5 em:
`C:\projetos\sankhya-html5\.specs\features\facilita-apuracao-faturas\` —
especialmente `spec.md`, `design.md`, `contracts.md`, `tasks.md`,
`homologacao.md` e `dados-sensiveis.md`.

O HTML5 deve chamar a fachada nova sob a sessão do Om e consumir o envelope
definido pelo backend. O prefixo atualmente presente no gadget é uma referência
de integração; confirme o prefixo registrado do novo Add-on antes de alterar o
contrato ou publicar.

### Fronteira entre os três projetos

1. O backend legado explica como a tela funcionava, mas não é dependência de
   build ou deploy deste repositório.
2. O novo HTML5 define a experiência, os payloads e o contrato que a fachada
   precisa atender.
3. Este repositório implementa a fachada transacional, autorização,
   persistência e erros seguros.
4. Quando houver divergência, preserve o contrato homologado no Om e registre
   a decisão em `.specs/STATE.md` e na feature correspondente.

Arquivos `.env`, tokens, senhas e configurações de infraestrutura dos projetos
de referência não devem ser lidos para copiar valores nem incluídos neste
repositório.

### Estrutura do novo backend

Organize novas responsabilidades nas camadas já existentes:

- `model/src/main/java/br/com/facilita/apuracao/controller/`: fachada pública
  fina, DTOs de entrada/saída e orquestração do Service Provider;
- `.../business/`: casos de uso e regras transacionais, sem detalhes de HTTP;
- `.../domain/`: comandos, filtros, snapshots e mapeamentos de domínio;
- `.../repository/`: leitura e persistência JAPE, sempre parametrizada;
- `.../integration/`: portas e adapters para anexos e workflow oficiais;
- `.../security/`: resolução de sessão e autorização fail-closed;
- `.../api/` e `.../api/error/`: contratos públicos, validações e códigos de
  erro;
- `model/src/main/resources/META-INF/parameter.xml`: somente configuração
  necessária do módulo.

O fluxo esperado é `Controller → Business → Repository/Integration`; entidades
e detalhes do banco não devem vazar para o contrato HTTP. Quando uma integração
oficial ainda não tiver contrato homologado, mantenha a porta e o adapter
fail-closed e registre a evidência pendente na feature.

## Skills obrigatórias

Antes de alterar código ou configuração, seguir estas skills:

- `sankhya-addon-sdk` para Addon Studio 2.x, JAPE, repositórios, controllers,
  transações e Bean Validation.
- `tlc-spec-driven` para o ciclo Specify → Design → Tasks → Execute.
- `coding-guidelines` e `clean-code` para implementação e revisão.
- `context7-mcp` para validar documentação de SDK, APIs e ferramentas.

## Consulta de documentação

Para qualquer decisão sobre Addon Studio, SDK, JAPE, `@Controller`,
`@Transactional`, Gradle ou API do Sankhya:

1. Usar Context7 primeiro: `resolve-library-id` com a biblioteca e a pergunta
   completa; depois `query-docs` com o identificador escolhido.
2. Registrar na spec a referência consultada e a versão, quando retornadas.
3. Se Context7 não tiver uma fonte confiável (o catálogo pode retornar um
   projeto homônimo de terceiros), consultar somente a documentação oficial em
   `developer.sankhya.com.br` e marcar a informação como fallback verificado.
4. Não inventar APIs. Uma lacuna permanece `PENDENTE` até ser confirmada no
   Om de homologação ou em documentação oficial.

## Regras de segurança do schema

`BH_FACAPU`, `TSIANX` e `TWFITAR` já existem e são mantidas pelo cliente ou
pelo Sankhya Om. São dependências externas deste add-on.

- Não criar, alterar, renomear ou excluir tabelas, colunas, índices ou chaves.
- O build final deve usar `autoDDL=false`; não gerar DDL para essas tabelas.
- Não criar XML de Table/NativeTable em `datadictionary` para essas entidades.
- Não usar os arquivos de exemplo do template (`datadictionary/tabela*.xml` e
  `dbscripts/V1.xml`) como base de instalação. Eles devem ser removidos ou
  isolados em uma tarefa de fundação, após revisão explícita.
- Qualquer objeto novo precisa de prefixo exclusivo aprovado antes do código.
- O add-on não deve executar SQL de migração no ambiente do cliente nesta fase.

## Padrões do SDK

- Fixar/verificar `br.com.sankhya.studio:gradle-plugin` em versão resolvida
  `>= 2.0.18` antes de criar classes do SDK. A declaração atual usa a faixa
  `2+`; a resolução validada no artefato foi `2.18.0`.
- Java 8 estrito; preservar o encoding do projeto e validar arquivos novos.
- Entidades usam `@JapeEntity`, `@Id` e `@Column` de
  `br.com.sankhya.studio.persistence`; não usar JPA.
- Entrada pública usa `@Controller(serviceName = "...SP")`, DTOs e
  `com.google.inject.Inject` no construtor; neste projeto o nome é
  `ApuracaoDashboardSP`.
- Regra de negócio fica em `@Component`; persistência fica em
  `@Repository`/`JapeRepository`; mutações ficam em método
  `@Transactional`.
- Entrada usa `@Valid` e mensagens em português. Erros sobem para
  `@ControllerAdvice`; não engolir exceções com `try/catch` no controller.
- Não usar APIs legadas como `ServiceBean`, `JapeFactory.dao`,
  `JapeSession.open` ou `DynamicVO` em código novo.
- Não retornar entidades JAPE no contrato HTTP; retornar DTOs/envelopes.

## Fluxo spec-driven

Toda alteração deve estar vinculada a uma feature em `.specs/features/` e
seguir:

1. Ler `.specs/STATE.md`, a spec e o design antes de implementar.
2. Executar uma tarefa atômica por vez, atualizando `tasks.md` e `memory.md`.
3. Incluir teste ou evidência de verificação no próprio task; não criar código
   sem critério binário de conclusão.
4. Atualizar riscos, decisões e contrato quando uma evidência do Om mudar uma
   premissa.
5. Não fazer deploy nem publicar no cliente sem autorização explícita e sem o
   smoke test de instalação sem DDL descrito na spec.

## Identidade e publicação

O `appKey` atualmente configurado pertence à solução **Apuração de Faturas** do
tipo Add-on e é a identidade de deploy deste projeto. Ele é diferente da
identidade da extensão legada. O código declara
`@Controller(serviceName = "ApuracaoDashboardSP")`; não substituir esse nome
por `ApuracaoDashboardControllerSP`.

O prefixo externo usado pelo gadget deve ser o appKey registrado seguido do
service name (`<appKey>@ApuracaoDashboardSP`), depois de confirmado o registro
do componente e alinhado com `tdb_partida.jsp`/`script.js` do novo HTML5. O
`group`, `rootProject.name` e `parceiroNome` continuam devendo permanecer
alinhados com o cadastro da solução.

Segredos, credenciais e `WILDFLY_HOME` ficam fora do controle de versão. O
`appKey` é o identificador da solução Add-on e deve permanecer alinhado ao
cadastro do Portal; ele não substitui tokens ou credenciais. O comando de
deploy só pode ser executado depois da aprovação do usuário e da homologação
manual.
