-- Seed data. Runs on every startup (H2 in-memory resets each restart).
-- Requires: spring.jpa.defer-datasource-initialization=true (so this runs AFTER Hibernate builds the schema).
--
-- NOTE: accounts store only opening_balance. The current balance is DERIVED at
-- read time as opening_balance + SUM(signed transactions), so there is no stored
-- balance to keep in sync here. INCOME/EXPENSE comes from the transaction's
-- CATEGORY, not from the transaction itself.

-- Categories -------------------------------------------------------------
-- 'type' classifica a categoria (INCOME/EXPENSE). Cada transação abaixo usa uma
-- categoria coerente com o próprio 'type' — mantenha isso ao editar.
INSERT INTO categories (id, name, type, color, is_default, creation_date) VALUES
  (1, 'Salário',      'INCOME',  '#22C55E', TRUE, DATE '2026-01-01'),
  (2, 'Freelance',    'INCOME',  '#14B8A6', TRUE, DATE '2026-01-01'),
  (3, 'Alimentação',  'EXPENSE', '#F97316', TRUE, DATE '2026-01-01'),
  (4, 'Moradia',      'EXPENSE', '#8B5CF6', TRUE, DATE '2026-01-01'),
  (5, 'Transporte',   'EXPENSE', '#3B82F6', TRUE, DATE '2026-01-01'),
  (6, 'Lazer',        'EXPENSE', '#EC4899', TRUE, DATE '2026-01-01');

-- Accounts -----------------------------------------------------------------
-- Saldos derivados que a API vai devolver, dadas as transações abaixo:
--   1: 0 + 4500 + 1200 - 350.75 - 1500 = 3849.25
--   2: 0 + 10000                        = 10000.00
--   3: 0 + 500 - 200 - 60               = 240.00
--   4: 0 + 800                          = 800.00
INSERT INTO accounts (id, name, opening_balance, color, institution, creation_date, type) VALUES
  (1, 'Conta Corrente', 0.00, '#7C3AED', 'Nubank',      DATE '2026-01-15', 'CHECKING'),
  (2, 'Poupança',       0.00, '#0EA5E9', 'Caixa',       DATE '2026-01-15', 'SAVINGS'),
  (3, 'Carteira',       0.00, '#84CC16', NULL,          DATE '2026-02-01', 'WALLET'),
  (4, 'Investimentos',  0.00, '#F59E0B', 'XP',          DATE '2026-03-10', 'INVESTMENT');

-- Transactions -----------------------------------------------------------
-- O sinal de cada linha vem do 'type' da category_id referenciada:
--   categorias 1-2 são INCOME, categorias 3-6 são EXPENSE.
INSERT INTO transactions (id, description, amount, date, creation_date, account_id, category_id) VALUES
  -- account 1
  (1, 'Salário mensal',        4500.00, DATE '2026-07-05', DATE '2026-07-05', 1, 1),
  (2, 'Projeto freelance',     1200.00, DATE '2026-07-10', DATE '2026-07-10', 1, 2),
  (3, 'Supermercado',           350.75, DATE '2026-07-12', DATE '2026-07-12', 1, 3),
  (4, 'Aluguel',               1500.00, DATE '2026-07-01', DATE '2026-07-01', 1, 4),
  -- account 2
  (5, 'Depósito poupança',    10000.00, DATE '2026-07-05', DATE '2026-07-05', 2, 1),
  -- account 3
  (6, 'Dinheiro em carteira',   500.00, DATE '2026-07-03', DATE '2026-07-03', 3, 2),
  (7, 'Gasolina',               200.00, DATE '2026-07-15', DATE '2026-07-15', 3, 5),
  (8, 'Cinema',                  60.00, DATE '2026-07-20', DATE '2026-07-20', 3, 6),
  -- account 4
  (9, 'Aporte investimento',    800.00, DATE '2026-07-08', DATE '2026-07-08', 4, 2);

-- Advance sequences past the manually-inserted ids so app-generated inserts don't collide.
ALTER SEQUENCE account_sequence     RESTART WITH 5;
ALTER SEQUENCE category_sequence    RESTART WITH 7;
ALTER SEQUENCE transaction_sequence RESTART WITH 10;
