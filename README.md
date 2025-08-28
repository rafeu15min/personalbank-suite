Com certeza. O `README.md` é um documento vivo e deve refletir a arquitetura final do projeto.

Com a adição do `ms-consent` e a solidificação da nossa estrutura, o fluxo de trabalho e a organização do projeto ficaram muito mais claros. Atualizei o `README.md` para incluir todas essas mudanças.

*(Informações geradas em 28 de Agosto de 2025, 11:06, em Catanduva, SP, Brazil)*

-----

# Personal Bank Suite

*Última atualização: 28 de Agosto de 2025*

O **Personal Bank Suite** é um ecossistema de microsserviços projetado para agregar e processar dados financeiros de múltiplas instituições através do padrão Open Finance Brasil. A arquitetura é moderna, escalável e orientada a eventos, utilizando Quarkus, Java, Kafka e Docker.

## Sumário

  * [Arquitetura](#arquitetura)
      * [Visão Geral](#visão-geral)
      * [Estrutura dos Módulos](#estrutura-dos-módulos)
  * [Ambiente de Desenvolvimento](#ambiente-de-desenvolvimento)
      * [Pré-requisitos](#pré-requisitos)
      * [Configuração Inicial](#configuração-inicial)
      * [Fluxo 1: Com VS Code e Dev Containers (Recomendado)](#fluxo-1-com-vs-code-e-dev-containers-recomendado)
      * [Fluxo 2: Com Docker Compose Puro (Alternativa)](#fluxo-2-com-docker-compose-puro-alternativa)
  * [Como Usar a Suite](#como-usar-a-suite)
      * [O Fluxo de Consentimento](#o-fluxo-de-consentimento)
      * [A Reação em Cadeia (Pub/Sub)](#a-reação-em-cadeia-pubsub)
  * [Build e Deploy em Produção](#build-e-deploy-em-produção)
      * [Construindo o Projeto](#construindo-o-projeto)
      * [Gerando Imagens Docker](#gerando-imagens-docker)
      * [Hospedagem](#hospedagem)
-----

## Arquitetura

### Visão Geral

Este projeto é um **sistema distribuído e orientado a eventos**. O `ms-consent` atua como a porta de entrada (entrypoint), orquestrando o fluxo de autorização do usuário. Após um consentimento bem-sucedido, ele dispara eventos via **Apache Kafka**, que são consumidos pelos microsserviços de dados de forma assíncrona e resiliente.

### Estrutura dos Módulos

O projeto é um **Maven Multi-Módulo**, garantindo consistência e reuso de código.

  * **`Personal-Bank-Suite/` (Raiz)**

      * Contém o `pom.xml` "Pai" que gerencia as dependências e versões para todos os módulos.
      * Contém a configuração do ambiente de desenvolvimento (`.devcontainer`, `docker-compose.yml`, etc.).
      * Contém a configuração centralizada das instituições (`config/banks.yml`).

  * **`common-library/`**

      * Uma biblioteca compartilhada (`.jar`) com todo o código comum.
      * **Responsabilidades:** Lógica de configuração multi-banco, criação dinâmica de clientes REST (`ApiClientProvider`), DTOs e eventos genéricos.

  * **`ms-consent/`**

      * O microsserviço orquestrador, responsável pela interação com o usuário.
      * **Responsabilidades:** Expor a API para o frontend, gerenciar o fluxo de consentimento OAuth 2.0 com os bancos, armazenar tokens de forma segura e **publicar eventos** no Kafka para os outros serviços.

  * **`ms-transactions/`**, **`ms-credit-cards/`**, **`ms-investments/`**

      * Microsserviços "operários", focados em seus respectivos domínios.
      * **Responsabilidades:** **Consumir eventos** do Kafka, usar a `common-library` para se conectar à API do banco correto e buscar/processar/armazenar os dados financeiros.

-----

## Ambiente de Desenvolvimento

O ambiente é padronizado e reproduzível para garantir consistência entre todos os desenvolvedores.

### Pré-requisitos

  * [Git](https://git-scm.com/)
  * [Docker Desktop](https://www.docker.com/products/docker-desktop/) (com backend WSL 2 no Windows)
  * Para o fluxo recomendado: [Visual Studio Code](https://code.visualstudio.com/) e a extensão [Dev Containers](https://marketplace.visualstudio.com/items?itemName=ms-vscode-remote.remote-containers).

### Configuração Inicial

Antes de iniciar o ambiente, é necessário criar o arquivo de configuração local.

1.  Na raiz do projeto, crie um arquivo chamado `.env`.
2.  Adicione as variáveis de ambiente para seu ambiente local, como as credenciais do banco de dados e a localização do Kafka.
    ```env
    # .env (Exemplo)
    DB_USER=personalbank_user
    DB_PASSWORD=local-super-secret-password
    DB_URL=jdbc:postgresql://localhost:5432/personalbank_db
    KAFKA_BROKERS=localhost:9092
    ```

Este arquivo é ignorado pelo Git e garante que seus segredos locais não sejam versionados.

### Fluxo 1: Com VS Code e Dev Containers (Recomendado)

1.  **Clone o Repositório** e abra a pasta raiz (`Personal-Bank-Suite`) no VS Code.
2.  Uma notificação aparecerá no canto inferior direito. Clique em **"Reopen in Container"**.
3.  Aguarde. O VS Code irá construir a imagem Docker, iniciar o container e conectar-se a ele. Este processo é automatizado:
      * O `postCreateCommand` irá executar `mvn clean install` para construir o projeto pela primeira vez.
      * O `postStartCommand` irá executar `docker-compose up -d` para iniciar a infraestrutura (Kafka).
4.  Para rodar um serviço, abra um terminal integrado (`Ctrl` + `     ` \`) e execute:
    ```bash
    cd ms-consent
    ./mvnw quarkus:dev
    ```

### Fluxo 2: Com Docker Compose Puro (Alternativa)

1.  **Clone o Repositório.**
2.  **Inicie o Ambiente Completo:** Na pasta raiz, execute o comando abaixo para subir a infraestrutura e o container de desenvolvimento.
    ```bash
    docker-compose -f docker-compose.yml -f docker-compose.dev.yml up -d --build
    ```
3.  **Acesse o Terminal do Ambiente:**
    ```bash
    docker-compose exec develop bash
    ```
4.  Uma vez dentro do shell, execute os serviços como no fluxo anterior.

-----

## Como Usar a Suite

A interação com a suite começa sempre pelo `ms-consent`.

### O Fluxo de Consentimento

1.  **Seu Frontend** faz uma chamada `POST` para a API do `ms-consent`:
      * **Endpoint:** `http://localhost:8083/api/consents/initiate`
      * **Corpo (Body):**
        ```json
        {
          "institutionKey": "NUBANK"
        }
        ```
2.  **`ms-consent`** responde com uma URL de redirecionamento do banco.
3.  **Seu Frontend** redireciona o usuário para essa URL para que ele autorize o acesso.
4.  Após a autorização, o banco redireciona o usuário de volta para o endpoint de `callback` do `ms-consent`, que finaliza o processo.

### A Reação em Cadeia (Pub/Sub)

1.  Após um consentimento bem-sucedido, o `ms-consent` publica um evento `InitialLoadEvent` em tópicos do Kafka (ex: `consentimentos.carga-inicial.transacoes`).
2.  Os microsserviços `ms-transactions`, `ms-credit-cards`, etc., que estão escutando seus respectivos tópicos, são ativados.
3.  Cada serviço então começa o trabalho de buscar os dados históricos daquele usuário no banco correspondente.

-----

## Build e Deploy em Produção

### Construindo o Projeto

Para compilar e empacotar todos os módulos, execute o seguinte comando na raiz do projeto:

```bash
mvn clean install -DskipTests
```

### Gerando Imagens Docker

Para gerar uma imagem Docker nativa e otimizada para produção para um microsserviço:

```bash
# O comando de build deve ser executado da raiz para ter o contexto completo
docker build -f ms-consent/Dockerfile -t personal-bank/ms-consent:1.0.0 .
```

### Hospedagem

Um ambiente de produção robusto para esta suite exigiria:

  * **Message Broker:** Um cluster Kafka gerenciado (ex: Confluent Cloud, Aiven, AWS MSK).
  * **Base de Dados:** Uma instância de banco de dados gerenciado (ex: AWS RDS, Google Cloud SQL).
  * **Orquestrador de Containers:** Kubernetes (AKS, EKS, GKE) ou OpenShift.
  * **Cofre de Segredos:** Um serviço como HashiCorp Vault ou equivalentes na nuvem para gerenciar chaves de criptografia e credenciais de produção.
  * **CI/CD:** Um pipeline automatizado (ex: GitHub Actions, Jenkins) para orquestrar o build e o deploy.