# Evidência — Provider HTTP gerado

Data da validação: 2026-09-21.

## Resultado

- `:model:compileJava`: concluído com sucesso usando JDK 21.0.12 e target Java
  8.
- `:model:test`: concluído; o projeto ainda não possui fontes de teste
  (`NO-SOURCE`).
- `gerarAddon`: concluído sem deploy.
- Artefato: `build/libs/facilita-apuracao-fatura-addon.exts`.
- WAR: `build/dist/web/facilita-apuracao-fatura-addon-web.war`.

## Provider publicado

O Addon Studio gerou automaticamente `WEB-INF/resources/service-providers.xml`
com:

```xml
<provider domain="ApuracaoDashboardSP"
          class="br.com.facilita.apuracao.controller.ApuracaoDashboardSP"
          type="ejb-stateless"
          jndi="br/com/facilita/apuracao/controller/ApuracaoDashboardSP"
          authentication="true"/>
```

O JAR EJB contém a interface remota, bean e sessão gerados para
`ApuracaoDashboardSP`, além da implementação `ApuracaoDashboardController`.

## Segurança da entrega

Os adapters sem contrato homologado continuam fail-closed. Nenhuma operação de
escrita em `BH_FACAPU`, `TSIANX` ou `TWFITAR` foi habilitada. A inspeção do JAR
não encontrou `dbscripts`, `datadictionary` ou artefato de DDL.

O WAR ainda contém o gadget de exemplo `TMP_ExemploHTML5` herdado do template;
ele deve ser removido antes da publicação caso não seja desejado no ambiente.
