package com.kunzel.finance_tracker.category;

import java.util.List;

import org.springframework.stereotype.Service;

import com.kunzel.finance_tracker.shared.exceptions.BusinessRuleException;
import com.kunzel.finance_tracker.shared.exceptions.NotFoundException;
import com.kunzel.finance_tracker.transaction.TransactionRepository;

@Service
public class CategoryService {
  private final CategoryRepository categoryRepository;
  private final TransactionRepository transactionRepository;

  public CategoryService(CategoryRepository categoryRepository, TransactionRepository transactionRepository) {
    this.categoryRepository = categoryRepository;
    this.transactionRepository = transactionRepository;
  }

  public Category getCategoryById(Long categoryId) {
    return categoryRepository.findById(categoryId).orElseThrow(() -> new NotFoundException(categoryId, "CATEGORIA"));
  }

  public List<Category> getAllCategories() {
    return categoryRepository.findAll();
  }

  public Category createCategory(String name) {
    assertNameAvailable(name, null);
    return categoryRepository.save(Category.createCustom(name));
  }

  public Category createDefaultCategory(String name) {
    assertNameAvailable(name, null);
    return categoryRepository.save(Category.createDefault(name));
  }

  public Category updateCategory(Long categoryId, String name) {
    Category categoryToUpdate = getCategoryById(categoryId);

    if (categoryToUpdate.isDefault()) {
      throw new BusinessRuleException("Categoria padrão não pode ser atualizada");
    }

    assertNameAvailable(name, categoryId);

    categoryToUpdate.update(name);
    return categoryRepository.save(categoryToUpdate);
  }

  public void removeCategory(Long categoryId) {
    Category categoryToRemove = getCategoryById(categoryId);

    if (categoryToRemove.isDefault()) {
      throw new BusinessRuleException("Categoria padrão não pode ser deletada");
    }

    if (transactionRepository.existsByCategoryId(categoryId)) {
      throw new BusinessRuleException("Categoria com lançamentos registrados não pode ser removida");
    }

    categoryRepository.delete(categoryToRemove);
  }

  private void assertNameAvailable(String name, Long excludeId) {
    categoryRepository.findExistingCategoryByName(name)
        .filter(existing -> !existing.getId().equals(excludeId))
        .ifPresent(existing -> {
          throw new BusinessRuleException("Você não pode ter duas categorias com mesmo nome");
        });
  }
}
