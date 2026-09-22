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

## Anexos e workflow

As consultas de leitura a `TSIANX` e `TWFITAR` retornaram `status=3:
Não autorizado`. Nenhuma query alternativa ou comando de escrita foi tentado.
Por isso permanecem pendentes:

- chave e campos de vínculo de anexos;
- serviço oficial de upload/associação e limites de arquivo;
- identificador de tarefa e regra de seleção do workflow;
- permissões de consulta/abertura.

## Consequência para a implementação

`BH_FACAPU` pode ser mapeada como dependência nativa para leitura, sem gerar
DDL. Mutação, anexos, workflow e concorrência continuam bloqueados até uma
captura autorizada desses contratos.
