-- Seed data. Runs on every startup (H2 in-memory resets each restart).
-- Requires: spring.jpa.defer-datasource-initialization=true (so this runs AFTER Hibernate builds the schema).
--
-- NOTE: these are raw INSERTs — they bypass the domain (deposit/withdraw never run).
-- So each account's balances are set MANUALLY:
--   initial_balance = opening balance before tracking (0 here — accounts start empty).
--   current_balance = initial_balance + SUM(signed transactions): INCOME adds, EXPENSE subtracts.
--   'type' lives on the transaction now (not on the category), so it is set per row below.
-- Keep this invariant when editing: current_balance == initial_balance + SUM(signed txns) per account.

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

-- Accounts (current_balance == initial_balance + net of transactions below) --
--   1: 0 + 4500 + 1200 - 350.75 - 1500 = 3849.25
--   2: 0 + 10000                        = 10000.00
--   3: 0 + 500 - 200 - 60               = 240.00
--   4: 0 + 800                          = 800.00
INSERT INTO accounts (id, name, initial_balance, current_balance, creation_date, type) VALUES
  (1, 'Conta Corrente', 0.00,  3849.25, DATE '2026-01-15', 'CHECKING'),
  (2, 'Poupança',       0.00, 10000.00, DATE '2026-01-15', 'SAVINGS'),
  (3, 'Carteira',       0.00,   240.00, DATE '2026-02-01', 'WALLET'),
  (4, 'Investimentos',  0.00,   800.00, DATE '2026-03-10', 'INVESTMENT');

-- Transactions -----------------------------------------------------------
INSERT INTO transactions (id, description, type, amount, date, creation_date, account_id, category_id) VALUES
  -- account 1
  (1, 'Salário mensal',       'INCOME',   4500.00, DATE '2026-07-05', DATE '2026-07-05', 1, 1),
  (2, 'Projeto freelance',    'INCOME',   1200.00, DATE '2026-07-10', DATE '2026-07-10', 1, 2),
  (3, 'Supermercado',         'EXPENSE',   350.75, DATE '2026-07-12', DATE '2026-07-12', 1, 3),
  (4, 'Aluguel',              'EXPENSE',  1500.00, DATE '2026-07-01', DATE '2026-07-01', 1, 4),
  -- account 2
  (5, 'Depósito poupança',    'INCOME',  10000.00, DATE '2026-07-05', DATE '2026-07-05', 2, 1),
  -- account 3
  (6, 'Dinheiro em carteira', 'INCOME',    500.00, DATE '2026-07-03', DATE '2026-07-03', 3, 2),
  (7, 'Gasolina',             'EXPENSE',   200.00, DATE '2026-07-15', DATE '2026-07-15', 3, 5),
  (8, 'Cinema',               'EXPENSE',    60.00, DATE '2026-07-20', DATE '2026-07-20', 3, 6),
  -- account 4
  (9, 'Aporte investimento',  'INCOME',    800.00, DATE '2026-07-08', DATE '2026-07-08', 4, 2);

-- Advance sequences past the manually-inserted ids so app-generated inserts don't collide.
ALTER SEQUENCE account_sequence     RESTART WITH 5;
ALTER SEQUENCE category_sequence    RESTART WITH 7;
ALTER SEQUENCE transaction_sequence RESTART WITH 10;
