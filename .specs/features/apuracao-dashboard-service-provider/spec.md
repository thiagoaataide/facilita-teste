# Service Provider da Apuração de Faturas — Especificação

**Status:** In Progress — camada segura e mapeamento nativo de leitura
implementados; integrações mutáveis e identidade externa aguardam homologação.

## Contexto

A tela legada de Apuração de Faturas falha no navegador ao executar
`GridConfig.getSelectedColumns`, embora o carregamento da configuração retorne
HTTP 200. O fonte disponível é apenas referência funcional e não pode ser
considerado um artefato seguro de build/deploy.

O dashboard HTML5 já está sendo preparado em outro repositório. Para que ele
entregue as capacidades de escrita da tela atual, este add-on precisa publicar
uma fachada transacional independente, sem sobrepor o schema existente.

## Objetivo

Publicar um Service Provider Sankhya SDK 2.x que:

1. exponha consulta e comandos com envelope estável para o dashboard;
2. use a sessão/permissão do usuário do Om;
3. leia e atualize apenas campos comprovados das tabelas existentes;
4. trate concorrência, idempotência, erros e correlação;
5. possa ser instalado/atualizado sem DDL ou alteração de metadata das tabelas
   `BH_FACAPU`, `TSIANX` e `TWFITAR`.

## Fora de escopo

- Corrigir ou republicar o add-on legado.
- Criar/alterar tabelas, colunas, índices, constraints ou workflows.
- Reimplementar regras de faturamento que não tenham evidência no Om.
- Expor senhas, CPF/CNPJ ou credenciais da operadora.
- Publicar em produção antes da homologação manual.
- Fixar o appKey ou os nomes definitivos de entidades sem confirmação do
  Portal/Om.

## Requisitos funcionais

| ID | Requisito | Critério de aceite |
| --- | --- | --- |
| SP-01 | Identidade do add-on | `group`, `rootProject.name`, package-base e appKey são exclusivos e documentados; plugin resolvido é >= 2.0.18. |
| SP-02 | Consulta de apurações | Lista somente registros autorizados, com filtros de mês, pendência, anexo, busca, ordenação e paginação definidos no contrato. |
| SP-03 | Detalhe | Retorna apenas campos permitidos; dados sensíveis ficam mascarados ou fora do DTO. |
| SP-04 | Atualização | Permite editar somente `DTVENC` e `VALOR`, valida entrada, verifica estado observado e só responde sucesso após commit. |
| SP-05 | Confirmação | Exige valor válido, respeita autorização, é idempotente e detecta concorrência. |
| SP-06 | Nova auditoria | Exige `BH_NOVAAUDIT = 'S'` (ou regra homologada), limpa somente os campos aprovados e grava atomicamente. |
| SP-07 | Anexos | Faz upload/associação por integração suportada, com um arquivo e tipo válido, sem manipular `TSIANX` diretamente fora do contrato homologado. |
| SP-08 | Tarefa | Consulta a tarefa pendente e retorna o identificador correto para a UI abrir a tela nativa. |
| SP-09 | Erros | Responde envelope com `ok`, `correlationId`, `data` e erro seguro com código `VALIDATION`, `FORBIDDEN`, `CONFLICT`, `INTEGRATION` ou `INTERNAL`. |
| SP-10 | Observabilidade | Registra usuário, operação, `NUAPURACAO`, resultado, motivo e correlation ID sem segredo ou stack trace no payload. |
| SP-11 | Não sobreposição | Build/instalação não gera DDL, não cria metadata das tabelas existentes e falha se detectar artefato de schema não aprovado. |
| SP-12 | Compatibilidade HTML5 | O contrato suporta o adaptador do dashboard; o prefixo final é `<novoAppKey>@ApuracaoDashboardSP` após registro. |

## Operações públicas propostas

| Ação (`serviceName`) | Entrada mínima | Saída |
| --- | --- | --- |
| `ApuracaoDashboardSP.listar` | filtros, página e ordenação | linhas autorizadas, contadores e `nextPage` |
| `ApuracaoDashboardSP.listarDetalhe` | `nuApuracao` | detalhe não sensível |
| `ApuracaoDashboardSP.atualizar` | `nuApuracao`, `valor`/`dtVenc`, versão observada | linha reconsultada |
| `ApuracaoDashboardSP.confirmar` | `nuApuracao`, versão/chave idempotente | estado confirmado reconsultado |
| `ApuracaoDashboardSP.solicitarNovaAuditoria` | `nuApuracao`, motivo opcional, versão | estado reiniciado reconsultado |
| `ApuracaoDashboardSP.anexar` | `nuApuracao`, upload temporário, tipo | metadados autorizados do anexo |
| `ApuracaoDashboardSP.listarAnexos` | `nuApuracao` | anexos autorizados |
| `ApuracaoDashboardSP.getTarefa` | `nuApuracao`/identificador da instância | identificador da tarefa ou ausência explícita |

Os nomes e campos marcados como proposta só ficam definitivos depois da captura
de requests/responses no Om de homologação.

## Envelope HTTP

Sucesso:

```json
{
  "ok": true,
  "correlationId": "uuid-ou-id-do-om",
  "data": {},
  "error": null
}
```

Falha:

```json
{
  "ok": false,
  "correlationId": "uuid-ou-id-do-om",
  "data": null,
  "error": {
    "code": "CONFLICT",
    "message": "A apuração foi alterada por outro usuário.",
    "field": null
  }
}
```

O envelope não pode conter SQL, stack trace, sessão, senha, token de upload ou
segredo.

## Critérios de aceite e evidências necessárias

- [ ] Uma consulta em homologação compara resultados representativos do legado
  e do dashboard sem `GridConfig`.
- [ ] Cada comando mutável tem request/response, permissão, estado anterior e
  estado posterior capturados no Om.
- [ ] A versão/campo usado para concorrência está comprovado; sem isso,
  mutações permanecem bloqueadas.
- [ ] Anexo: limite, MIME, antivírus, tipo, associação e visualização estão
  comprovados.
- [ ] Workflow: identificador (`IDINSTPRN`, `IDINSTTAR` ou outro) e caminho de
  abertura estão comprovados.
- [ ] Instalação/upgrade em ambiente de teste não gera DDL nem altera metadata
  das três tabelas existentes.
- [ ] Falhas deixam `correlationId` rastreável e nunca exibem dado parcial como
  completo.

## Riscos e ambiguidades

| Item | Impacto | Tratamento |
| --- | --- | --- |
| AppKey do template pertence a outro contexto | Publicação no componente errado | Tarefa de fundação exige appKey novo e registrado. |
| `autoDDL=true` no template | Pode gerar DDL indesejado | Alterar para `false` antes do primeiro build funcional e inspecionar artefato. |
| Nomes reais de entidades/dicionário | Falha de mapeamento JAPE | Capturar metadata no Om; não inventar `@JapeEntity`. |
| Contrato de anexos/workflow | Escrita parcial ou link quebrado | Usar APIs suportadas e homologar cada operação isoladamente. |
| Versão de concorrência desconhecida | Sobrescrita de alteração de outro usuário | Usar `DHALTER`/versão somente após comprovação; senão bloquear escrita. |
