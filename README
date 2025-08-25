# Personal Bank Suite

*Última atualização: 25 de Agosto de 2025, 15:26 (Catanduva, SP, Brazil)*

O **Personal Bank Suite** é um ecossistema de microsserviços projetado para agregar e processar dados financeiros de múltiplas instituições através do padrão Open Finance Brasil. A arquitetura é moderna, escalável e orientada a eventos, utilizando Quarkus, Java, Kafka e Docker.

## Sumário

  * [Arquitetura](#arquitetura)
    * [Visão Geral](#visão-geral)
    * [Estrutura dos Módulos](#estrutura-dos-módulos)
  * [Ambiente de Desenvolvimento](#ambiente-de-desenvolvimento)
    * [Pré-requisitos](#pré-requisitos)
    * [Fluxo 1 (com VS Code)](#fluxo-1)
    * [Fluxo 2 (sem VS Code)](#fluxo-2)
  * [Como Consumir os Microsserviços](#como-consumir-os-microsserviços)
    * [O Padrão Pub/Sub](#o-padrão-pubsub)
    * [Exemplo: Carga Inicial](#exemplo-carga-inicial)
    * [Exemplo: Atualização Contínua](#exemplo-atualização-contínua)
  * [Build e Deploy em Produção](#build-e-deploy-em-produção)
    * [Construindo o Projeto](#construindo-o-projeto)
    * [Gerando Imagens Docker](#gerando-imagens-docker)
    * [Hospedagem](#hospedagem)

-----

## Arquitetura

### Visão Geral

Este projeto é um **sistema distribuído e orientado a eventos**. Os microsserviços são largamente desacoplados e se comunicam de forma assíncrona através de um message broker **Apache Kafka**.

O fluxo principal é:

1.  Um evento é publicado em um tópico do Kafka (ex: "um novo consentimento de usuário foi concedido").
2.  O microsserviço relevante (ex: `ms-transactions`) consome a mensagem.
3.  O serviço então busca os dados na API Open Finance do banco correspondente.
4.  Os dados são processados e armazenados internamente.

### Estrutura dos Módulos

O projeto é um **Maven Multi-Módulo**, organizado da seguinte forma:

  * **`Personal-Bank-Suite/` (Raiz)**

      * Contém o `pom.xml` "Pai" que gerencia as dependências e versões para todos os módulos.
      * Contém a configuração do ambiente de desenvolvimento (`.devcontainer`, `docker-compose.yml`).
      * Contém a configuração centralizada das instituições (`config/banks.yml`).

  * **`common-library/`**

      * Uma biblioteca compartilhada (`.jar`) que contém todo o código comum.
      * **Responsabilidades:** Lógica de configuração, criação dinâmica de clientes REST (`ApiClientProvider`), DTOs e eventos genéricos.

  * **`ms-transactions/`**

      * Microsserviço focado em dados de contas correntes.
      * **Responsabilidades:** Consumir eventos para buscar e processar o extrato de transações.

  * **`ms-credit-cards/`**

      * Microsserviço focado em dados de cartões de crédito.
      * **Responsabilidades:** Consumir eventos para buscar dados de contas de cartão, faturas e os lançamentos de cada fatura.

  * **`ms-investments/`**

      * Microsserviço focado em dados de investimentos.
      * **Responsabilidades:** Consumir eventos para buscar a posição consolidada de investimentos (Renda Fixa, Fundos, etc.).

-----

## Ambiente de Desenvolvimento

O ambiente é padronizado e reproduzível. Recomendamos o uso do **VS Code com Dev Containers** para a experiência mais integrada, mas um fluxo com **Docker Compose puro** também é totalmente suportado.

### Pré-requisitos
* [Git](https://git-scm.com/)
* [Docker](https://www.docker.com/) e [Docker Compose](https://docs.docker.com/compose/)
* Para o fluxo recomendado: [Visual Studio Code](https://code.visualstudio.com/) e a extensão [Dev Containers](https://marketplace.visualstudio.com/items?itemName=ms-vscode-remote.remote-containers).

---
### Fluxo 1: Com VS Code e Dev Containers (Recomendado)

1.  **Clone o Repositório:**
    ```bash
    git clone <url-do-seu-repositorio> Personal-Bank-Suite
    cd Personal-Bank-Suite
    ```
2.  **Abra no VS Code:**
    Abra a pasta `Personal-Bank-Suite` no VS Code. Uma notificação aparecerá no canto inferior direito.
    
3.  **Inicie o Dev Container:**
    Clique em **"Reopen in Container"**. O VS Code irá construir a imagem, iniciar o container e conectar-se a ele. Este processo é automatizado:
    * O `postCreateCommand` irá executar `mvn clean install` para construir o projeto pela primeira vez.
    * O `postStartCommand` irá executar `docker-compose up -d` para iniciar o Kafka.

4.  **Execute um Microsserviço:**
    Abra um terminal integrado no VS Code (`Ctrl` + ` ` `). Este terminal já está *dentro* do container.
    ```bash
    # Para rodar o serviço de transações
    cd ms-transactions
    ./mvnw quarkus:dev
    ```

---
### Fluxo 2: Com Docker Compose Puro (Alternativa via Terminal)

Este fluxo é ideal para quem não usa o VS Code ou prefere controlar o ambiente diretamente pelo terminal (seja no Linux ou Windows com PowerShell/CMD).

1.  **Clone o Repositório:**
    ```bash
    git clone <url-do-seu-repositorio> Personal-Bank-Suite
    cd Personal-Bank-Suite
    ```

2.  **Inicie o Ambiente Completo:**
    Este comando utiliza dois arquivos: `docker-compose.yml` (para a infraestrutura, como o Kafka) e `docker-compose.dev.yml` (para o container de desenvolvimento).
    ```bash
    docker-compose -f docker-compose.yml -f docker-compose.dev.yml up -d --build
    ```
    * A flag `--build` é importante na primeira vez para construir a imagem do zero.
    * O `entrypoint.sh` configurado no `Dockerfile` irá executar o `mvn clean install` automaticamente apenas na primeira inicialização.

3.  **Acesse o Terminal do Ambiente:**
    Para obter um shell dentro do container de desenvolvimento, execute:
    ```bash
    docker-compose exec develop bash
    ```

4.  **Execute um Microsserviço:**
    Uma vez dentro do shell do container, o processo é o mesmo:
    ```bash
    # Para rodar o serviço de transações
    cd ms-transactions
    ./mvnw quarkus:dev
    ```
5.  **Pare o Ambiente:**
    Quando terminar de trabalhar, para parar e remover todos os containers, execute na raiz do projeto:
    ```bash
    docker-compose -f docker-compose.yml -f docker-compose.dev.yml down
    ```
## Como Consumir os Microsserviços

### O Padrão Pub/Sub

Esses serviços não expõem APIs para serem chamados. Para "usá-los", você deve **publicar uma mensagem (um evento) em um tópico do Kafka**.

### Exemplo: Carga Inicial

Para instruir o `ms-credit-cards` a buscar todos os dados de um novo cartão conectado do Bradesco:

  * **Tópico Kafka:** `consentimentos.carga-inicial.cartoes`
  * **Payload da Mensagem (JSON):**
    ```json
    {
      "accountId": "uuid-da-conta-cartao-no-bradesco",
      "userToken": "token-oauth2-do-usuario",
      "institutionKey": "BRADESCO"
    }
    ```

### Exemplo: Atualização Contínua

Para instruir o `ms-investments` a verificar apenas a Renda Fixa de um usuário no Itaú:

  * **Tópico Kafka:** `investimentos.atualizacoes.ITAU`
  * **Payload da Mensagem (JSON):**
    ```json
    {
      "accountId": "uuid-da-conta-investimento-no-itau",
      "userToken": "token-oauth2-do-usuario",
      "institutionKey": "ITAU",
      "type": "BANK_FIXED_INCOME"
    }
    ```

-----

## Build e Deploy em Produção

### Construindo o Projeto

Para compilar e empacotar todos os módulos, execute o seguinte comando na raiz do projeto, de dentro do Dev Container:

```bash
mvn clean install -DskipTests
```

### Gerando Imagens Docker

Para gerar uma imagem Docker nativa e otimizada para produção para um microsserviço:

```bash
# Navegue para a pasta do microsserviço
cd ms-transactions

# Execute o build com o perfil 'native'
./mvnw package -Pnative -Dquarkus.container-image.build=true
```

### Hospedagem

Um ambiente de produção robusto para esta suite exigiria:

  * **Message Broker:** Um cluster Kafka gerenciado (ex: Confluent Cloud, Aiven, AWS MSK).
  * **Orquestrador de Containers:** Kubernetes (AKS, EKS, GKE) ou OpenShift para gerenciar o ciclo de vida dos containers dos microsserviços.
  * **CI/CD:** Um pipeline automatizado (ex: GitHub Actions, Jenkins) para construir, testar e implantar os serviços.
  * **Configuração e Segredos:** Um cofre para gerenciar segredos e configurações de produção (ex: HashiCorp Vault, AWS Secrets Manager).