# Memória do projeto

Fatos que valem para qualquer feature deste add-on. Decisões de arquitetura ficam em `STATE.md` (`AD-NNN`); aqui ficam fatos comprovados e armadilhas.

## Invariantes

- Nenhum DDL, em nenhuma feature. `BH_FACAPU`, `TSIANX`, `TWFITAR` e `TSIUSU` já existem no legado. `autoDDL=false`, entidades com `isNativeTable = true`, sem `dbscripts`.
- Deploy e publicação só com autorização explícita do usuário. Quem gera e instala o pacote na Facilita é o usuário.

## Schema comprovado

- `TSIUSU.BH_NOVAAUDIT` existe em produção. Validado pelo usuário antes de 2026-09-29. O legado lê a flag do usuário da sessão.
- `BH_FACAPU`: PK `NUAPURACAO NUMBER(10,0)`; flags em `VARCHAR2` (`S`/`N`); `VALOR` e `VALORREF` em `FLOAT`; datas em `DATE`. Sem coluna de versão (`AD_DHALTER` não existe).
- `TSIANX` da apuração: `NOMEINSTANCIA = 'bhApuracao'` e `PKREGISTRO = '{NUAPURACAO}_bhApuracao'`.

## Chamada da fachada

- Só funciona pela tela HTML5 do add-on, aberta pelo menu `FACAPU`, com `ServiceProxy.callService("facilita-apuracao-fatura-addon@ApuracaoDashboardSP.{metodo}", { request: {...} })`.
- `/mge/service.sbr` responde "Nenhum provedor" para qualquer prefixo. O gadget de BI não tem `ServiceProxy`.
- Sucesso chega em `responseBody.body`; erro do envelope chega em `responseBody.error`.

## Armadilhas do SDK

- `@Criteria` em entidade parcial de tabela nativa falhou no Om (`TSIANX` e `BH_FACAPU`). `@NativeQuery` funcionou. A causa da falha não foi registrada.
- A interface `@NativeQuery.Result` precisa ficar em arquivo próprio; aninhada no repositório não compila.
- Todo parâmetro de `@NativeQuery` precisa de `@Parameter(name = "...")`.
- `catch` que troca exceção por `ApuracaoBusinessException` precisa registrar a causa no log; sem isso, o log do Om não ajuda.

## Ambiente local

- Testes: `$env:JAVA_HOME = "C:\Program Files\Java\jdk-21.0.12"; .\gradlew.bat :model:test`. O `JAVA_HOME` padrão é um JRE 8 e quebra o Kotlin do plugin.
- O shell precisa rodar sem sandbox nesta máquina.
- Consultas no Om de produção pelo MCP `user-sankhya` passam por aprovação.

## Pendências transversais

- Fontes Java estão em UTF-8; o projeto exige ISO-8859-1. Mensagens com acento aparecem quebradas no Om (`NÃ£o`). Mensagens novas vão sem acento até a conversão.
- O plugin avisa que `service-providers.xml` foi criado à mão.
