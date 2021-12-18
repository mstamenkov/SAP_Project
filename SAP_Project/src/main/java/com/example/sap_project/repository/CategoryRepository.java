package com.example.sap_project.repository;

import com.example.sap_project.model.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

@Transactional
public interface CategoryRepository extends JpaRepository<Category, Long> {
    @Query("select c from Category c where c.categoryName = ?1")
    public Category getCategoryByName(String categoryName);

    @Query("SELECT CASE WHEN COUNT(c) > 0 THEN true ELSE false END FROM Category c WHERE c.categoryName = ?1")
    public boolean findCategoriesByName(String categoryName);

    @Query(nativeQuery = true, value = "select category_id from offers o where o.id = ?1")
    public Long getCategoryIdByOfferId(long offerId);

    @Modifying
    @Query("update Category c set c.categoryName = ?1 where c.id = ?2")
    public void changeCategoryName(String categoryName, long id);
}