package com.kunzel.finance_tracker.category;

import java.net.URI;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;

import com.kunzel.finance_tracker.category.dtos.CategoryResponse;
import com.kunzel.finance_tracker.category.dtos.CreateCategoryRequest;
import com.kunzel.finance_tracker.category.dtos.UpdateCategoryRequest;

import jakarta.validation.Valid;

@RestController
@RequestMapping(CategoryController.BASE_PATH)
public class CategoryController {
  static final String BASE_PATH = "/api/v1/categories";

  private final CategoryService categoryService;

  public CategoryController(CategoryService categoryService) {
    this.categoryService = categoryService;
  }

  @GetMapping
  public ResponseEntity<List<CategoryResponse>> fetchCategories() {
    List<CategoryResponse> categories = categoryService.getAllCategories().stream().map(CategoryResponse::from).toList();
    return ResponseEntity.ok().body(categories);
  }

  @GetMapping("/{id}")
  public ResponseEntity<CategoryResponse> getCategory(@PathVariable("id") Long categoryId) {
    Category categoryToBeFound = categoryService.getCategoryById(categoryId);
    return ResponseEntity.ok().body(CategoryResponse.from(categoryToBeFound));
  }

  @PostMapping
  public ResponseEntity<CategoryResponse> createCategory(@Valid @RequestBody CreateCategoryRequest request,
      UriComponentsBuilder uriBuilder) {
    Category createdCategory = categoryService.createCategory(request.name(), request.type(), request.color());
    URI location = uriBuilder.path(BASE_PATH + "/{id}").buildAndExpand(createdCategory.getId()).toUri();

    return ResponseEntity.created(location).body(CategoryResponse.from(createdCategory));
  }

  @PutMapping("/{id}")
  public ResponseEntity<CategoryResponse> updateCategory(@PathVariable("id") Long categoryId,
      @Valid @RequestBody UpdateCategoryRequest request) {
    Category updatedCategory = categoryService.updateCategory(categoryId, request.name(), request.color(),
        request.type());
    return ResponseEntity.ok().body(CategoryResponse.from(updatedCategory));
  }

  @DeleteMapping("/{id}")
  public ResponseEntity<Void> removeCategory(@PathVariable("id") Long categoryId) {
    categoryService.removeCategory(categoryId);
    return ResponseEntity.noContent().build();
  }
}
