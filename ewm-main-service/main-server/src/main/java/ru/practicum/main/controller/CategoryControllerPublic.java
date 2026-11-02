package ru.practicum.main.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.practicum.main.dto.CategoryDto;
import ru.practicum.main.service.CategoryService;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/categories")
public class CategoryControllerPublic {

    private final CategoryService categoryService;

    public CategoryControllerPublic(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    @GetMapping("/{catId}")
    public CategoryDto getCategoryById(@PathVariable Long catId) {
        log.info("Получен запрос на категорию с id={}", catId);
        return categoryService.getCategoryById(catId);
    }

    @GetMapping
    public List<CategoryDto> getCategories(@RequestParam(defaultValue = "0") int from, @RequestParam(defaultValue = "10") int size) {
        log.info("Получен запрос на список категорий: from={}, size={}", from, size);
        return categoryService.getCategories(from, size);
    }
}
