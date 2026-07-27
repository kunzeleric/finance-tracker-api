-- Seed data. Runs on every startup (H2 in-memory resets each restart).
-- Requires: spring.jpa.defer-datasource-initialization=true (so this runs AFTER Hibernate builds the schema).

-- Accounts ---------------------------------------------------------------
INSERT INTO accounts (id, name, balance, creation_date, type) VALUES
  (1, 'Conta Corrente', 2500.00, DATE '2026-01-15', 'CHECKING'),
  (2, 'Poupança',      10000.00, DATE '2026-01-15', 'SAVINGS'),
  (3, 'Carteira',        300.00, DATE '2026-02-01', 'WALLET'),
  (4, 'Investimentos',  5000.00, DATE '2026-03-10', 'INVESTMENT');

-- Categories -------------------------------------------------------------
INSERT INTO categories (id, name, type, is_default, creation_date) VALUES
  (1, 'Salário',        'INCOME',  TRUE, DATE '2026-01-01'),
  (2, 'Freelance',      'INCOME',  TRUE, DATE '2026-01-01'),
  (3, 'Alimentação',    'EXPENSE', TRUE, DATE '2026-01-01'),
  (4, 'Moradia',        'EXPENSE', TRUE, DATE '2026-01-01'),
  (5, 'Transporte',     'EXPENSE', TRUE, DATE '2026-01-01'),
  (6, 'Lazer',          'EXPENSE', TRUE, DATE '2026-01-01');

-- Transactions -----------------------------------------------------------
INSERT INTO transactions (id, description, amount, date, creation_date, account_id, category_id) VALUES
  (1, 'Salário mensal',       4500.00, DATE '2026-07-05', DATE '2026-07-05', 1, 1),
  (2, 'Projeto freelance',    1200.00, DATE '2026-07-10', DATE '2026-07-10', 1, 2),
  (3, 'Supermercado',          350.75, DATE '2026-07-12', DATE '2026-07-12', 1, 3),
  (4, 'Aluguel',              1500.00, DATE '2026-07-01', DATE '2026-07-01', 1, 4),
  (5, 'Gasolina',              200.00, DATE '2026-07-15', DATE '2026-07-15', 3, 5),
  (6, 'Cinema',                 60.00, DATE '2026-07-20', DATE '2026-07-20', 3, 6),
  (7, 'Aporte investimento',   800.00, DATE '2026-07-08', DATE '2026-07-08', 4, 2);

-- Advance sequences past the manually-inserted ids so app-generated inserts don't collide.
ALTER SEQUENCE account_sequence     RESTART WITH 5;
ALTER SEQUENCE category_sequence    RESTART WITH 7;
ALTER SEQUENCE transaction_sequence RESTART WITH 8;
