package com.example.sap_project.web;

import com.example.sap_project.exception.RecordNotFoundException;
import com.example.sap_project.exception.RegistrationException;
import com.example.sap_project.model.Offer;
import com.example.sap_project.model.User;
import com.example.sap_project.repository.OfferRepository;
import com.example.sap_project.repository.UserRepository;
import com.example.sap_project.service.OfferService;
import com.example.sap_project.service.UserDetailsServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.mail.MessagingException;
import javax.servlet.http.HttpServletRequest;
import java.io.UnsupportedEncodingException;
import java.util.Optional;

@Controller
public class AppController {

    @Autowired
    private UserRepository userRepo;

    @Autowired
    private OfferRepository offerRepo;

    @Autowired
    private UserDetailsServiceImpl service;

    @Autowired
    private OfferService offerService;

    @GetMapping("")
    public String viewHomePage() {
        return "index";
    }


    @PostMapping("/create")
    public String createOffer(Offer offerEntity) {
        System.out.println(offerEntity.getPhone() + offerEntity.getDescription());
        offerService.addOffer(offerEntity);
        return "redirect:/home";
    }

    @GetMapping(path = {"/create", "/create/{id}"})
    public String createOffer(Model model, @PathVariable("id") Optional<Long> id) throws RecordNotFoundException {
        if (id.isPresent()) {
            model.addAttribute("offer", offerService.getOfferById(id.get()));
            return "add_offer";
        } else {
            model.addAttribute("offer", new Offer());
            return "add_offer";
        }

    }


    /////////////////////////////////////////////////////////////////////////////////////////


    @GetMapping("/register")
    public String showRegistrationForm(Model model) {
        model.addAttribute("user", new User());

        return "signup_form";
    }

    @PostMapping("/process_register")
    public String processRegister(User user, HttpServletRequest request, RedirectAttributes redirAttrs) throws UnsupportedEncodingException, MessagingException {
        try {
            if (service.verifyEmail(user)) {
                service.register(user, getSiteURL(request));
                return "register_success";
            } else {
                redirAttrs.addFlashAttribute("error", "Invalid email. Please check you email and try again.");
                return "redirect:/register";
            }
        } catch (RegistrationException e) {
            redirAttrs.addFlashAttribute("error", e.getMessage());
            return "redirect:/register";
        }
    }

    private String getSiteURL(HttpServletRequest request) {
        String siteURL = request.getRequestURL().toString();
        return siteURL.replace(request.getServletPath(), "");
    }

    @GetMapping("/home")
    public String homePage(Model model) {
        model.addAttribute("offers", offerService.getOffersByStatus(true));
        return "home";
    }

    @GetMapping("/verify")
    public String verifyUser(@Param("code") String code, Model model) {
        if (service.verify(code)) {
            model.addAttribute("verify", "activated !");
        } else {
            model.addAttribute("verify", "not activated :(");
        }
        return "emailVerify";
    }
}
