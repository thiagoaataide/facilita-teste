# Estado do projeto — Add-on de Apuração de Faturas

## Decisions

### AD-001 — Add-on isolado da UI e do legado

- **Status:** active
- O Service Provider será implementado neste repositório, separado do gadget
  HTML5 e do checkout legado `facilitatelecoment`.
- A UI chama o contrato do add-on; não grava diretamente em serviços legados.

### AD-002 — Schema existente é somente dependência

- **Status:** active
- `BH_FACAPU`, `TSIANX` e `TWFITAR` não serão criadas nem alteradas pelo add-on.
- O build deve desabilitar AutoDDL para a entrega e não deve produzir
  dicionário/DDL dessas tabelas.

### AD-003 — Controller SDK como fronteira pública

- **Status:** active
- O ponto de entrada usa `@Controller(serviceName =
  "ApuracaoDashboardSP")`, DTOs, `@Valid` e `@Transactional` onde
  houver mutação.
- O prefixo externo `<appKey>@...` só será fixado depois do novo appKey ser
  registrado.

### AD-004 — Context7 antes da documentação oficial

- **Status:** active
- Toda decisão sobre SDK/Gradle/API começa com `resolve-library-id` e
  `query-docs` no Context7. Se não houver correspondência confiável, a fonte
  oficial Sankhya é o fallback e a lacuna é registrada.

## Handoff

- **Fase atual:** fachada pública, provider HTTP, fundação segura, DTOs,
  envelope/Advice, portas, casos de uso puros e leitura nativa por chave foram
  implementados e compilados. As operações ainda não homologadas permanecem
  bloqueadas de forma explícita.
- **Próximo gate:** homologar no Om os filtros/paginação, permissões, campo de
  concorrência, serviços oficiais de anexos/workflow e identidade do novo
  appKey antes de ligar adapters de escrita e liberar as operações bloqueadas.
- **Bloqueios conhecidos:** `TSIANX` e `TWFITAR` retornaram `Não autorizado` no
  ambiente de teste; o contrato de listagem e a versão de concorrência de
  `BH_FACAPU` ainda não foram comprovados. O endpoint está publicado no
  artefato, mas as operações sem homologação permanecem fail-closed e não
  executam mutações concretas.
