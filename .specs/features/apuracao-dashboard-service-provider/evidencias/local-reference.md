# Evidências locais — Apuração de Faturas

**Estado:** levantamento somente leitura; não comprova o contrato publicável do
Service Provider novo.

## Escopo e confiabilidade

Este documento compara a especificação do add-on com duas referências locais:

- `C:\projetos\sankhya-html5\.specs\features\facilita-apuracao-faturas` —
  especificação do dashboard, contrato cliente e roteiro de homologação;
- `C:\projetos\facilitatelecoment` — checkout legado usado como referência
  funcional, sem evidência de que corresponda ao artefato instalado no cliente.

A inspeção foi estática. Não foram chamados serviços do Sankhya, executadas
consultas SQL, alterados dados, compilados artefatos ou realizados deploys.

O checkout legado contém APIs antigas, mensagens com sinais de problema de
encoding (`Cód`, `Referência`, `apua??o`) e tratamento que engole exceções.
Portanto, nomes e comportamentos abaixo são evidências de referência, não
autorização para copiar implementação.

## Evidência do dashboard e da tela legada

O contrato do HTML5 documenta as seguintes capacidades observadas:

| Capacidade | Evidência local | Confiabilidade |
| --- | --- | --- |
| Filtro inicial pelo primeiro dia do mês e “somente pendentes” | `C:\projetos\facilitatelecoment\src\web\webapp\html5\Apuracao\Apuracao.js:121-127` | Alta para o comportamento do código de referência; ainda requer comparação no Om. |
| Pesquisa por sequência, conta, valor, referência e vencimento; filtro de anexo via `TSIANX` | `Apuracao.js:320-370` | Alta para a implementação legada; sem prova de que o filtro é o contrato desejado para o novo SP. |
| Apenas `DTVENC` e `VALOR` editáveis no dataset | `Apuracao.js:310-312`; `Apuracao.html:143-148` | Alta para a UI legada. |
| Confirmar e solicitar nova auditoria conforme `CONFIRMADO` | `Apuracao.html:186-187`; `Apuracao.js:471-483` | Alta para a UI; a transação e as permissões precisam ser revalidadas. |
| Anexo com exatamente um arquivo e tipos `FO`, `2V`, `FA`, `BO`, `NF`, `RE` | `Apuracao.js:34-41`, `260-307` | Alta para opções exibidas; limite, MIME e antivírus não estão comprovados. |
| Duplo clique abre tarefa nativa | `Apuracao.js:452-468` | Alta para a intenção da UI; identificador e permissão ainda são bloqueadores. |
| Falha original em `GridConfig.getSelectedColumns` após criação da grade | `Apuracao.js:129-142`; especificação HTML5 `spec.md` | Alta para o diagnóstico do navegador; não é evidência de um endpoint de escrita. |

O `Apuracao.html` também seleciona relações e campos sensíveis de
`bhConta`/`ContatoLoginOperadora`, incluindo `LOGIN`, `SENHA`, CPF/CNPJ, e-mail
e linha gestora (`Apuracao.html:6-14`, `210-240`). Isso comprova que a tela
legada podia exibir esses dados, mas não comprova que o novo dashboard/SP possa
ou deva devolvê-los. A matriz de dados sensíveis do projeto HTML5 mantém esses
campos fora do payload até aprovação.

## Entidade e campos observados

O dicionário legado `src/main/resources/datadictionary/Apuracao.xml` declara:

- tabela `BH_FACAPU`, instância `bhApuracao` e chave primária `NUAPURACAO`
  (`Apuracao.xml:4-8`);
- campos de identificação: `SEQUENCIACON`, `CODCONTA`, `NUNOTA`,
  `NUMCONTRATO`;
- valores/data: `VALOR`, `VALORREF`, `REFERENCIA`, `REFERENCIAADIADA`,
  `DTVENC`;
- estado/processo: `CONFIRMADO`, `IDINSTPRN`, `EMAILENVIADO`,
  `POSSUIANEXO`, `AUDITORIAFINALIZADA`, `FATURAMENTOLIBERADO`,
  `SEQUENCIAFATURAMENTO`, `NUFILA`;
- relações/identificação operacional: `CODVEND`, `OPERADORA`, `CLIENTE`;
- outros campos: `LINK`, `TAMANHOANEXO`, `PLANO`.

O mesmo XML relaciona `bhApuracao` a `bhConta` por `CODCONTA`, a `bhContrato`
por `NUMCONTRATO`, a `bhConfiguracaoConta` por `NUMCONTRATO` +
`SEQUENCIACON`, e a parceiros/vendedor por `OPERADORA`, `CLIENTE` e `CODVEND`
(`Apuracao.xml:16-57`). Isso é material de referência, não metadata capturada
da instalação do novo add-on.

O DAO legado confirma uma busca por `bhApuracao`/`NUAPURACAO` e uma classe VO
com os campos acima (`src/main/kotlin/br/com/sankhya/facilitatelecom/dao/BaseDAO.kt:25-34`;
`.../dao/vo/bhApuracaoVO.kt`). Ele não declara campo de versão/`DHALTER` para
concorrência. A ausência de um campo no VO não prova que a tabela não possua
metadata de alteração no ambiente.

