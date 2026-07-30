package com.kunzel.finance_tracker;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.kunzel.finance_tracker.account.AccountRepository;
import com.kunzel.finance_tracker.category.CategoryRepository;
import com.kunzel.finance_tracker.transaction.TransactionRepository;

/**
 * Teste de fumaça: sobe o contexto Spring uma vez e confirma que os
 * repositórios foram construídos.
 *
 * Não é teste de integração (RNF06/RNF07, V3) — não executa consulta nem valida
 * resposta HTTP. O que ele cobre é o que nenhum teste de service cobre: o Spring
 * Data valida nome de método derivado e sintaxe de @Query na criação do proxy do
 * repositório, durante a subida do contexto. Sem este teste, uma consulta mal
 * escrita derruba a aplicação no boot com a suíte inteira verde.
 *
 * Não apagar. Os testes de service continuam rodando sem contexto (RNF02).
 */
@SpringBootTest
class FinanceTrackerApplicationTests {

  @Autowired
  private AccountRepository accountRepository;

  @Autowired
  private CategoryRepository categoryRepository;

  @Autowired
  private TransactionRepository transactionRepository;

  @Test
  void contextLoads() {
    assertThat(accountRepository).isNotNull();
    assertThat(categoryRepository).isNotNull();
    assertThat(transactionRepository).isNotNull();
  }
}
