# Evidência sanitizada — metadata do Om de teste

**Perfil:** Facilita Telecom  
**Ambiente:** teste  
**Data da captura:** 2026-09-21  
**Modo:** consultas somente leitura pelo `DbExplorerSP.executeQuery`

## BH_FACAPU

As consultas confirmaram a tabela existente, sem executar qualquer mutação:

- tabela: `BH_FACAPU`;
- instância: `bhApuracao`;
- descrição da instância: `Apuracao`;
- chave primária: `NUAPURACAO` (`NUMBER(10,0)`, não nulo);
- `VALOR` e `VALORREF`: `FLOAT`;
- `REFERENCIA`, `REFERENCIAADIADA` e `DTVENC`: `DATE`;
- flags `CONFIRMADO`, `EMAILENVIADO`, `POSSUIANEXO`,
  `AUDITORIAFINALIZADA` e `FATURAMENTOLIBERADO`: `VARCHAR2`;
- identificadores `CODCONTA`, `NUNOTA`, `NUMCONTRATO`, `SEQUENCIACON`,
  `IDINSTPRN`, `CODVEND`, `OPERADORA`, `CLIENTE`, `SEQUENCIAFATURAMENTO`,
  `NUFILA` e `PLANO`: `NUMBER(10,0)`;
- `LINK`: `VARCHAR2(1000)`;
- `TAMANHOANEXO`: `FLOAT`.

As colunas acima foram obtidas de `ALL_TAB_COLUMNS` e de uma leitura de uma
linha sem registrar valores de negócio. O resultado não comprova permissão de
escrita nem controle otimista de concorrência.

**Revalidação 2026-09-23:** nova consulta a `ALL_TAB_COLUMNS` listou 29 colunas
em `BH_FACAPU`; não há `AD_DHALTER`, `DHALTER` nem equivalente. O gadget HTML5
deixou de projetar `APU.AD_DHALTER`; o token provisório de edição é
`{valorPlain}|{dtVencIso}` (ver `evidencias/t15-write-adapter.md`).

## Anexos e workflow

### `TSIANX` — metadata de colunas

Em 2026-09-23, uma consulta somente leitura a `ALL_TAB_COLUMNS`, pelo
`DbExplorerSP.executeQuery` no perfil **Facilita Telecom**, ambiente **teste**,
retornou a seguinte estrutura de `TSIANX`:

| Coluna | Tipo | Nulo |
| --- | --- | --- |
| `NUATTACH` | `NUMBER(10,0)` | não |
| `NOMEINSTANCIA` | `VARCHAR2(30)` | não |
| `CHAVEARQUIVO` | `VARCHAR2(300)` | sim |
| `NOMEARQUIVO` | `VARCHAR2(1000)` | não |
| `DESCRICAO` | `VARCHAR2(300)` | não |
| `RESOURCEID` | `VARCHAR2(1000)` | sim |
| `TIPOAPRES` | `VARCHAR2(3)` | não |
| `TIPOACESSO` | `VARCHAR2(3)` | não |
| `CODUSU` | `NUMBER(5,0)` | não |
| `DHALTER` | `DATE` | não |
| `PKREGISTRO` | `VARCHAR2(512)` | não |
| `CODUSUALT` | `NUMBER(5,0)` | não |
| `DHCAD` | `DATE` | não |
| `LINK` | `VARCHAR2(1000)` | sim |
| `BH_TIPO` | `VARCHAR2(2)` | sim |

Essa consulta comprovou somente metadata, sem ler valores de anexos. Uma
consulta separada a `ALL_CONSTRAINTS` retornou `status=3: Não autorizado`; a
PK/constraints não foram confirmadas por essa fonte. Consultas anteriores a
dados de `TSIANX` e `TWFITAR` também retornaram `status=3: Não autorizado`.
Nenhuma escrita foi executada.

### Documentação pública de anexos

A documentação oficial [Anexar Arquivos](https://developer.sankhya.com.br/reference/get_anexaarquivos)
descreve o upload genérico em duas etapas: `sessionUpload.mge` e depois
`AnexoSistemaSP.salvar`, com `pkEntity`, `keySession` e `nameEntity`. A chamada
documentada usa API Gateway com `appkey` e Bearer token, e se destina a
entidades compatíveis com o componente padrão de anexos. A documentação também
descreve `CRUDServiceProvider.loadRecords` como consulta genérica às entidades
disponíveis. Esse é somente um caminho candidato: não comprova a entidade
`AnexoSistema`, suas permissões nem a invocação sob a sessão atual do Add-on.
Não há contrato público localizado para remover/compensar o upload, limite por
arquivo, MIME permitido ou idempotência, nem confirmação de disponibilidade do
fluxo para `bhApuracao`. Nenhuma chamada de upload, associação ou listagem foi
executada.

### `TWFITAR`

A captura anterior retornou `status=3: Não autorizado`; não houve nova consulta.

Permanecem pendentes:

- PK/constraints e permissão de leitura de dados de `TSIANX`;
- contrato de serviço homologado no Om para anexar e listar `bhApuracao`;
- validação homologada de tamanho, MIME, tipo e antivírus;
- idempotência e compensação para falha após upload;
- identificador de tarefa e regra de seleção do workflow;
- permissões de consulta/abertura.

## Consequência para a implementação

`BH_FACAPU` pode ser mapeada como dependência nativa para leitura, sem gerar
DDL. O inventário de colunas de `TSIANX` é evidência de schema, não autorização
para alterar a tabela nem prova do contrato de serviço. Mutação, anexos,
workflow e concorrência continuam bloqueados até uma captura autorizada desses
contratos.
