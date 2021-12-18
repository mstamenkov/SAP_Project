package com.example.sap_project.service;

import com.example.sap_project.exception.RecordNotFoundException;
import com.example.sap_project.exception.UserException;
import com.example.sap_project.model.Category;
import com.example.sap_project.model.User;
import com.example.sap_project.repository.CategoryRepository;
import com.example.sap_project.repository.OfferRepository;
import com.example.sap_project.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import java.util.Optional;

@Service
public class CategoryService {
    @Autowired
    private UserRepository userRepo;

    @Autowired
    private OfferRepository offerRepo;

    @Autowired
    HttpServletRequest servletRequest;

    @Autowired
    private CategoryRepository categoryRepo;

    public void addEditCategory(Category category) throws UserException {
        User user = userRepo.findByUsername(servletRequest.getRemoteUser());
        if (user.isAdmin()) {
            if (!categoryRepo.findCategoriesByName(category.getCategoryName())) {
                if (category.getId() == null) categoryRepo.save(category);
                else categoryRepo.changeCategoryName(category.getCategoryName(), category.getId());
            } else throw new UserException("category already added");
        } else throw new UserException("don't have access");
    }

    public Category getCategoryById(Long id) throws RecordNotFoundException {
        Optional<Category> category = categoryRepo.findById(id);

        if (category.isPresent()) {
            return category.get();
        } else {
            throw new RecordNotFoundException("No file record exist for given id");
        }
    }
}
