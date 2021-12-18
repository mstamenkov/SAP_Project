package com.example.sap_project.repository;

import com.example.sap_project.model.Offer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Transactional
public interface OfferRepository extends JpaRepository<Offer, Long> {
    @Modifying
    @Query("delete from Offer o where o.id = ?1")
    public void deleteOfferById(long id);

    @Query("select o from Offer o where o.dateOfExpiry is not null")
    public List<Offer> getInactiveOffers();
}
