package ru.practicum.main.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.main.dto.CategoryDto;
import ru.practicum.main.dto.NewCategoryDto;
import ru.practicum.main.error.ConflictException;
import ru.practicum.main.error.NotFoundException;
import ru.practicum.main.mapper.CategoryMapper;
import ru.practicum.main.model.Category;
import ru.practicum.main.repository.CategoryRepository;
import ru.practicum.main.repository.EventRepository;

import java.util.ArrayList;
import java.util.List;

@Service
@Transactional(readOnly = true)
public class CategoryService {
    private final CategoryRepository categoryRepository;

    private final EventRepository eventRepository;

    public CategoryService(CategoryRepository categoryRepository,
                           EventRepository eventRepository) {
        this.categoryRepository = categoryRepository;
        this.eventRepository = eventRepository;
    }

    public CategoryDto getCategoryById(Long catId) {
        Category category = categoryRepository.findById(catId)
                .orElseThrow(() -> new NotFoundException("Категория  с id " + catId + " не найдена"));
        return CategoryMapper.toDto(category);
    }

    public List<CategoryDto> getCategories(int from, int size) {
        List<Category> categories = categoryRepository.findCategories(from, size);
        List<CategoryDto> categoryDtos = new ArrayList<>();
        for (Category category : categories) {
            CategoryDto categoryDto = CategoryMapper.toDto(category);
            categoryDtos.add(categoryDto);
        }
        return categoryDtos;
    }

    @Transactional
    public CategoryDto createCategory(NewCategoryDto newCategoryDto) {
        String name = newCategoryDto.getName();
        if (categoryRepository.existsByName(name)) {
            throw new ConflictException("Категория с именем " + name + " уже существует");
        }
        Category category = CategoryMapper.toCategory(newCategoryDto);
        Category saveCategory = categoryRepository.save(category);
        return CategoryMapper.toDto(saveCategory);
    }

    @Transactional
    public CategoryDto updateCategory(Long catId, CategoryDto categoryDto) {
        Category category = categoryRepository.findById(catId)
                .orElseThrow(() ->
                        new NotFoundException("Категория с id " + catId + " не найдена"));
        String newName = categoryDto.getName();
        if (!category.getName().equals(newName)
                && categoryRepository.existsByName(newName)) {
            throw new ConflictException(
                    "Категория с именем " + newName + " уже существует");
        }

        category.setName(newName);
        Category updatedCategory = categoryRepository.save(category);
        return CategoryMapper.toDto(updatedCategory);
    }

    @Transactional
    public void deleteCategory(Long catId) {
        Category category = categoryRepository.findById(catId)
                .orElseThrow(() ->
                        new NotFoundException("Категория с id " + catId + " не найдена"));

        if (eventRepository.existsByCategory_Id(catId)) {
            throw new ConflictException(
                    "Нельзя удалить категорию, связанную с событиями");
        }

        categoryRepository.delete(category);
    }
}

