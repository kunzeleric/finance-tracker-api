# Checklist — V1 (Fundação)

Progresso de implementação dos requisitos da V1. Marque cada item conforme concluir.

## Entidades

- [x] `Conta` (ex: carteira, banco X, cartão Y)
- [x] `Categoria` (ex: alimentação, transporte, salário)
- [x] `Transação` (valor, tipo, data, categoria, conta) — tipo próprio da transação

## Requisitos funcionais

### RF01 — Cadastrar conta

- [x] Criar uma conta com nome e saldo inicial
- [x] Validar: nome não pode ser vazio
- [x] Validar: saldo inicial não pode ser negativo

### RF02 — Cadastrar categoria

- [x] Categoria tem nome (sem tipo — é neutra quanto a receita/despesa)
- [x] Não permitir duas categorias com o mesmo nome

### RF03 — Cadastrar transação

- [x] Transação tem valor (positivo), tipo, data, categoria e conta associados
- [x] Tipo (receita/despesa) é atributo próprio da transação e obrigatório
- [x] Ao criar transação, atualizar saldo da conta conforme tipo da transação (receita soma, despesa subtrai)

### RF04 — Listar transações

- [x] Filtrar por conta
- [x] Filtrar por categoria
- [x] Filtrar por período (data inicial/final)

### RF05 — Editar e excluir transação

- [x] Editar transação recalcula corretamente o saldo da conta
- [x] Excluir transação recalcula corretamente o saldo da conta
- [x] Trocar o tipo da transação (receita ↔ despesa) inverte o sinal aplicado ao saldo

### RF06 — Consultar saldo de uma conta

- [x] Saldo retornado reflete a soma de todas as transações daquela conta

## Requisitos não-funcionais / processo

### RNF01 — TDD como processo

- [x] Cada regra (RF01–RF06) tem teste unitário escrito **antes** da implementação no service
- [x] Todo método de service com lógica condicional tem teste de caminho feliz + caminho de erro

### RNF02 — Isolamento de testes unitários

- [x] Testes de service rodam sem subir o contexto do Spring
- [x] Testes de service não acessam banco real (repositories mockados)

### RNF03 — Arquitetura em camadas

- [x] Separação clara entre Controller, Service, Repository e domínio (entidades/DTOs)
- [x] Sem lógica de negócio dentro do Controller
