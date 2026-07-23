package com.kunzel.finance_tracker.category;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.kunzel.finance_tracker.category.dtos.CategoryResponse;
import com.kunzel.finance_tracker.category.dtos.CreateCategoryRequest;
import com.kunzel.finance_tracker.category.dtos.UpdateCategoryRequest;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/categories")
public class CategoryController {
  private final CategoryService categoryService;

  public CategoryController(CategoryService categoryService) {
    this.categoryService = categoryService;
  }

  @GetMapping
  public ResponseEntity<List<CategoryResponse>> fetchCategories() {
    List<CategoryResponse> categories = categoryService.getAllCategories().stream().map(CategoryResponse::new).toList();
    return ResponseEntity.ok().body(categories);
  }

  @GetMapping("/{id}")
  public ResponseEntity<CategoryResponse> getCategory(@PathVariable("id") Long categoryId) {
    Category categoryToBeFound = categoryService.getCategoryById(categoryId);
    return ResponseEntity.ok().body(new CategoryResponse(categoryToBeFound));
  }

  @PostMapping
  public ResponseEntity<CategoryResponse> createCategory(@Valid @RequestBody CreateCategoryRequest request) {
    Category createdCategory = categoryService.createCategory(request.name(), request.type());
    return ResponseEntity.status(HttpStatus.CREATED).body(new CategoryResponse(createdCategory));
  }

  @PutMapping("/{id}")
  public ResponseEntity<CategoryResponse> updateCategory(@PathVariable("id") Long categoryId,
      @Valid @RequestBody UpdateCategoryRequest request) {
    Category updatedCategory = categoryService.updateCategory(categoryId, request.name(), request.type());
    return ResponseEntity.ok().body(new CategoryResponse(updatedCategory));
  }
}
