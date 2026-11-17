# API de Transferência Bancária - Banco Digital

Esta é uma API REST para um banco digital, desenvolvida com Spring Boot 3 e Java 21. O objetivo principal é permitir a transferência de valores entre contas e a consulta de movimentações financeiras de forma altamente concorrente, assíncrona, consistente e resiliente a falhas.

---

## 🚀 Como Executar o Projeto

### Pré-requisitos
* **Java 21+**
* **Docker e Docker Compose** (para o RabbitMQ) ou RabbitMQ instalado localmente
* **Maven** (ou o `./mvnw` embutido)

### 1. Iniciar o RabbitMQ via Docker Compose
Na raiz do repositório, execute:
```bash
docker compose up -d
```
* **Broker AMQP:** `localhost:5672`
* **Painel Web (Management):** `http://localhost:15672` (Usuário: `guest`, Senha: `guest`)

### 2. Executar a Aplicação Spring Boot
Acesse a pasta da aplicação e inicie via Maven ou IDE:
```bash
cd transferencia-bancaria-api
.\mvnw.cmd spring-boot:run
```
*(No Linux/macOS: `./mvnw spring-boot:run`)*

A API ficará disponível por padrão em: `http://localhost:8080`

---

## 🏛️ Arquitetura e Decisões de Design

O sistema adota uma arquitetura orientada a eventos e mensageria para gerenciar concorrência e garantir alta resiliência:

```
[Cliente HTTP] 
      │  POST /transacoes (HTTP 202 Accepted com Protocolo UUID)
      ▼
[TransferenciaProducer]
      │  Mensagem JSON
      ▼
[RabbitMQ Exchange: transferencia.exchange]
      │
      ▼
[Fila Principal: transferencia.queue] ──(Falha / Max Retries)──► [DLQ: transferencia.dlq]
      │
      ▼
[TransferenciaConsumer]
      │  Lock Pessimista Ordenado (findByIdForUpdate)
      ▼
[Banco de Dados H2 / Transacao Salva]
      │
      ▼ (After Commit)
[NotificacaoService] (Assíncrono)
```

### Principais Mecanismos de Concorrência e Resiliência:
1. **Desacoplamento e Enfileiramento Assíncrono:**
   * O endpoint `POST /transacoes` valida os parâmetros de entrada e enfileira a solicitação instantaneamente no RabbitMQ, devolvendo **HTTP 202 (Accepted)** com um identificador de protocolo único (`UUID`).
   * Picos de tráfego são amortecidos pela fila sem sobrecarregar o banco de dados.

2. **Mitigação de Deadlock e Integridade Concorrente:**
   * O consumidor obtém registros de contas via **Lock Pessimista** (`PESSIMISTIC_WRITE`) com ordenação determinística de IDs (`Math.min` e `Math.max`), eliminando deadlocks mesmo com consumidores paralelos processando transferências cruzadas.

3. **Política de Retry e Dead Letter Queue (DLQ):**
   * **Erros Transitórios** (ex: timeout temporário de conexão ou contenção de lock): A política de retry é disparada com backoff exponencial (até 3 tentativas).
   * **Erros de Negócio Irrecuperáveis** (ex: saldo insuficiente, conta inexistente): A mensagem é rejeitada sem repetição (`AmqpRejectAndDontRequeueException`) e enviada diretamente para a **Dead Letter Queue (`transferencia.dlq`)**, permitindo auditoria e evitando loops desnecessários.

4. **Notificações Assíncronas Pós-Commit:**
   * Notificações são acionadas estritamente após a conclusão bem-sucedida da transação no banco de dados (`TransactionSynchronization.afterCommit`).

---

## 📡 Endpoints da API

### 1. Enfileirar Transferência
* **Rota:** `POST /transacoes`
* **Status HTTP:** `202 Accepted`
* **Corpo da Requisição (JSON):**
  ```json
  {
    "idContaOrigem": 1,
    "idContaDestino": 2,
    "valorTransferencia": 150.00
  }
  ```
* **Resposta de Sucesso (JSON):**
  ```json
  {
    "protocolo": "f385c490-4d43-46fb-a09c-482470ebfa72",
    "status": "RECEBIDA",
    "mensagem": "Transferência recebida com sucesso e enfileirada para processamento.",
    "dataSolicitacao": "2026-11-17T15:30:00"
  }
  ```

### 2. Consultar Transferência por Protocolo
* **Rota:** `GET /transacoes/protocolo/{protocolo}`
* **Descrição:** Retorna os dados da transferência concluída vinculada ao protocolo gerado. Retorna `404 Not Found` caso ainda esteja em fila/processamento ou não exista.
* **Exemplo de Resposta (JSON):**
  ```json
  {
    "id": 1,
    "protocolo": "f385c490-4d43-46fb-a09c-482470ebfa72",
    "idContaOrigem": 1,
    "nomeContaOrigem": "Vinicius Junior",
    "idContaDestino": 2,
    "nomeContaDestino": "Gabriel Magalhães",
    "dataHora": "2026-11-17T15:30:01",
    "valor": 150.00
  }
  ```

### 3. Consultar Histórico de Movimentações
* **Rota:** `GET /transacoes/historico/{contaId}`
* **Descrição:** Retorna o histórico de transferências enviadas e recebidas pela conta.
* **Exemplo de Resposta (JSON):**
  ```json
  [
    {
      "id": 1,
      "protocolo": "f385c490-4d43-46fb-a09c-482470ebfa72",
      "idContaOrigem": 1,
      "nomeContaOrigem": "Vinicius Junior",
      "idContaDestino": 2,
      "nomeContaDestino": "Gabriel Magalhães",
      "dataHora": "2026-11-17T15:30:01",
      "valor": 150.00
    }
  ]
  ```

---

## 👥 Dados de Contas Pré-carregadas

O sistema inicializa um banco de dados em memória (H2) com 4 contas para testes:

| ID | Cliente | Saldo Inicial |
| :---: | :--- | :--- |
| **1** | Vinicius Junior | R$ 5.000,00 |
| **2** | Gabriel Magalhães | R$ 1.500,00 |
| **3** | Alisson Becker | R$ 3.000,00 |
| **4** | Lucas Paquetá | R$ 4.000,00 |

---

## 🧪 Testes e Documentação

* **Suíte Completa de Testes:** Execute os testes unitários e de integração com cobertura via JaCoCo:
  ```bash
  .\mvnw.cmd test
  ```
  *(Inclui testes de integridade concorrente com lock pessimista, produtor, consumidor com DLQ, controller e services).*
* **Swagger / OpenAPI:** `http://localhost:8080/swagger-ui.html`
* **Console H2 Database:** `http://localhost:8080/h2-console` (JDBC URL: `jdbc:h2:mem:transferenciadb`, Usuário: `sa`, Senha em branco).
* **CI/CD (GitHub Actions):** Pipeline automatizado (`.github/workflows/ci.yml`) que compila o projeto, executa toda a suíte de testes e gera o relatório de cobertura JaCoCo (com resumo no Job Summary e upload do artefato HTML/XML) a cada push ou pull request para a branch `main`.
