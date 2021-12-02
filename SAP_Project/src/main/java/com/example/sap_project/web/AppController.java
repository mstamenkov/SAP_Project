package com.example.sap_project.web;

import com.example.sap_project.model.User;
import com.example.sap_project.repository.UserRepository;
import com.example.sap_project.service.UserDetailsServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.mail.MessagingException;
import javax.servlet.http.HttpServletRequest;
import java.io.UnsupportedEncodingException;

@Controller
public class AppController {

    @Autowired
    private UserRepository userRepo;

    @Autowired
    private UserDetailsServiceImpl service;

    @GetMapping("")
    public String viewHomePage() {
        return "index";
    }

    @GetMapping("/register")
    public String showRegistrationForm(Model model) {
        model.addAttribute("user", new User());

        return "signup_form";
    }

    @PostMapping("/process_register")
    public String processRegister(User user, HttpServletRequest request, RedirectAttributes redirAttrs) throws UnsupportedEncodingException, MessagingException {
        if (service.verifyEmail(user)) {
            service.register(user, getSiteURL(request));
            return "register_success";
        } else {
            redirAttrs.addFlashAttribute("error", "Invalid email. Please check you email and try again.");
            return "redirect:/register";
        }
    }

    private String getSiteURL(HttpServletRequest request) {
        String siteURL = request.getRequestURL().toString();
        return siteURL.replace(request.getServletPath(), "");
    }

    @GetMapping("/tempHomePage")
    public String homePage(Model model) {
        return "tempHomePage";
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
