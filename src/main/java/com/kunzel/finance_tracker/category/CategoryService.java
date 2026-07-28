package com.kunzel.finance_tracker.category;

import java.util.List;

import org.springframework.stereotype.Service;

import com.kunzel.finance_tracker.shared.exceptions.NotFoundException;

@Service
public class CategoryService {
  private final CategoryRepository categoryRepository;

  public CategoryService(CategoryRepository categoryRepository) {
    this.categoryRepository = categoryRepository;
  }

  public Category getCategoryById(Long categoryId) {
    return categoryRepository.findById(categoryId).orElseThrow(() -> new NotFoundException(categoryId, "CATEGORIA"));
  }

  public List<Category> getAllCategories() {
    return categoryRepository.findAll();
  }

  public Category createCategory(String name, CategoryType type) {
    assertNameTypeAvailable(name, type, null);
    return categoryRepository.save(Category.createCustom(name, type));
  }

  public Category createDefaultCategory(String name, CategoryType type) {
    assertNameTypeAvailable(name, type, null);
    return categoryRepository.save(Category.createDefault(name, type));
  }

  public Category updateCategory(Long categoryId, String name, CategoryType type) {
    Category categoryToUpdate = getCategoryById(categoryId);
    assertNameTypeAvailable(name, type, categoryId);

    categoryToUpdate.updateDetails(name, type);
    return categoryRepository.save(categoryToUpdate);
  }

  public void removeCategory(Long categoryId) {
    Category categoryToRemove = getCategoryById(categoryId);
    categoryRepository.delete(categoryToRemove);
    // TODO: fazer um cascade delete em TRANSACTIONS com mesmo category_id quando
    // uma categoria for removida
  }

  private void assertNameTypeAvailable(String name, CategoryType type, Long excludeId) {
    categoryRepository.findExistingCategoryByNameAndType(name, type)
        .filter(existing -> !existing.getId().equals(excludeId))
        .ifPresent(existing -> {
          throw new IllegalArgumentException("Você não pode ter duas categorias com mesmo nome e tipo.");
        });
  }
}
