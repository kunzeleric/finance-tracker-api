package com.kunzel.finance_tracker.category;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.kunzel.finance_tracker.shared.exceptions.BusinessRuleException;
import com.kunzel.finance_tracker.shared.exceptions.NotFoundException;
import com.kunzel.finance_tracker.shared.exceptions.ValidationException;
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

  public Category createCategory(String name, CategoryType type, String color) {
    assertNameAvailable(name, null);
    return categoryRepository.save(Category.createCustom(name, type, color));
  }

  public Category createDefaultCategory(String name, CategoryType type, String color) {
    assertNameAvailable(name, null);
    return categoryRepository.save(Category.createDefault(name, type, color));
  }

  public Category updateCategory(Long categoryId, String name, String color, CategoryType type) {
    Category categoryToUpdate = getCategoryById(categoryId);

    assertNameAvailable(name, categoryId);

    // As regras de categoria padrão (só cor é editável) vivem na entidade.
    categoryToUpdate.update(name, color, type);
    return categoryRepository.save(categoryToUpdate);
  }

  @Transactional
  public void removeCategory(Long categoryId, Long reassignToId) {
    Category categoryToRemove = getCategoryById(categoryId);

    if (categoryToRemove.isDefault()) {
      throw new BusinessRuleException("Categoria padrão não pode ser deletada");
    }

    if (reassignToId != null) {
      Category reassignTarget = resolveReassignTarget(categoryToRemove, reassignToId);
      transactionRepository.reassignCategory(categoryId, reassignTarget);
    } else {
      transactionRepository.deleteByCategoryId(categoryId);
    }

    categoryRepository.delete(categoryToRemove);
  }

  private Category resolveReassignTarget(Category categoryToRemove, Long reassignToId) {
    if (reassignToId.equals(categoryToRemove.getId())) {
      throw new ValidationException("Categoria de destino não pode ser a mesma que está sendo removida");
    }

    Category reassignTarget = getCategoryById(reassignToId);

    if (reassignTarget.getType() != categoryToRemove.getType()) {
      throw new BusinessRuleException(
          "Categoria de destino precisa ser do mesmo tipo (" + categoryToRemove.getType() + ")");
    }

    return reassignTarget;
  }

  private void assertNameAvailable(String name, Long excludeId) {
    if (name == null) {
      return;
    }

    categoryRepository.findExistingCategoryByName(name)
        .filter(existing -> !existing.getId().equals(excludeId))
        .ifPresent(existing -> {
          throw new BusinessRuleException("Você não pode ter duas categorias com mesmo nome");
        });
  }
}
