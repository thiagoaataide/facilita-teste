# Contrato do Service Provider — Apuração de Faturas

**Estado:** proposta para captura no Om de homologação.

## Fronteira

O gadget HTML5 chama `service.sbr` usando a sessão do usuário. O add-on recebe
DTOs, valida autorização/estado, delega a regra para `@Component` e retorna um
DTO de resposta. O controller não executa SQL nem contém regra de negócio.

O nome lógico publicado é `ApuracaoDashboardSP`. O prefixo de componente usado
externamente será `<novoAppKey>@ApuracaoDashboardSP` somente depois do registro
do novo appKey; não reutilizar `facilitatelecom@ApuracaoDashboardSP` nem o
appKey do template.

## Operações e invariantes

As consultas usam página zero-based. Quando omitido, `pagina` assume `0` e
`tamanhoPagina` assume `50`; o limite máximo de uma página é `500`. A ordenação
aceita somente campos/valores que o adapter homologado declarar como permitidos
e direção `ASC` ou `DESC`; nenhum valor recebido é concatenado em SQL.

| Operação | Invariantes antes da gravação | Resultado mínimo |
| --- | --- | --- |
| `listar` | filtros normalizados; limite de página; usuário autorizado | linhas e contadores sem campos sensíveis |
| `listarDetalhe` | `NUAPURACAO` pertence ao escopo do usuário | detalhe permitido |
| `atualizar` | somente `VALOR`/`DTVENC`; validação; versão observada | linha reconsultada após commit |
| `confirmar` | valor presente; estado elegível; permissão | confirmação idempotente |
| `solicitarNovaAuditoria` | registro confirmado; `BH_NOVAAUDIT = 'S'`; motivo conforme homologação | campos de processo reiniciados atomicamente |
| `anexar` | exatamente um arquivo; tipo e MIME permitidos; chave de idempotência | associação e metadados autorizados |
| `listarAnexos` | registro autorizado | lista sem URL/token indevido |
| `getTarefa` | instância existente ou consulta explícita | identificador de tarefa/ausência |

## Envelope

O formato é o definido em `spec.md`. Códigos de erro são estáveis e mensagens
seguras; o `correlationId` deve ser devolvido também nos erros.

## Dados ainda pendentes

1. Escala efetiva de `VALOR` e `VALORREF` no contrato JSON.
2. Formato de `DTVENC` no request.
3. Campo de versão/concorrência e semântica de `DHALTER` (não foi confirmado
   no inventário de `BH_FACAPU`).
4. Serviço oficial de upload, tipo de anexo e identificador retornado; a
   leitura de `TSIANX` ainda está sem autorização no teste.
5. Identificador de tarefa que abre o workflow; a leitura de `TWFITAR` ainda
   está sem autorização no teste.
6. Permissões para consulta, mutação e nova auditoria.

Metadata de `BH_FACAPU`, PK e instância `bhApuracao` estão comprovadas em
`evidencias/om-teste-metadata.md`.
