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

- Confirmação externa do nome do Service Provider e alinhamento do prefixo no
  dashboard HTML5.
- Versão mínima do Om e versão resolvida do plugin Addon Studio.
- Campo de concorrência de `BH_FACAPU`.
- Semântica de confirmação atômica/idempotência do store ainda não homologada.
- Serviços oficiais de anexos e workflow, permissões e identificador da tarefa.
- Aprovação do contrato e do roteiro de homologação antes do primeiro build
  funcional/deploy.
- Acesso de leitura aos objetos `TSIANX` e `TWFITAR` no ambiente de teste;
  ambos retornaram `status=3: Não autorizado`.

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
- T5: consulta por `NUAPURACAO` concluída; listagem/paginação/contadores ainda
  bloqueados pelo contrato de filtros.
- T9/T10/T11: casos de uso e portas existem, mas adapters concretos seguem
  bloqueados até permissões, concorrência, anexos/workflow e contrato do Om.
- T12: fachada `ApuracaoDashboardSP` criada com provider HTTP gerado
  automaticamente; as operações ainda não homologadas usam adapters
  fail-closed e não gravam no ambiente.
- Validação local: `:model:test` passou com 12 testes usando JDK 21 (target Java
  8); a inspeção do artefato não encontrou DDL/metadata de criação para
  `BH_FACAPU`, `TSIANX` ou `TWFITAR`.
