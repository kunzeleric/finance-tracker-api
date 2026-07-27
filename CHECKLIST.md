# Checklist — V1 (Fundação)

Progresso de implementação dos requisitos da V1. Marque cada item conforme concluir.

## Entidades

- [x] `Conta` (ex: carteira, banco X, cartão Y)
- [x] `Categoria` (ex: alimentação, transporte, salário)
- [ ] `Transação` (valor, data, categoria, conta) — tipo herdado da categoria

## Requisitos funcionais

### RF01 — Cadastrar conta

- [x] Criar uma conta com nome e saldo inicial
- [x] Validar: nome não pode ser vazio
- [x] Validar: saldo inicial não pode ser negativo

### RF02 — Cadastrar categoria

- [x] Categoria tem nome e tipo (receita ou despesa)
- [x] Não permitir duas categorias com o mesmo nome e mesmo tipo

### RF03 — Cadastrar transação

- [x] Transação tem valor (positivo), data, categoria e conta associados
- [x] Tipo (receita/despesa) derivado da categoria associada
- [ ] Ao criar transação, atualizar saldo da conta conforme tipo da categoria (receita soma, despesa subtrai)

### RF04 — Listar transações

- [x] Filtrar por conta
- [x] Filtrar por categoria
- [ ] Filtrar por período (data inicial/final)

### RF05 — Editar e excluir transação

- [ ] Editar transação recalcula corretamente o saldo da conta
- [ ] Excluir transação recalcula corretamente o saldo da conta

### RF06 — Consultar saldo de uma conta

- [ ] Saldo retornado reflete a soma de todas as transações daquela conta

## Requisitos não-funcionais / processo

### RNF01 — TDD como processo

- [ ] Cada regra (RF01–RF06) tem teste unitário escrito **antes** da implementação no service
- [ ] Todo método de service com lógica condicional tem teste de caminho feliz + caminho de erro

### RNF02 — Isolamento de testes unitários

- [ ] Testes de service rodam sem subir o contexto do Spring
- [ ] Testes de service não acessam banco real (repositories mockados)

### RNF03 — Arquitetura em camadas

- [ ] Separação clara entre Controller, Service, Repository e domínio (entidades/DTOs)
- [ ] Sem lógica de negócio dentro do Controller
