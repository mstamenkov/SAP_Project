package com.example.sap_project.web;

import com.example.sap_project.exception.RecordNotFoundException;
import com.example.sap_project.exception.RegistrationException;
import com.example.sap_project.exception.UserException;
import com.example.sap_project.model.Category;
import com.example.sap_project.model.Offer;
import com.example.sap_project.model.User;
import com.example.sap_project.repository.CategoryRepository;
import com.example.sap_project.repository.OfferRepository;
import com.example.sap_project.repository.UserRepository;
import com.example.sap_project.service.CategoryService;
import com.example.sap_project.service.OfferService;
import com.example.sap_project.service.UserDetailsServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.repository.query.Param;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.mail.MessagingException;
import javax.servlet.http.HttpServletRequest;
import java.io.UnsupportedEncodingException;
import java.text.ParseException;
import java.util.Optional;

@Controller
@EnableGlobalMethodSecurity(prePostEnabled = true)
public class AppController {

    @Autowired
    private UserRepository userRepo;

    @Autowired
    private OfferRepository offerRepo;

    @Autowired
    private CategoryRepository categoryRepo;

    @Autowired
    private UserDetailsServiceImpl service;

    @Autowired
    private OfferService offerService;

    @Autowired
    private CategoryService categoryService;

    @Autowired
    HttpServletRequest servletRequest;

    @GetMapping("")
    public String viewHomePage() {
        return "redirect:/home";
    }

    @PostMapping("/create")
    public String createOffer(Offer offerEntity, Category category) {
        if (offerEntity.getUser().isEmpty()) {
            offerService.addOffer(offerEntity, category);
        } else {
            offerService.updateOffer(offerEntity);
        }
        return "redirect:/home";
    }

    @GetMapping(path = {"/create", "/create/{id}"})
    public String createOffer(Model model, @PathVariable("id") Optional<Long> id, RedirectAttributes redirAttrs) throws RecordNotFoundException {
        model.addAttribute("categories", categoryRepo.findAll());
        if (id.isPresent()) {
            if (offerService.ownerCheck(id.get())) {
                model.addAttribute("offer", offerService.getOfferById(id.get()));
                model.addAttribute("category", categoryRepo.getById(categoryRepo.getCategoryIdByOfferId(offerService.getOfferById(id.get()).getId())));
            } else {
                redirAttrs.addFlashAttribute("error", "Unknown offer id");
                return "redirect:/home";
            }
        } else {
            model.addAttribute("category", new Category());
            model.addAttribute("offer", new Offer());
        }
        return "add_offer";
    }

    @GetMapping("/myoffers")
    public String listOffers(Model model) {
        User user = userRepo.findByUsername(servletRequest.getRemoteUser());
        model.addAttribute("offers", user.getEntityList());
        model.addAttribute("categories", categoryRepo.findAll());
        return "list_offers";
    }

    @GetMapping("/delete/{id}")
    public String deleteOffer(@PathVariable("id") long id) {
        offerService.deleteOffer(id);
        return "redirect:/myoffers";
    }

