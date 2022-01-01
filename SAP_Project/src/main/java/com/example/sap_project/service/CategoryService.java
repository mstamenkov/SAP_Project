package com.example.sap_project.service;

import com.example.sap_project.exception.RecordNotFoundException;
import com.example.sap_project.exception.UserException;
import com.example.sap_project.model.Category;
import com.example.sap_project.repository.CategoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class CategoryService {

    @Autowired
    private CategoryRepository categoryRepo;

    public void addEditCategory(Category category) throws UserException {
            if (!categoryRepo.findCategoriesByName(category.getCategoryName())) {
                if (category.getId() == null) categoryRepo.save(category);
                else categoryRepo.changeCategoryName(category.getCategoryName(), category.getId());
            } else throw new UserException("category already added");
    }

    public Category getCategoryById(Long id) throws RecordNotFoundException {
        Optional<Category> category = categoryRepo.findById(id);
        return category.orElseThrow(RecordNotFoundException::new);
    }
}
