package com.kunzel.finance_tracker.category;

import java.util.List;
import java.util.Optional;

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
    Optional<Category> existingCategory = categoryRepository.findExistingCategoryByNameAndType(name, type);

    if (existingCategory.isPresent()) {
      throw new IllegalArgumentException("Você não pode ter duas categorias com mesmo nome e tipo.");
    }

    return categoryRepository.save(Category.createCustom(name, type));
  }

  public Category createDefaultCategory(String name, CategoryType type) {
    Optional<Category> existingCategory = categoryRepository.findExistingCategoryByNameAndType(name, type);

    if (existingCategory.isPresent()) {
      throw new IllegalArgumentException("Você não pode ter duas categorias com mesmo nome e tipo.");
    }

    return categoryRepository.save(Category.createDefault(name, type));
  }

  public Category updateCategory(Long categoryId, String name, CategoryType type) {
    Optional<Category> existingCategory = categoryRepository.findExistingCategoryByNameAndType(name, type);

    if (existingCategory.isPresent()) {
      throw new IllegalArgumentException("Você não pode ter duas categorias com mesmo nome e tipo.");
    }

    Category categoryToUpdate = categoryRepository.getReferenceById(categoryId);
    categoryToUpdate.updateDetails(name, type);
    return categoryToUpdate;
  }

  public void removeCategory(Long categoryId) {
    Category categoryToRemove = getCategoryById(categoryId);
    categoryRepository.delete(categoryToRemove);
  }
}
