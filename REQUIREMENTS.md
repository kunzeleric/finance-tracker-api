# Personal Finance Tracker — Requirements

Domínio: controle financeiro pessoal — transações, categorias, contas e orçamento.

---

## V1 — Fundação (CRUD + regras básicas)

**Objetivo:** modelar o domínio central e já nascer com testes unitários guiando o design (TDD).

### Entidades

- `Conta` (ex: carteira, banco X, cartão Y)
- `Categoria` (ex: alimentação, transporte, salário)
- `Transação` (valor, tipo, data, categoria, conta) — o tipo (receita/despesa) é atributo da própria transação

### Requisitos funcionais

**RF01 — Cadastrar conta**

- Critério: deve ser possível criar uma conta com nome e saldo inicial.
- Critério: nome não pode ser vazio; saldo inicial não pode ser negativo.

**RF02 — Cadastrar categoria**

- Critério: categoria tem apenas nome — é neutra quanto a receita/despesa.
- Critério: não deve permitir duas categorias com o mesmo nome.

**RF03 — Cadastrar transação**

- Critério: transação tem valor (positivo), tipo (receita/despesa), data, categoria e conta associados.
- Critério: o tipo é atributo da própria transação e é obrigatório. A categoria não o restringe — a mesma categoria pode receber transações de receita e de despesa.
- Critério: ao criar a transação, o saldo da conta associada deve ser atualizado conforme o tipo da transação (receita soma, despesa subtrai).

**RF04 — Listar transações**

- Critério: deve ser possível listar transações filtrando por conta, por categoria, e por período (data inicial/final).

**RF05 — Editar e excluir transação**

- Critério: editar ou excluir uma transação deve recalcular corretamente o saldo da conta associada.
- Critério: editar o tipo de uma transação (receita ↔ despesa) deve inverter o sinal aplicado ao saldo da conta.

**RF06 — Consultar saldo de uma conta**

- Critério: o saldo retornado deve refletir a soma de todas as transações daquela conta.

### Requisitos não-funcionais / processo

**RNF01 — TDD como processo**

- Critério: cada regra de negócio (RF01 a RF06) deve ter seu teste unitário escrito **antes** da implementação da lógica correspondente no service.
- Critério: cobertura mínima informal — todo método de service com lógica condicional (if/else de validação, cálculo de saldo) deve ter ao menos um teste cobrindo o caminho feliz e um cobrindo o caminho de erro.

**RNF02 — Isolamento de testes unitários**

- Critério: os testes de service devem rodar sem subir o contexto do Spring e sem acessar banco de dados real (usar mocks para repositories).

**RNF03 — Arquitetura em camadas**

- Critério: separação clara entre Controller, Service, Repository e camada de domínio (entidades/DTOs), sem lógica de negócio dentro do Controller.

### Stack de testes sugerida para V1

- **JUnit 5** (padrão de fato no ecossistema Java)
- **Mockito** para mockar repositories nos testes de service
- **AssertJ** para assertions mais expressivas (opcional, mas muito usado junto)

---

## V2 — Regras de negócio mais ricas (Orçamento e Alertas)

**Objetivo:** adicionar complexidade que force decisões de design (SOLID na prática).

### Requisitos funcionais

**RF07 — Definir orçamento mensal por categoria**

- Critério: deve ser possível definir um limite de gasto mensal para uma categoria.
- Critério: o limite considera apenas as transações de tipo despesa (EXPENSE) daquela categoria — a categoria em si não tem tipo.

**RF08 — Alertar estouro de orçamento**

- Critério: ao registrar uma transação de tipo despesa (EXPENSE), o sistema deve indicar se aquela categoria ultrapassou o orçamento do mês.
- Critério: deve ser possível ter mais de uma "forma" de alerta (ex: só sinalizar no retorno da API vs. logar um aviso) sem alterar a lógica de cálculo do estouro — aqui é onde vale a pena isolar essa parte por trás de uma interface.

**RF09 — Relatório agregado**

- Critério: deve ser possível obter o total de despesas e receitas agrupado por categoria, dentro de um período.
- Critério: o agrupamento é por (categoria, tipo) — como a categoria não tem tipo, a mesma categoria pode aparecer nos dois lados do relatório.