## Serviços legados observados

| Serviço/prefixo | Operações observadas | Evidência e implicação |
| --- | --- | --- |
| `facilitatelecom@ApuracaoSP` | `confirmar`, `getTarefa` | `src/main/java/.../apuracao/service/Apuracao.java:14`; wrappers em `src/web/webapp/html5/commons/service.js:178-210`. É legado e não deve ser dependência automática do novo SP. |
| `facilitatelecom@BHAnexoServiceSP` | `atualizaTipoAnexo`, `downloadAnexos`, `downloadAnexosApuracao`, `emailAnexos`, `fetchFiles`, `getFile`, `listarEntidadesAnexo`, `listarSubItens` | `service.js:10-138`; anotação `BHAnexoService.java:12`. O fluxo da tela usa especificamente `atualizaTipoAnexo`; as demais operações pertencem a telas/fluxos de anexos mais amplos. |
| `AnexoSistemaSP.salvar` | upload/registro temporário do arquivo | chamada direta em `Apuracao.js:287-306`. O contrato do request inclui `pkEntity`, `nameEntity='bhApuracao'`, `description`, `typeAcess='ALL'`, `typeApres='GLO'` e nome do arquivo. Não é prova de que esse serviço esteja disponível ou seguro para o novo SP. |
| `facilitatelecom@PreparaApuracaoSP` | `getScheduleConfig`, `onSchedule` | `service.js:142-174`; preparação/generation, fora do escopo de mutações do dashboard. |
| `br.com.sankhya.workflow.listatarefa` | tela nativa aberta com `IDINSTPRN` e `IDINSTTAR` | `Apuracao.js:460-466`. A fachada nova deve retornar o identificador homologado, não executar update direto no workflow. |

O serviço legado `ApuracaoSP.confirmar` está anotado com `@RemoteController`
e chama `ApuracaoController` (`Apuracao.java:14-38`). Em exceções genéricas,
ele registra e retorna `"Sucesso"`; somente `IllegalStateException` e
`IllegalArgumentException` são relançadas. Essa é uma evidência de risco para o
contrato do dashboard, não um comportamento a preservar.

## Semântica legada de confirmação/auditoria

`ApuracaoModel.confirmarApuracao` (`src/main/kotlin/.../apuracao/model/ApuracaoModel.kt:21-49`)
mostra a seguinte lógica:

1. lê `BH_NOVAAUDIT` do usuário atual;
2. para cada `NUAPURACAO` informado, carrega por chave;
3. se já confirmado, exige `BH_NOVAAUDIT = 'S'` e limpa
   `IDINSTPRN`, `CONFIRMADO`, `AUDITORIAFINALIZADA`, `EMAILENVIADO` e
   `FATURAMENTOLIBERADO`;
4. se não confirmado, exige `VALOR != null` e marca `CONFIRMADO = true`;
5. salva o VO.

O arquivo não demonstra idempotência por chave, verificação de versão, escopo
por usuário/conta ou reconsulta após commit. O novo SP deve considerar todos
esses pontos bloqueados até captura no Om.

## Semântica legada de workflow

`ApuracaoModel.obterIdTarefa` executa uma seleção que faz `JOIN` de
`BH_FACAPU` com `TWFITAR` por `IDINSTPRN`, filtra `DHCONCLUSAO IS NULL` e
retorna `MIN(IDINSTTAR)` ordenado por `IDELEMENTO`
(`ApuracaoModel.kt:52-66`). Se não encontra valor, inicializa `BigDecimal.ZERO`
e a exceção somente ocorre quando o resultado é `null`; isso não é contrato
seguro para “sem tarefa”. Não há evidência local de metadata de `TWFITAR`, de
permissão por tarefa, nem do comportamento correto quando há múltiplas tarefas.

## Semântica legada de anexos

Na UI, `createANX`:

1. monta os parâmetros para a entidade `bhApuracao` com `pkEntity = NUAPURACAO`
   e `keySession = NUAPURACAO + '_bhApuracao'` (`Apuracao.js:260-280`);
2. marca `POSSUIANEXO = 'S'` e chama `dsApuracao.save()` antes de finalizar o
   upload (`Apuracao.js:283-286`);
3. chama `AnexoSistemaSP.salvar`;
4. usa o `NUATTACH` retornado em `BHAnexoServiceSP.atualizaTipoAnexo`.

O modelo legado de `BHAnexoServiceSP.atualizaTipoAnexo` busca `AnexoSistema`
por `NUATTACH`, altera `BH_TIPO`, localiza a entidade relacionada por
`NOMEINSTANCIA`/`PKREGISTRO`, exige `DTVENC` e, se houver conta/parceiro,
renomeia arquivo e descrição (`AnexosModel.java:62-83`). A relação de anexo
usa `PKREGISTRO = '<NUAPURACAO>_bhApuracao'` (`AnexoSistemaDAO.kt:8-15`).

