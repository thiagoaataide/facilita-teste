# Gate HTML5 T22 — checklist (lado add-on)

**Referência:** `C:\projetos\sankhya-html5\.specs\features\facilita-apuracao-faturas\tasks.md` (T22)

O UAT de leitura é manual no Om (solicitante). Este arquivo registra o que o backend
espera após os ajustes de 2026-09-23.

## Pré-requisitos aplicados no HTML5

- Remoção de `AD_DHALTER` das queries (`dados.jsp`, `detalhe_payload.jsp`) — coluna
  ausente em `BH_FACAPU` no Om de teste.
- Versão enviada aos comandos via `observedEditionVersion(detail)` alinhada ao add-on.

## Checklist UAT (solicitante)

- [ ] Gadget carrega lista sem `GridConfig` e sem erro SQL na consulta JSP.
- [ ] Filtros de mês, pendência, anexo e busca combinam com a amostra funcional.
- [ ] Detalhe por `nuapuracao` validado server-side (`detalhe_payload.jsp`).
- [ ] Perfil autorizado e negado (somente leitura; sem mutação nesta fatia).
- [ ] Falha de leitura não exibe dados parciais/obsoletos como atuais.

## Se o gate reprovar

- Implementar `ApuracaoDashboardSP.listar` / `listarDetalhe` (T5) e apontar o gadget
  para a fachada antes de liberar integração transacional (T18).