**RF10 — Transações recorrentes**

- Critério: deve ser possível marcar uma transação como recorrente (mensal) e o sistema deve gerar as próximas ocorrências automaticamente (ou sob demanda, via endpoint).

### Requisitos não-funcionais / processo

**RNF04 — TDD mantido**

- Critério: mesma disciplina da V1 — teste unitário antes da lógica, para RF07 a RF10.

**RNF05 — Extensibilidade validada por teste**

- Critério: a lógica de "estratégia de alerta" (RF08) deve poder ser testada isoladamente, sem depender da lógica de cálculo de orçamento.

---

## V3 — Testes de integração + persistência real

**Objetivo:** validar que as camadas conversam corretamente com banco de dados de verdade (ou próximo disso).

### Requisitos funcionais

- Nenhuma nova regra de negócio obrigatória — foco em consolidar o que já existe com testes de ponta a ponta.
- Opcional: **RF11 — Importação de extrato via CSV**, se quiser uma motivação real pra testar integração com I/O externo.

### Requisitos não-funcionais / processo

**RNF06 — Testes de integração de repository**

- Critério: os repositories (queries customizadas, se houver) devem ter testes rodando contra um banco real ou equivalente (H2 em memória para começar; Testcontainers com Postgres depois).

**RNF07 — Testes de integração de API (end-to-end)**

- Critério: os principais fluxos (criar transação → saldo atualizado, criar transação → orçamento estourado) devem ter ao menos um teste subindo o contexto Spring completo (`@SpringBootTest`) e validando a resposta HTTP.

**RNF08 — Separação clara entre unitário e integração**

- Critério: deve ser possível rodar só os testes unitários (rápidos) separadamente dos testes de integração (mais lentos) — geralmente feito por convenção de nome/pacote ou tags do JUnit 5.

### Stack sugerida para V3

- **H2** (banco em memória) para começar
- **Testcontainers** (padrão de mercado hoje para integração com banco real em container) quando quiser subir o nível
- **@SpringBootTest**, **@DataJpaTest** (Spring Boot Test)

---

## V4 — Autenticação e Autorização

**Objetivo:** cada usuário só vê e mexe nos próprios dados.

### Requisitos funcionais

**RF12 — Cadastro e login de usuário**

- Critério: deve ser possível criar uma conta de usuário (com senha) e autenticar-se.
- Critério: senha nunca deve ser armazenada em texto plano.

**RF13 — Autorização por dono do recurso**

- Critério: contas, categorias e transações passam a pertencer a um usuário; um usuário não pode ler ou modificar dados de outro usuário.

**RF14 — Proteção de endpoints**

- Critério: todos os endpoints de transação/conta/categoria/orçamento devem exigir autenticação.

### Requisitos não-funcionais / processo

**RNF09 — Testes de segurança**

- Critério: deve haver testes garantindo que um usuário autenticado como A não consegue acessar dados do usuário B (ex: retorno 403/404).

### Opções de lib/framework para autenticação

| Opção                                                                                        | Quando faz sentido                                                                                            |
| -------------------------------------------------------------------------------------------- | ------------------------------------------------------------------------------------------------------------- |
| **Spring Security + JWT** (com `jjwt` ou `nimbus-jose-jwt`)                                  | O mais comum no mercado para APIs stateless. Ótimo pra aprender o que tá por baixo do pano.                   |
| **Spring Security + Sessions**                                                               | Mais simples de montar, mas menos usado em APIs modernas. Bom se quiser algo mais direto antes de ir pro JWT. |
| **OAuth2 / OpenID Connect** (Spring Security OAuth2 Client, ou provedor como Auth0/Keycloak) | Padrão de mercado quando envolve login social ou múltiplos serviços.                                          |
| **Keycloak** (self-hosted)                                                                   | Se quiser aprender a integrar com um Identity Provider de verdade, sem implementar login do zero.             |

Sugestão: comece com **Spring Security + JWT manual** — é o caminho que mais ensina e o mais citado em vagas de Java no mercado.