O dicionário `AnexoSistema.xml` identifica `TSIANX` como tabela nativa, chave
`NUATTACH`, e declara `BH_TIPO` (`AnexoSistema.xml:4-22`). Porém, o legado
também usa `DynamicVO`, `JapeFactory` e acesso ao filesystem do repositório.
Isso não deve ser copiado para o add-on SDK sem um gateway suportado e sem
homologar autorização, limite, MIME, antivírus, compensação e visualização.

## O que não pode ser assumido

- Que o checkout legado corresponde ao JAR instalado ou ao código publicado.
- Que `ApuracaoSP`, `BHAnexoServiceSP` ou `AnexoSistemaSP` estejam disponíveis,
  estáveis ou autorizados para serem chamados pelo novo add-on.
- Que `BH_FACAPU` no ambiente do cliente tenha exatamente os campos/tipos do
  `Apuracao.xml` local.
- Que o tipo Java, escala ou nulabilidade de `VALOR` seja o mesmo do XML local.
- Que `DHALTER`, outro timestamp ou lock otimista exista para concorrência;
  também não se pode assumir que `DHALTER` tenha semântica de versão.
- Que `POSSUIANEXO` seja fonte confiável de existência: o dashboard HTML5
  explicitamente propõe verificar existência em `TSIANX`.
- Que `BH_NOVAAUDIT` seja a única autorização necessária, ou que o usuário do
  processo possa ser lido pelo mesmo mecanismo no novo SDK.
- Que `IDINSTPRN` seja suficiente para abrir tarefa; a UI exige também
  `IDINSTTAR`, cujo contrato não foi homologado.
- Que `MIN(IDINSTTAR)` seja a tarefa correta quando existem várias tarefas.
- Que upload, associação, atualização de `POSSUIANEXO` e alteração de tipo
  sejam uma única transação ou tenham compensação.
- Que os nomes e payloads dos endpoints propostos em `contracts.md` sejam
  nomes reconhecidos pelo Sankhya antes do registro do novo appKey.
- Que os campos de login, senha, CPF/CNPJ, e-mail, operadora ou cliente possam
  ser expostos apenas porque aparecem na tela legada.
- Que mensagens textuais do legado estejam íntegras; problemas de encoding
  tornam essas mensagens inadequadas como contrato.

## Bloqueadores para T2 — captura no Om de homologação

### Metadata real

Capturar somente leitura, por entidade/serviço suportado, para:

- `BH_FACAPU`: nome da entidade, chave, tipos/escala/nulabilidade de cada
  campo permitido, relacionamentos, regras de persistência, campos de auditoria
  e campo de alteração/versionamento;
- `TSIANX`/`AnexoSistema`: chave, campos de vínculo, `NOMEINSTANCIA`,
  `PKREGISTRO`, `BH_TIPO`, contrato de leitura e serviço oficial de upload;
- `TWFITAR`: chave, `IDINSTPRN`, `IDINSTTAR`, `DHCONCLUSAO`, `IDELEMENTO`,
  regra para escolher a tarefa e serviço oficial para abrir a tarefa.

O dicionário local pode orientar os nomes, mas não substitui a captura no
ambiente do cliente.

### Permissões

Para um usuário permitido e um usuário negado, registrar sem dados pessoais:

- consulta/listagem e detalhe de `BH_FACAPU`;
- alteração de `VALOR`/`DTVENC`;
- confirmação e solicitação de nova auditoria;
- upload, associação, visualização e download de anexos;
- consulta e abertura de tarefa;
- valor real e origem da permissão `BH_NOVAAUDIT`.

O resultado deve indicar se a autorização vem do nível/recurso, entidade,
workflow ou regra da aplicação; o campo `BH_NOVAAUDIT` isolado não basta.

### Concorrência

Executar em homologação, com dois usuários/sessões ou uma alteração externa:

1. ler uma apuração e guardar a versão/estado observado;
2. alterar a mesma linha fora do fluxo do SP;
3. tentar `atualizar`, `confirmar` e `solicitarNovaAuditoria` com o estado
   antigo;
4. registrar se o Om rejeita, sobrescreve ou atualiza silenciosamente.

Sem evidência de controle otimista/pessimista, T2 deve marcar as mutações como
bloqueadas, não escolher `DHALTER` por convenção.

### Anexos e workflow

Registrar request/response sanitizados, estado antes/depois e `correlationId`
para um caso de sucesso e cada falha de:

- arquivo único, tipos `FO`, `2V`, `FA`, `BO`, `NF`, `RE`, tamanho, MIME,
  antivírus e associação;
- falha depois do upload e antes da associação, para definir compensação;
- visualização/download autorizado e negado;
- tarefa única, múltiplas tarefas e ausência de tarefa;
- usuário sem permissão para cada operação.

## Resultado da auditoria local

A referência local sustenta o conjunto funcional principal e identifica os
serviços legados, as tabelas e os campos que devem orientar T2. Ela não sustenta
nenhum contrato publicável de escrita, anexos, workflow, autorização ou
concorrência. O próximo passo seguro é executar T2 somente como captura de
metadata/permissões/contratos no Om de homologação e atualizar
`contracts.md`/`tasks.md` com evidências sanitizadas; até isso, não criar
entidades JAPE nem habilitar mutações.

