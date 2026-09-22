# Facilita — Add-on de Apuração de Faturas

Este repositório contém o novo add-on backend que deverá expor o Service
Provider da Apuração de Faturas para o dashboard HTML5. Ele é independente do
projeto `C:\projetos\sankhya-html5` e do checkout legado em
`C:\projetos\facilitatelecoment`.

## Estado da fundação

O projeto ainda está em preparação. A identidade abaixo é provisória e deve
ser confirmada antes da publicação:

- `rootProject.name`: `facilita-apuracao-fatura-addon`
- `group`: `br.com.facilitatelecom.apuracao.fatura`
- parceiro: `Facilita Telecom (provisório)`
- serviço publicado: `ApuracaoDashboardSP`

O `appKey` não é armazenado no repositório. Para uma execução autorizada,
defina-o no ambiente:

```powershell
$env:ADDON_APP_KEY = '<appKey registrado para este add-on>'
$env:ADDON_PARTNER_NAME = 'Facilita Telecom'
$env:WILDFLY_HOME = 'C:\\wildfly'
```

Sem `ADDON_APP_KEY`, o Gradle interrompe a configuração deliberadamente para
evitar o uso acidental do appKey do template ou de outro componente.

## Proteção do schema existente

As tabelas `BH_FACAPU`, `TSIANX` e `TWFITAR` já existem no ambiente do cliente.
Este add-on deverá apenas mapeá-las após confirmação da metadata; não poderá
criar, alterar ou excluir tabelas, colunas, índices ou chaves.

Por isso, `build.gradle` mantém `autoDDL=false`. Os arquivos de dicionário e
scripts de banco do template foram movidos, sem exclusão, para
`_template-quarantine/`. Eles não fazem parte do escopo da Apuração e não
devem ser reintroduzidos como base de instalação.

## Compatibilidade e documentação

- Java 8 permanece obrigatório.
- O plugin Addon Studio está fixado em `2.0.18`; a resolução do artefato ainda
  precisa ser comprovada em um ambiente com acesso ao repositório Maven.
- Para decisões sobre SDK, Gradle ou APIs Sankhya, consultar Context7 primeiro
  (`resolve-library-id` e depois `query-docs`). Se não houver fonte confiável,
  usar apenas a documentação oficial Sankhya e registrar o fallback na spec.

## Especificação

A especificação, o desenho, o contrato e as tarefas estão em
`.specs/features/apuracao-dashboard-service-provider/`. Consulte também
`AGENTS.md` antes de alterar o add-on.

## Publicação

Não executar deploy neste estágio. A publicação somente poderá ocorrer após a
definição do appKey/package-base, validação da metadata existente e smoke test
que comprove a ausência de DDL.
