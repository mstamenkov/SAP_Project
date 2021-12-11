package com.example.sap_project.service;

import com.example.sap_project.exception.RecordNotFoundException;
import com.example.sap_project.exception.UserException;
import com.example.sap_project.model.Category;
import com.example.sap_project.model.Offer;
import com.example.sap_project.model.User;
import com.example.sap_project.repository.CategoryRepository;
import com.example.sap_project.repository.OfferRepository;
import com.example.sap_project.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import javax.mail.MessagingException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.servlet.http.HttpServletRequest;
import java.io.UnsupportedEncodingException;
import java.sql.Date;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class OfferService {
    @Autowired
    private UserRepository userRepo;

    @Autowired
    private CategoryRepository categoryRepo;

    @Autowired
    private OfferRepository offerRepo;

    @Autowired
    HttpServletRequest servletRequest;

    @Autowired
    private JavaMailSender mailSender;

    public void addUpdateOffer(Offer offer, Category categoryName) {
        if (offer.getUser().isEmpty()) {
            Category category = categoryRepo.getCategoryByName(categoryName.getCategoryName());
            User user = userRepo.findByUsername(servletRequest.getRemoteUser());
            offer.setUser(servletRequest.getRemoteUser());
            offer.setDateOfCreation(new Date(System.currentTimeMillis()));
            offer.setDateOfExpiry(null);
            offer.setActive(true);
            category.addOffer(offer);
            user.addOffer(offer);

            offerRepo.save(offer);
            userRepo.save(user);
            categoryRepo.save(category);
        } else {
            if (offer.isActive()) {
                offer.setDateOfExpiry(null);
            } else {
                offer.setDateOfExpiry(new Date(System.currentTimeMillis()));
            }
            offerRepo.save(offer);
        }
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

    public boolean ownerCheck(long id) {
        Offer offer = offerRepo.getById(id);
        return (offer.getUser().equals(servletRequest.getRemoteUser()));
    }

    public void deleteOffer(long id) {
        Offer offer = offerRepo.getById(id);
        User user = userRepo.findByUsername(offer.getUser());
        user.removeOffer(offer);
        userRepo.save(user);
        offerRepo.deleteOfferById(offer.getId());
    }

    public void addFavorite(Offer offer, User user) throws UserException {
        if (!user.isFavoritePresent(offer) && !offer.getUser().equals(user.getUsername())) {
            user.addFavorite(offer);
            userRepo.save(user);
        } else throw new UserException("cant add offer to fav");

    }

    public void removeFavorite(Offer offer) {
        User user = userRepo.findByUsername(servletRequest.getRemoteUser());
        user.removeFavorite(offer);
        userRepo.save(user);
    }

    public List<Offer> getFavoriteOffers() {
        User user = userRepo.findByUsername(servletRequest.getRemoteUser());
        return user.getFavoritesList();
    }

    @Async
    public void sendFavEmail(Offer offer)
            throws MessagingException, UnsupportedEncodingException {
        User user = userRepo.findByUsername(offer.getUser());
        String toAddress = user.getEmail();
        String subject = "Added offer to Favorite";
        String content = "Dear [[name]],<br>"
                + "Your offer for [[offerTitle]] was added to Favorite<br>"
                + "Lorem Ipsum";

        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message);

        helper.setFrom(new InternetAddress("noreply@localhost.com", "noreply"));
        helper.setTo(toAddress);
        helper.setSubject(subject);

        content = content.replace("[[name]]", user.getUsername());
        content = content.replace("[[offerTitle]]", offer.getTitle());

        helper.setText(content, true);

        mailSender.send(message);

    }
}
