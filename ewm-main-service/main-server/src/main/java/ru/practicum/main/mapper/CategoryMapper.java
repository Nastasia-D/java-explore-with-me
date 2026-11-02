package ru.practicum.main.mapper;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import ru.practicum.main.dto.CategoryDto;
import ru.practicum.main.dto.NewCategoryDto;
import ru.practicum.main.model.Category;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class CategoryMapper {
    public static CategoryDto toDto(Category category) {
        if (category == null) {
            return null;
        }

        CategoryDto categoryDto = new CategoryDto();
        categoryDto.setId(category.getId());
        categoryDto.setName(category.getName());
        return categoryDto;
    }

    public static Category toCategory(NewCategoryDto newCategoryDto) {
        if (newCategoryDto == null) {
            return null;
        }

        Category category = new Category();
        category.setName(newCategoryDto.getName());
        return category;
    }
}
