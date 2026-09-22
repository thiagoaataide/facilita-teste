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

## Pending

- Confirmação final de `parceiroNome`, `group`, `rootProject.name`, package-base
  e nome externo do Service Provider.
- Versão mínima do Om e versão resolvida do plugin Addon Studio.
- Campo de concorrência de `BH_FACAPU`.
- Serviços oficiais de anexos e workflow, permissões e identificador da tarefa.
- Aprovação do contrato e do roteiro de homologação antes do primeiro build
  funcional/deploy.
- Acesso de leitura aos objetos `TSIANX` e `TWFITAR` no ambiente de teste;
  ambos retornaram `status=3: Não autorizado`.

## Execution status

- T1: configuração segura e quarentena concluídas; appKey oficial confirmado
  no Portal, aguardando somente a confirmação final do parceiro.
- T3: appKey do Add-on confirmado e distinto da extensão legada; contrato
  externo, identidade restante e apontamento do dashboard continuam pendentes.
- T2: metadata de `BH_FACAPU` capturada em `evidencias/om-teste-metadata.md`;
  permissões, concorrência, anexos e workflow continuam pendentes.
- T6/T7/T8: DTOs, erros e casos de uso puros implementados; faltam testes e
  adapter real.
- T4: mapeamento nativo de `BH_FACAPU` concluído para os campos comprovados.
- T5: consulta por `NUAPURACAO` concluída; listagem/paginação/contadores ainda
  bloqueados pelo contrato de filtros.
- T9/T10/T11: casos de uso e portas existem, mas adapters concretos seguem
  bloqueados até permissões, concorrência, anexos/workflow e contrato do Om.
- T12: fachada `ApuracaoDashboardSP` criada com provider HTTP gerado
  automaticamente; as operações ainda não homologadas usam adapters
  fail-closed e não gravam no ambiente.
- Validação local: `:model:compileJava` passou com JDK 21 (target Java 8),
  `:model:test` passou sem fontes de teste e a inspeção do artefato não encontrou
  DDL/metadata de criação para `BH_FACAPU`, `TSIANX` ou `TWFITAR`.
