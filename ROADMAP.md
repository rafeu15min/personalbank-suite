# Roadmap

Próximos passos do Personal Bank Suite, a partir do estado em que o fluxo de
consentimento e a carga inicial funcionam contra o `mock-bank`.

As fases estão em ordem de dependência. O objetivo imediato é chegar ao
controle de entradas e saídas (fase 3), então as fases 1 → 2 → 3 vêm primeiro e
as fases 4 a 6 depois.

## Fase 1 — Base para a API

Hoje os dados importados não pertencem a nenhum usuário, o que impede abrir uma
API de consulta com segurança.

1. **Usuários e login.** Cada consentimento recebe um `userId` aleatório.
   Proposta: Keycloak no docker compose e a extensão OIDC do Quarkus, que valida
   o token sem código próprio.
2. **Dados vinculados ao usuário.** O `userId` passa a ir no `InitialLoadEvent`
   e em todas as tabelas, e toda consulta filtra pelo usuário logado.
3. **Uma porta só para a API.** Cada serviço responde numa porta (8083 a 8086).
   Um gateway (nginx ou Traefik) junta tudo em `localhost:8080/api/...`.
4. **Documentação interativa.** OpenAPI com Swagger UI, para testar os endpoints
   pelo navegador enquanto não há frontend.
5. **API interna protegida.** Qualquer cliente que alcance o `ms-consent`
   consegue pedir o token de um consentimento em
   `/internal/consents/{id}/access-token`. Ela passa a exigir autenticação
   entre serviços.

## Fase 2 — API de consulta

6. **Contas e saldos.** Listar as contas e importar o saldo de cada uma
   (endpoint de saldos do Open Finance, ainda não usado).
7. **Transações.** Filtros por conta, período e tipo (crédito ou débito), com
   paginação.
8. **Cartões.** Cartões, faturas e lançamentos de cada fatura.
9. **Investimentos.** Posições por tipo e total investido.
10. **Consentimentos.** Listar os bancos conectados e revogar o acesso a um deles.

## Fase 3 — Controle de entradas e saídas

11. **Extrato unificado.** Todas as entradas e saídas do usuário, de todas as
    contas, em ordem de data.
12. **Resumo do período.** Total de entradas, total de saídas e saldo líquido,
    por mês ou por intervalo escolhido.
13. **Categorias.** Categoria automática por regras (ex.: contraparte contém
    "Vivo" → Telefonia). O usuário pode corrigir, e a correção pode virar regra.
14. **Lançamentos manuais.** Registrar movimentações que o banco não vê (ex.:
    dinheiro em espécie) numa "carteira" própria. Só lançamentos manuais podem
    ser editados ou apagados; nos importados, apenas categoria e observação.
15. **Conferência do saldo.** Comparar o saldo calculado pelas transações com o
    saldo informado pelo banco, para detectar transações faltando.
16. **Orçamento por categoria** (após o resto da fase). Limite mensal por
    categoria e quanto já foi gasto.

**Compras no cartão não entram no fluxo de caixa da conta.** O que sai da conta
é o pagamento da fatura, que já aparece como débito; somar as compras também
contaria o gasto duas vezes. O fluxo de caixa usa as transações da conta, e os
lançamentos do cartão ficam como detalhamento da fatura.

### Esboço da API

```
GET    /api/accounts                         contas com saldo
GET    /api/transactions?from=&to=&type=CREDITO|DEBITO&accountId=&category=&page=
PATCH  /api/transactions/{id}                categoria e observação
POST   /api/transactions                     lançamento manual
PUT    /api/transactions/{id}                editar (só manuais)
DELETE /api/transactions/{id}                apagar (só manuais)
GET    /api/cashflow/summary?from=&to=&groupBy=month|category
GET    /api/categories            POST /api/categories/rules
GET    /api/cards                 GET  /api/cards/{id}/bills/{billId}/transactions
GET    /api/investments
GET    /api/consents              DELETE /api/consents/{id}
```

## Fase 4 — Dados sempre atualizados

17. **Renovação do token.** Hoje a API interna responde `409` quando o access
    token expira (15 minutos no `mock-bank`). O `ms-consent` passa a usar o
    refresh token.
18. **Sincronização periódica.** Um agendador dispara a atualização de cada
    consentimento, buscando só o que é novo desde a última sincronização.
19. **Paginação das APIs do banco.** Hoje só a primeira página de cada lista é
    lida.
20. **Mock-bank mais realista.** Tela de login, dados diferentes por
    consentimento e transações novas surgindo com o tempo, para testar a
    sincronização.

## Fase 5 — Bancos reais

21. **Agregador regulado (ex.: Pluggy).** Entra como mais uma instituição no
    `banks.yml` e no `ms-consent`. O formato das respostas é outro, então os
    DTOs dos serviços de dados também mudam.

## Fase 6 — Qualidade e operação

22. **Migrações com Flyway** no lugar do `hibernate-orm.database.generation=update`.
23. **Testes de integração** com Postgres e Kafka reais (Testcontainers) e CI no
    GitHub Actions.
24. **Health checks e métricas**, para o compose saber quando cada serviço está
    pronto.
25. **Frontend**, consumindo a API pronta.
