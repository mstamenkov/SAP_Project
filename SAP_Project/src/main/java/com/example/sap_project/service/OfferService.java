package com.example.sap_project.service;

import com.example.sap_project.exception.RecordNotFoundException;
import com.example.sap_project.model.Offer;
import com.example.sap_project.model.User;
import com.example.sap_project.repository.OfferRepository;
import com.example.sap_project.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import java.sql.Date;
import java.util.ArrayList;
import java.util.Optional;

@Service
public class OfferService {
    @Autowired
    private UserRepository userRepo;

    @Autowired
    private OfferRepository offerRepo;

    @Autowired
    HttpServletRequest servletRequest;

    public void addOffer(Offer offer) {
        if (offer.getUser() == null) {
            User user = userRepo.findByUsername(servletRequest.getRemoteUser());
            offer.setUser(servletRequest.getRemoteUser());
            offer.setDateOfCreation(new Date(System.currentTimeMillis()));
            offer.setDateOfExpiry(null);
            offer.setActive(true);
            user.addOffer(offer);

            offerRepo.save(offer);
            userRepo.save(user);
        } else {
            offerRepo.save(offer);
        }
    }

    public void editOffer(Offer offer) {

    }

    public Offer getOfferById(Long id) throws RecordNotFoundException {
        Optional<Offer> offer = offerRepo.findById(id);

        if (offer.isPresent()) {
            return offer.get();
        } else {
            throw new RecordNotFoundException("No file record exist for given id");
        }
    }

    public ArrayList<Offer> getOffersByStatus(boolean isActive) {
        ArrayList<Offer> offers = (ArrayList<Offer>) offerRepo.findAll();
        offers.removeIf(offer -> offer.isActive() != isActive);
        return offers;
    }
}