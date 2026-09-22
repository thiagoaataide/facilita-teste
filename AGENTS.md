# AGENTS.md — Add-on de Apuração de Faturas

## Escopo e fronteiras

Este repositório contém somente o novo add-on backend da Facilita para expor um
Service Provider da Apuração de Faturas. O gadget HTML5 vive em
`C:\projetos\sankhya-html5` e o checkout legado em
`C:\projetos\facilitatelecoment` é apenas referência funcional. Nunca buildar,
copiar ou publicar o checkout legado para resolver esta demanda.

O objetivo do add-on é fornecer uma fachada estável para consulta, edição,
confirmação, nova auditoria, anexos e abertura de tarefa. A fachada deve usar a
sessão e as permissões do Sankhya Om e preservar as tabelas já existentes.

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
  `>= 2.0.18` antes de criar classes do SDK. A configuração atual fixa 2.0.18;
  a resolução do artefato ainda precisa de confirmação em ambiente com acesso
  ao repositório Maven.
- Java 8 estrito; preservar o encoding do projeto e validar arquivos novos.
- Entidades usam `@JapeEntity`, `@Id` e `@Column` de
  `br.com.sankhya.studio.persistence`; não usar JPA.
- Entrada pública usa `@Controller(serviceName = "...ControllerSP")`, DTOs e
  `com.google.inject.Inject` no construtor.
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

O `appKey` atualmente presente no template pertence a outro contexto e não
deve ser reutilizado. O novo `appKey`, `group`, `rootProject.name` e o nome
externo do serviço devem ser definidos antes da implementação. A convenção
proposta é `ApuracaoDashboardControllerSP`; o prefixo final usado pelo gadget
será `<novoAppKey>@ApuracaoDashboardControllerSP` somente após o registro do
componente no Portal do Desenvolvedor.

Segredos, credenciais, `WILDFLY_HOME` e appKeys reais ficam fora do controle de
versão. O comando de deploy só pode ser executado depois da aprovação do
usuário e da homologação manual.
