# API de Transferência Bancária - Banco Digital

Esta é uma API REST simplificada para um banco digital, desenvolvida como parte do desafio técnico da Compass.UOL. O objetivo principal é permitir a transferência de valores entre contas e a consulta de movimentações financeiras, garantindo consistência, resiliência e performance.

---

## Como Executar o Projeto

1. Certifique-se de ter o **Java 17+** (ou a versão utilizada no seu projeto) e o **Maven** instalados.
2. Clone o repositório e acesse a pasta raiz do projeto.
3. Execute a aplicação através da sua IDE de preferência ou pelo terminal executando a classe principal:
   `TransferenciaBancariaApiApplication`
4. A API ficará disponível por padrão em: `http://localhost:8080`

---

## Arquitetura e Decisões de Design

O sistema foi projetado seguindo os princípios do **SOLID**, com foco no **Princípio da Responsabilidade Única (SRP)**, de forma a garantir alta coesão e baixo acoplamento. A estrutura de pacotes foi organizada da seguinte forma:

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
    "contaOrigemId": 1,
    "contaDestinoId": 2,
    "valor": 150.00
  }
  ```

### 2. Consultar Histórico de Transações
* **Rota:** `GET /transacoes/historico/{contaId}`
* **Descrição:** Retorna a lista de movimentações financeiras envolvendo a conta informada.

---

## 💾 Dados das Contas Pré-carregadas

Para fins de simulação e teste, o sistema inicia com uma base pré-carregada de 4 contas:

| ID | Cliente | Saldo Inicial |
| :---: | :--- | :--- |
| **1** | Vinicius Junior | R$ 5.000,00 |
| **2** | Gabriel Magalhães | R$ 1.500,00 |
| **3** | Alisson Becker | R$ 3.000,00 |
| **4** | Lucas Paquetá | R$ 4.000,00 |

---

## Testes e Documentação

* **Testes Unitários:** Implementados para garantir a cobertura das principais regras de negócio da aplicação (como fluxo de transferência e validações de saldo). Para rodar os testes, utilize o comando:
  ```bash
  mvn test
  ```
* **Documentação (Swagger/OpenAPI):** A documentação interativa das rotas pode ser acessada localmente através do link: `http://localhost:8080/swagger-ui.html`
* **Notificações:** Após o sucesso de cada transferência, o sistema dispara de forma resiliente uma simulação de notificação para o cliente envolvido.
