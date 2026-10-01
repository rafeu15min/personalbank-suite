# Personal Bank Suite

Microsserviços que agregam dados financeiros de várias instituições seguindo o
fluxo do Open Finance Brasil: o usuário autoriza o acesso no banco e a suite
importa contas, transações, cartões de crédito e investimentos. Feito com Java 21,
Quarkus, Kafka e PostgreSQL.

## Estado atual

O fluxo completo funciona localmente contra o `mock-bank`, um banco simulado que
imita o formato das APIs do Open Finance com dados fictícios.

As APIs reais do Open Finance só aceitam participantes autorizados pelo Banco
Central (cadastro no diretório, certificados ICP-Brasil, mTLS e FAPI), então a
suite não fala diretamente com bancos de verdade. O próximo passo previsto é
integrar um agregador regulado (por exemplo, a Pluggy), que dá esse acesso a
desenvolvedores.

## Arquitetura

```
 navegador ──POST /api/consents/initiate──▶ ms-consent ──token + consentimento──▶ banco
     │                                          │
     └──── autoriza no banco ──▶ callback ──────┘
                                                │ grava tokens criptografados (Postgres)
                                                │ publica InitialLoadEvent
                                                ▼
                                   Kafka: consentimentos.autorizados
                        ┌───────────────────────┼───────────────────────┐
                        ▼                       ▼                       ▼
                 ms-transactions         ms-credit-cards         ms-investments
                        │   pedem o token ao ms-consent e buscam os dados no banco
                        ▼                       ▼                       ▼
                 schema transactions     schema credit_cards     schema investments
```

1. O frontend chama `POST /api/consents/initiate` com a instituição escolhida.
   O `ms-consent` obtém um token da aplicação (`client_credentials`), cria o
   consentimento no banco e devolve a URL de autorização.
2. O usuário autoriza no banco, que redireciona para
   `/api/consents/callback` com um código. O `ms-consent` troca o código pelos
   tokens do usuário e os grava criptografados com AES-256-GCM.
3. O `ms-consent` publica um `InitialLoadEvent` (só o ID do consentimento e a
   instituição; o token não vai para o Kafka).
4. Cada serviço de dados consome o tópico com seu próprio consumer group, pede o
   token à API interna do `ms-consent`, busca os dados e grava no seu schema.
   Reprocessar o mesmo evento atualiza os registros em vez de duplicá-los.
   Eventos que falham vão para um tópico `dead-letter-topic-<canal>`.

### Módulos

| Módulo | Porta | Função |
|---|---|---|
| `common-library` | – | Configuração das instituições (`config/banks.yml`), criação dinâmica de clientes REST, envelope de resposta do Open Finance, evento compartilhado |
| `ms-consent` | 8083 | Fluxo de consentimento OAuth 2.0, guarda dos tokens, publicação do evento |
| `ms-transactions` | 8084 | Contas e transações |
| `ms-credit-cards` | 8085 | Cartões, faturas e lançamentos |
| `ms-investments` | 8086 | Renda fixa bancária, fundos e renda variável |
| `mock-bank` | 8090 | Banco simulado (OAuth, consentimentos e APIs de dados) |

As URLs de cada instituição ficam em `config/banks.yml`. Uma família de API
ausente faz o serviço correspondente ignorar aquela instituição.

## Como rodar

Pré-requisito: Docker com o plugin Compose. Java e Maven não precisam estar
instalados; o build roda dentro dos containers.

1. Crie o `.env` a partir do exemplo e gere a chave que protege os tokens:
   ```
   cp .env.example .env
   openssl rand -base64 32
   ```
   Cole o valor gerado em `TOKEN_ENCRYPTION_KEY` no `.env`.

2. Suba tudo:
   ```
   docker compose up -d --build
   ```

3. Inicie um consentimento:
   ```
   curl -s -X POST localhost:8083/api/consents/initiate \
     -H 'Content-Type: application/json' -d '{"institutionKey":"MOCKBANK"}'
   ```
   Abra o `redirectUrl` da resposta no navegador. O `mock-bank` aprova na hora
   e volta para o callback, que mostra o consentimento como `AUTHORISED`.

4. Confira os dados importados:
   ```
   docker compose logs ms-transactions ms-credit-cards ms-investments | grep concluída
   docker compose exec postgres psql -U personalbank -d personalbank \
     -c 'select * from transactions.account_transactions'
   ```

## Desenvolvimento

Para trabalhar em um serviço com live reload, pare o container dele e rode-o em
modo dev. O Postgres (5432), o Kafka (9092), o `ms-consent` e o `mock-bank` do
compose ficam acessíveis em `localhost`:

```
docker compose stop ms-transactions
docker run --rm -it --network host -v "$PWD":/src -v personalbank-m2:/root/.m2 \
  -w /src/ms-transactions maven:3.9-eclipse-temurin-21 mvn quarkus:dev
```

Na primeira vez, instale os módulos no cache do Maven antes:

```
docker run --rm -v "$PWD":/src -v personalbank-m2:/root/.m2 -w /src \
  maven:3.9-eclipse-temurin-21 mvn -B install -DskipTests
```

Para rodar os testes, troque `install -DskipTests` por `verify`.

O `.devcontainer` do VS Code também funciona: ele sobe só a infraestrutura
(Postgres, Kafka e `mock-bank`).

## Próximos passos

- Integrar um agregador regulado para acessar bancos reais.
- Renovar o access token com o refresh token (hoje a API interna responde
  `409` quando o token expira).
- Autenticação na API interna do `ms-consent` e cadastro de usuários
  (o `userId` do consentimento ainda é gerado aleatoriamente).
- APIs de leitura dos dados importados e um frontend.
- Paginação das APIs do banco e migrações com Flyway.
