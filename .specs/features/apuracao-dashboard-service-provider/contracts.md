# Contrato do Service Provider — Apuração de Faturas

**Estado:** proposta para captura no Om de homologação.

**Baseline funcional:** decisões de interface e regras de negócio abaixo foram
comparadas ao fonte legado; isso não comprova permissões, transações ou serviços
disponíveis para o Add-on.

## Fronteira

O gadget HTML5 chama `service.sbr` usando a sessão do usuário. O add-on recebe
DTOs, valida autorização/estado, delega a regra para `@Component` e retorna um
DTO de resposta. O controller não executa SQL nem contém regra de negócio.

O nome lógico publicado é `ApuracaoDashboardSP`. O prefixo de componente usado
externamente será `<novoAppKey>@ApuracaoDashboardSP` somente depois do registro
do novo appKey; não reutilizar `facilitatelecom@ApuracaoDashboardSP` nem o
appKey do template.

## Faseamento da interface

- A interface do Provider é a seam de todas as mutações de apuração. O gadget
  não grava `BH_FACAPU` diretamente e não chama os serviços de escrita do
  legado.
- Lista e detalhe podem permanecer em JSP server-side somente leitura durante
  a recuperação inicial, desde que parâmetros, projeção e autorização sejam
  comprovados. Se isso não for possível, publicar uma consulta autorizada no
  Provider antes de liberar os dados.
- Os comandos iniciais são `atualizar`, `confirmar` e
  `solicitarNovaAuditoria`, ativados individualmente após homologação.
  `anexar`, `listarAnexos` e `getTarefa` ficam em uma fase própria e não
  bloqueiam a consulta ou a primeira fatia de escrita, salvo decisão explícita
  do aceite funcional.

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
| `solicitarNovaAuditoria` | registro confirmado; autorização do usuário corrente (no legado, `BH_NOVAAUDIT = 'S'`); versão/idempotência são proteções adicionais propostas | limpar `IDINSTPRN`, `CONFIRMADO`, `AUDITORIAFINALIZADA`, `EMAILENVIADO` e `FATURAMENTOLIBERADO`; preservar valor, vencimento e anexo |
| `anexar` | exatamente um arquivo; tipo em `FO`, `2V`, `FA`, `BO`, `NF`, `RE`; MIME/limite pendentes; chave de sessão escopada | associação e metadados autorizados |
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
4. Fluxo oficial de anexos para `bhApuracao`: a documentação pública descreve
   `sessionUpload.mge` seguido de `AnexoSistemaSP.salvar` no API Gateway, mas
   não comprova a chamada sob a sessão atual do Add-on nem fornece serviço de
   listagem específico, compensação, limite/MIME por arquivo ou idempotência.
   `CRUDServiceProvider.loadRecords` é candidato genérico para listagem, mas a
   entidade `AnexoSistema`, suas permissões e o uso sob a sessão atual não foram
   comprovados. As colunas de `TSIANX` foram capturadas sem valores;
   PK/constraints e autorização para ler registros seguem pendentes. A análise
   somente leitura do legado confirmou os tipos `FO`, `2V`, `FA`, `BO`, `NF`,
   `RE`, a chave temporária `ANEXO_SISTEMA_bhApuracao_<NUAPURACAO>` e o filtro
   `PKREGISTRO='<NUAPURACAO>_bhApuracao'`; o caso de uso preserva a allowlist e
   valida a chave. O legado também grava diretamente em `TSIANX`, marca
   `POSSUIANEXO` antes de concluir a associação e não mostra compensação; esses
   comportamentos não foram copiados. Não habilitar o adapter até homologar
   esses pontos no Om.
5. Identificador de tarefa que abre o workflow; a leitura de `TWFITAR` ainda
   está sem autorização no teste.
6. Permissões para consulta, mutação e nova auditoria.

7. **Chave de upload incompatível:** o gadget HTML5 atual envia
   `APURACAO_DASHBOARD_<NUAPURACAO>_<timestamp>` para `sessionUpload.mge` e
   encaminha a mesma chave a `anexar`. O backend T10 valida a convenção legada
   `ANEXO_SISTEMA_bhApuracao_<NUAPURACAO>`. Assim, a requisição atual é rejeitada
   antes do gateway. Não relaxar a validação por prefixo: alinhar o cliente e o
   backend e comprovar que a mesma chave é aceita pelo serviço suportado.
8. **Versão de leitura:** o gadget envia `adDhalter` como `version`, enquanto o
   adapter de leitura do Add-on não preenche `ApuracaoSnapshot.version`. Sem
   campo e semântica de versão comprovados, T7–T9 não podem aceitar comandos
   mutáveis.
9. **Identidade do provider:** o gadget atual chama
   `facilitatelecom@ApuracaoDashboardSP`; o Add-on usa appKey próprio. Confirmar
   o prefixo publicado e alinhar o cliente antes da homologação ponta a ponta.

Detalhes e referências de linha: `evidencias/html5-contract-alignment.md`.

`motivo` não é enviado pelo gadget nem observado no legado; permanece fora do
contrato publicado até aprovação funcional, apesar do campo opcional no DTO.

Metadata de `BH_FACAPU`, PK e instância `bhApuracao` estão comprovadas em
`evidencias/om-teste-metadata.md`.

A documentação pública consultada para upload está registrada em
`evidencias/om-teste-metadata.md`; ela não substitui a homologação do fluxo
interno do Add-on.
