# Memória do projeto

Fatos que valem para qualquer feature deste add-on. Decisões de arquitetura ficam em `STATE.md` (`AD-NNN`); aqui ficam fatos comprovados e armadilhas.

## Invariantes

- Nenhum DDL, em nenhuma feature. `BH_FACAPU`, `TSIANX`, `TWFITAR` e `TSIUSU` já existem no legado. `autoDDL=false`, entidades com `isNativeTable = true`, sem `dbscripts`.
- Quem gera e instala o pacote na Facilita é o desenvolvedor. O agente não executa `gerarAddon` nem `deployAddon`.

## Schema comprovado

- `TSIUSU.BH_NOVAAUDIT` existe em produção. Validado pelo usuário antes de 2026-09-29. O legado lê a flag do usuário da sessão.
- `BH_FACAPU`: PK `NUAPURACAO NUMBER(10,0)`; flags em `VARCHAR2` (`S`/`N`); `VALOR` e `VALORREF` em `FLOAT`; datas em `DATE`. Sem coluna de versão (`AD_DHALTER` não existe).
- `TSIANX` da apuração: `NOMEINSTANCIA = 'bhApuracao'` e `PKREGISTRO = '{NUAPURACAO}_bhApuracao'`.
- Associação de arquivo: a tela chama `AnexoSistemaSP.salvar` depois do upload com a chave `ANEXO_SISTEMA_bhApuracao_{NUAPURACAO}`. Documentação oficial: `developer.sankhya.com.br/reference/get_anexaarquivos`. O nome composto (AD-009) é aplicado em seguida pelo add-on, não por esse serviço.
- `IDENTIFICADOR` está em `BH_FACCON`. O CPF/CNPJ do nome é `TGFPAR.CGC_CPF` do `TITULARIDADE` da conta. O nome do parceiro na descrição é `TGFPAR.NOMEPARC` da `OPERADORA` da conta. O mês no nome não leva zero (`2026_9`).
- O botão antigo abre `/facilitatelecom/visualizadorArquivos.facilita?nuApuracao=` e esse caminho lê o anexo mais recente (`DHCAD DESC`). Ele não escolhe um `NUATTACH`. `AnexosModel.putFileSession` não grava o arquivo na sessão: a linha do `putHttpSessionAttribute` está comentada.

## Gravação pelo gadget

- Salvar valor e vencimento no dashboard HTML5 chama `ActionButtonsSP.executeJava` do botão `77` (`atualizarBotaoId` em `tdb_partida.jsp`). Parâmetros texto: `NUAPURACAO`, `VALOR` (`114.95`), `DTVENC` (`yyyy-MM-dd`).
- O Java grava a instância `bhApuracao`. O `ApuracaoListener` do legado regrava `NUMCONTRATO`, `OPERADORA`, `CLIENTE` e `CODVEND` a partir da configuração da conta na `REFERENCIA`. Não gera contrato.
- Comprovado em 2026-09-30 na apuração `189300545`: `DTVENC` foi para `05/10/2026` e voltou para `03/10/2026`; `VALOR` permaneceu `114.95`. Os quatro campos do listener bateram (contrato 1, operadora 3649, cliente 1, vendedor 4).
- Status `1` ou `2` é sucesso e abre o aviso central. Outro status fica no rodapé, com código `FA-`. O `NUAPURACAO` aparece no título "Apuração {n}"; a sequência contratual é outro campo.
- Ver anexo no gadget abre `/facilitatelecom/visualizadorArquivos.facilita?nuApuracao={NUAPURACAO}` numa aba nova. A `mgeSession` e a `chaveArquivo` são colocadas pelo visualizador. Comprovado em 2026-09-30. Abre o anexo mais recente da apuração, como a tela antiga. Não escolhe um `NUATTACH`.

## Chamada da fachada

- Só funciona pela tela HTML5 do add-on, aberta pelo menu `FACAPU`, com `ServiceProxy.callService("facilita-apuracao-fatura-addon@ApuracaoDashboardSP.{metodo}", { request: {...} })`.
- `/mge/service.sbr` responde "Nenhum provedor" para qualquer prefixo. O gadget de BI não tem `ServiceProxy`.
- Sucesso chega em `responseBody.body`; erro do envelope chega em `responseBody.error`.

## Armadilhas do SDK

- `@Criteria` em entidade parcial de tabela nativa falhou no Om (`TSIANX` e `BH_FACAPU`). `@NativeQuery` funcionou. A causa da falha não foi registrada.
- A interface `@NativeQuery.Result` precisa ficar em arquivo próprio; aninhada no repositório não compila.
- Todo parâmetro de `@NativeQuery` precisa de `@Parameter(name = "...")`.
- `catch` que troca exceção por `ApuracaoBusinessException` precisa registrar a causa no log; sem isso, o log do Om não ajuda.
- O Om valida com Hibernate Validator 5 (Bean Validation 1.1). `javax.validation.constraints.NotBlank` existe só no BV 2.0 e vira `UnexpectedTypeException` (`HV000030`), que o advice publica como `INTERNAL`. Campo obrigatório de texto usa `@NotNull` e `@Size(min = 1)`. Evidência: `prepararAnexo` em 2026-09-30, correlationId `bec1c684-16cc-4542-aa8e-ac29d735cf5e`.

## Ambiente local

- Testes: `$env:JAVA_HOME = "C:\Program Files\Java\jdk-21.0.12"; .\gradlew.bat :model:test`. O `JAVA_HOME` padrão é um JRE 8 e quebra o Kotlin do plugin.
- O shell precisa rodar sem sandbox nesta máquina.
- Consultas no Om de produção pelo MCP `user-sankhya` passam por aprovação.

## Pendências transversais

- Fontes Java estão em UTF-8; o projeto exige ISO-8859-1. Mensagens com acento aparecem quebradas no Om (`NÃ£o`). Mensagens novas vão sem acento até a conversão.
- O plugin avisa que `service-providers.xml` foi criado à mão.
