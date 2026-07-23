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
    return categoryRepository.save(Category.createCustom(name, type));
  }

  public Category createDefaultCategory(String name, CategoryType type) {
    return categoryRepository.save(Category.createDefault(name, type));
  }
}
