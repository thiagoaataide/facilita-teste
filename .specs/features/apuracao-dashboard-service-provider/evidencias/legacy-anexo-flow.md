# Referência local: fluxo de anexos legado

**Fonte:** repositório `facilitatelecoment`, commit
`68f758737abad0b1620cc46e0ffd8f41bd99775c` (inspeção somente leitura em
2026-09-23). Esta referência documenta comportamento do legado; não é evidência
de homologação nem autorização para reproduzir suas mutações.

## Fluxo observado

- `src/web/webapp/html5/Apuracao/Apuracao.js:34-41` oferece os tipos `FO`,
  `2V`, `FA`, `BO`, `NF` e `RE`; `:197-210` limita a seleção a um arquivo e
  exige tipo.
- `Apuracao.js:189-190, 215-225` faz upload temporário com a chave
  `ANEXO_SISTEMA_bhApuracao_<NUAPURACAO>`.
- `Apuracao.js:260-304` associa via `AnexoSistemaSP.salvar`, informando
  `pkEntity=NUAPURACAO`, `nameEntity=bhApuracao`, a mesma chave de sessão e o
  tipo em `description`. O legado persiste `POSSUIANEXO='S'` antes da
  associação; uma falha posterior pode deixar o indicador inconsistente.
- Após a associação, `BHAnexoServiceSP.atualizaTipoAnexo` é chamado. Em
  `src/main/java/br/com/sankhya/facilitatelecom/anexos/model/AnexosModel.java:61-80`,
  o serviço carrega e persiste `AnexoSistema`, define `BH_TIPO` e, se houver
  conta, reescreve `NOMEARQUIVO` usando identificador, ano/mês e CPF/CNPJ, além
  de trocar a descrição pelo nome do parceiro. Isso é mutação JAPE direta em
  `TSIANX`, pode expor dado pessoal no nome e não tem compensação visível se a
  etapa falhar; não foi copiado.
- `src/main/kotlin/br/com/sankhya/facilitatelecom/dao/AnexoSistemaDAO.kt:7-16`
  localiza anexos por `PKREGISTRO = '<NUAPURACAO>_bhApuracao'`, em ordem
  `DHCAD DESC`. O legado permite listar todos por essa chave.
- `AnexosModel.java:98-108` busca por `NOMEINSTANCIA` sem restringir o
  `PKREGISTRO` à apuração. Essa listagem ampla não deve ser portada; a futura
  listagem precisa ser escopada ao registro e autorizada.

## Adaptação no Add-on

`AnexarBusiness` aceita somente os seis códigos exibidos pelo fluxo legado e
exige a chave temporária exata da apuração solicitada. A requisição do Add-on já
representa um único arquivo. A convenção `PKREGISTRO` fica registrada para
orientar o adapter de listagem, sem consultar nem alterar `TSIANX`.

Não foram portados a gravação de `POSSUIANEXO`, a mutação de `AnexoSistema`, a
renomeação com CPF/CNPJ, acesso direto ao repositório de arquivos ou a listagem
genérica. A porta continua fail-closed até homologar o serviço disponível na
sessão do Add-on, autorização/escopo de leitura, validação do conteúdo e
compensação de falhas.
