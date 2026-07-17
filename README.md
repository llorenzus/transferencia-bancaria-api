# API de Transferência Bancária - Banco Digital

Esta é uma API REST simplificada para um banco digital, desenvolvida como parte do desafio técnico. O objetivo principal é permitir a transferência de valores entre contas e a consulta de movimentações financeiras, garantindo consistência, resiliência e performance.

---

## Como Executar o Projeto

1. Certifique-se de ter o **Java 21+** e o **Maven** instalados.
2. Clone o repositório e acesse a pasta raiz do projeto.
3. Execute a aplicação através da sua IDE de preferência ou pelo terminal executando a classe principal:
   `TransferenciaBancariaApiApplication`
4. A API ficará disponível por padrão em: `http://localhost:8080`

---

## Arquitetura e Decisões de Design

O sistema foi projetado seguindo os princípios do **SOLID**, com foco no **Princípio da Responsabilidade Única (SRP)**. A estrutura de pacotes foi organizada da seguinte forma:

* **`config`**: Responsável pela configuração inicial do sistema e pelo pré-carregamento dos dados em memória.
* **`controller`**: Define os endpoints da API REST, realiza a validação dos dados de entrada (Jakarta Validation) e centraliza o tratamento global de exceções.
* **`domain.model`**: Contém as entidades de negócio (`Conta` e `Transacao`), responsáveis pelo mapeamento e consistência dos dados.
* **`domain.exception`**: Centraliza as exceções customizadas do negócio (ex: saldo insuficiente, conta não encontrada).
* **`service`**: Camada principal responsável pelas regras de negócio, validações de saldo, concorrência e simulação do envio de notificações pós-transferência.
* **`repository`**: Interface que implementa a conexão e a persistência de dados tanto para contas quanto para transações.

---

## Endpoints da API

A aplicação disponibiliza os seguintes endpoints principais:

### 1. Realizar Transferência
* **Rota:** `POST /transacoes`
* **Corpo da Requisição (JSON):**
  ```json
  {
    "idContaOrigem": 1,
    "idContaDestino": 2,
    "valorTransferencia": 150.00
  }
  ```

### 2. Consultar Histórico de Transações
* **Rota:** `GET /transacoes/historico/{contaId}`
* **Descrição:** Retorna a lista de movimentações financeiras envolvendo a conta informada. Os saldos atuais das contas envolvidas são ocultados por questões de privacidade.
* **Exemplo de Resposta (JSON):**
  ```json
  [
    {
      "id": 1,
      "idContaOrigem": 1,
      "nomeContaOrigem": "Vinicius Junior",
      "idContaDestino": 2,
      "nomeContaDestino": "Gabriel Magalhães",
      "dataHora": "2026-07-16T21:00:00",
      "valor": 150.00
    }
  ]
  ```

---

## Dados das Contas Pré-carregadas

Para fins de simulação e teste, o sistema inicia com uma base pré-carregada de 4 contas:

| ID | Cliente | Saldo Inicial |
| :---: | :--- | :--- |
| **1** | Vinicius Junior | R$ 5.000,00 |
| **2** | Gabriel Magalhães | R$ 1.500,00 |
| **3** | Alisson Becker | R$ 3.000,00 |
| **4** | Lucas Paquetá | R$ 4.000,00 |

---

## Testes e Documentação

* **Testes Unitários:** Implementados para garantir a cobertura das principais regras de negócio da aplicação (como fluxo de transferência e validações de saldo). 
* **Documentação (Swagger/OpenAPI):** A documentação interativa das rotas pode ser acessada localmente através do link: `http://localhost:8080/swagger-ui.html`
* **Notificações:** Após o sucesso de cada transferência, o sistema dispara de forma resiliente uma simulação de notificação para o cliente envolvido.