    @GetMapping("/favorite/{id}")
    public String addFavorite(@PathVariable("id") long id, RedirectAttributes redirAttrs) throws RecordNotFoundException {
        try {
            offerService.addFavorite(offerService.getOfferById(id), userRepo.findByUsername(servletRequest.getRemoteUser()));
            offerService.sendFavEmail(offerService.getOfferById(id));
        } catch (UserException e) {
            redirAttrs.addFlashAttribute("error", e.getMessage());
            return "redirect:/home";
        } catch (MessagingException | UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        redirAttrs.addFlashAttribute("success", "fav added");
        return "redirect:/home";
    }

    @GetMapping("/rfav/{id}")
    public String removeFavorite(@PathVariable("id") long id, Model model, RedirectAttributes redirAttrs) throws RecordNotFoundException {
        offerService.removeFavorite(offerService.getOfferById(id));
        redirAttrs.addFlashAttribute("error", "fav removed");
        return "redirect:/myfav";
    }

    @GetMapping("/myfav")
    public String listFavorite(Model model) {
        model.addAttribute("offers", offerService.getFavoriteOffers());
        model.addAttribute("categories", categoryRepo.findAll());
        return "favorites";
    }

    @PreAuthorize("@userDetailsServiceImpl.admin")
    @GetMapping(path = {"/addcategory", "/addcategory/{id}"})
    public String addEditCategory(@PathVariable("id") Optional<Long> id, Model model) throws RecordNotFoundException {
        if (id.isPresent()) {
            model.addAttribute("category", categoryService.getCategoryById(id.get()));
        } else {
            model.addAttribute("category", new Category());
        }
        return "add_category";
    }

    @PreAuthorize("@userDetailsServiceImpl.admin")
    @PostMapping("/addcategory")
    public String postCategory(Category category, RedirectAttributes redirAttrs) {
        try {
            categoryService.addEditCategory(category);
            redirAttrs.addFlashAttribute("successCategory", "ADMIN MESSAGE: Category successfully added/edited");
        } catch (UserException e) {
            redirAttrs.addFlashAttribute("errorCategory", "ADMIN MESSAGE: " + e.getMessage());
            return "redirect:/adminpage";
        }
        return "redirect:/adminpage";
    }

    @PreAuthorize("@userDetailsServiceImpl.admin")
    @GetMapping("/adminpage")
    public String listUsers(Model model) {
        model.addAttribute("users", userRepo.findAll());
        model.addAttribute("categories", categoryRepo.findAll());
        return "admin_page";
    }

    @PreAuthorize("@userDetailsServiceImpl.admin")
    @GetMapping("/addadmin/{id}")
    public String addAdmin(@PathVariable("id") long id) {
        service.setAdmin(id, true);
        return "redirect:/adminpage";
    }

    @PreAuthorize("@userDetailsServiceImpl.admin")
    @GetMapping("/removeadmin/{id}")
    public String removeAdmin(@PathVariable("id") long id, RedirectAttributes redirAttrs) {
        try {
            service.setAdmin(id, false);
        } catch (UserException e) {
            redirAttrs.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/adminpage";
    }

    @GetMapping("/newoffers")
    public String newOffers() {
        return "new_offers_list";
    }

    @PostMapping("/newoffers")
    public String postNewOffers(@RequestParam(value = "endDate") String endDate, @RequestParam(value = "startDate") String startDate, Model model, RedirectAttributes redirAttrs) throws ParseException {
        try {
            model.addAttribute("offers", offerService.findByDate(startDate, endDate, true));
        } catch (UserException e) {
            redirAttrs.addFlashAttribute("error", e.getMessage());
            return "redirect:/newoffers";
        }
        model.addAttribute("categories", categoryRepo.findAll());
        return "new_offers_list";
    }

    @PreAuthorize("@userDetailsServiceImpl.admin")
    @GetMapping("/expoffers")
    public String expiredOffers() {
        return "exp_offers_list";
    }

    @PreAuthorize("@userDetailsServiceImpl.admin")
    @PostMapping("/expoffers")
    public String postExpiredOffers(@RequestParam(value = "endDate") String endDate, @RequestParam(value = "startDate") String startDate, Model model, RedirectAttributes redirAttrs) throws ParseException {
        try {
            model.addAttribute("offers", offerService.findByDate(startDate, endDate, false));
        } catch (UserException e) {
            redirAttrs.addFlashAttribute("error", e.getMessage());
            return "redirect:/expoffers";
        }
        model.addAttribute("categories", categoryRepo.findAll());
        return "exp_offers_list";
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
                service.register(user);
                service.sendVerificationEmail(user, getSiteURL(request));
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
        model.addAttribute("categories", categoryRepo.findAll());
        model.addAttribute("user", userRepo.findByUsername(servletRequest.getRemoteUser()));
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

    @GetMapping("/login")
    public String login() {
        return "login";
    }
}
