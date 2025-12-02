package com.ims.stockmanagement.services;

import com.ims.stockmanagement.dtos.CategoryDTO;
import com.ims.stockmanagement.dtos.Response;
import com.ims.stockmanagement.exceptions.AlreadyExistsException;
import com.ims.stockmanagement.exceptions.NotFoundException;
import com.ims.stockmanagement.models.Category;
import com.ims.stockmanagement.repositories.CategoryRepository;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CategoryService {

    private final CategoryRepository categoryRepository;
    private final ModelMapper modelMapper;

    /**
     * Tüm kategorileri listele (READ - CRUD)
     */
    public Response getAllCategories() {
        List<Category> categories = categoryRepository.findAll();
        List<CategoryDTO> categoryDTOs = categories.stream()
                .map(category -> modelMapper.map(category, CategoryDTO.class))
                .collect(Collectors.toList());

        return Response.builder()
                .statusCode(200)
                .message("Categories retrieved successfully")
                .categoryList(categoryDTOs)
                .timestamp(LocalDateTime.now())
                .build();
    }

    /**
     * ID'ye göre kategori getir (READ - CRUD)
     */
    public Response getCategoryById(Long id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Category not found with id: " + id));

        CategoryDTO categoryDTO = modelMapper.map(category, CategoryDTO.class);

        return Response.builder()
                .statusCode(200)
                .message("Category retrieved successfully")
                .category(categoryDTO)
                .timestamp(LocalDateTime.now())
                .build();
    }

    /**
     * Yeni kategori oluştur (CREATE - CRUD)
     * Method Level Security: Authenticated users can create categories
     */
    @Transactional
    @PreAuthorize("isAuthenticated()")
    public Response createCategory(CategoryDTO categoryDTO) {
        // Aynı isimde kategori var mı kontrol et
        if (categoryRepository.existsByName(categoryDTO.getName())) {
            throw new AlreadyExistsException("Category already exists with name: " + categoryDTO.getName());
        }

        Category category = modelMapper.map(categoryDTO, Category.class);
        Category savedCategory = categoryRepository.save(category);
        CategoryDTO savedCategoryDTO = modelMapper.map(savedCategory, CategoryDTO.class);

        return Response.builder()
                .statusCode(201)
                .message("Category created successfully")
                .category(savedCategoryDTO)
                .timestamp(LocalDateTime.now())
                .build();
    }

    /**
     * Kategori güncelle (UPDATE - CRUD)
     * Method Level Security: Authenticated users can update categories
     */
    @Transactional
    @PreAuthorize("isAuthenticated()")
    public Response updateCategory(Long id, CategoryDTO categoryDTO) {
        Category existingCategory = categoryRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Category not found with id: " + id));

        // Başka bir kategori aynı ismi kullanıyor mu kontrol et
        if (!existingCategory.getName().equals(categoryDTO.getName()) &&
                categoryRepository.existsByName(categoryDTO.getName())) {
            throw new AlreadyExistsException("Category already exists with name: " + categoryDTO.getName());
        }

        existingCategory.setName(categoryDTO.getName());
        existingCategory.setDescription(categoryDTO.getDescription());

        Category updatedCategory = categoryRepository.save(existingCategory);
        CategoryDTO updatedCategoryDTO = modelMapper.map(updatedCategory, CategoryDTO.class);

        return Response.builder()
                .statusCode(200)
                .message("Category updated successfully")
                .category(updatedCategoryDTO)
                .timestamp(LocalDateTime.now())
                .build();
    }

    /**
     * Kategori sil (DELETE - CRUD)
     * Method Level Security: Only ADMIN can delete categories
     * This prevents accidental deletion of categories that have products
     */
    @Transactional
    @PreAuthorize("hasRole('ADMIN')")
    public Response deleteCategory(Long id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Category not found with id: " + id));

        categoryRepository.delete(category);

        return Response.builder()
                .statusCode(200)
                .message("Category deleted successfully")
                .timestamp(LocalDateTime.now())
                .build();
    }

    /**
     * İsme göre kategori ara
     */
    public Response searchCategoriesByName(String name) {
        List<Category> categories = categoryRepository.findByNameContainingIgnoreCase(name);
        List<CategoryDTO> categoryDTOs = categories.stream()
                .map(category -> modelMapper.map(category, CategoryDTO.class))
                .collect(Collectors.toList());

        return Response.builder()
                .statusCode(200)
                .message("Categories found: " + categoryDTOs.size())
                .categoryList(categoryDTOs)
                .timestamp(LocalDateTime.now())
                .build();
    }
}

