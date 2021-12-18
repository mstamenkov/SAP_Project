package com.example.sap_project.service;

import com.example.sap_project.exception.RegistrationException;
import com.example.sap_project.exception.UserException;
import com.example.sap_project.model.User;
import com.example.sap_project.repository.UserRepository;
import net.bytebuddy.utility.RandomString;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import javax.mail.MessagingException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.servlet.http.HttpServletRequest;
import java.io.UnsupportedEncodingException;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class UserDetailsServiceImpl implements UserDetailsService {

    @Autowired
    private UserRepository userRepo;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JavaMailSender mailSender;

    @Autowired
    HttpServletRequest servletRequest;

    @Async
    public void sendVerificationEmail(User user, String siteURL)
            throws MessagingException, UnsupportedEncodingException {
        String toAddress = user.getEmail();
        String subject = "Please verify your registration";
        String content = "Dear %s,<br>"
                + "Please click the link below to verify your registration:<br>"
                + "<h3><a href=\"%s\" target=\"_self\">VERIFY</a></h3>"
                + "Thank you,<br>"
                + "Your company name.";

        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message);

        helper.setFrom(new InternetAddress("noreply@localhost.com", "noreply"));
        helper.setTo(toAddress);
        helper.setSubject(subject);

        String verifyURL = siteURL + "/verify?code=" + user.getVerificationCode();
        content = String.format(content, user.getUsername(), verifyURL);

        helper.setText(content, true);

        if (!user.isAdmin()) mailSender.send(message);
        userRepo.save(user);
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepo.findByEmail(username);
        if (user == null) { //to do read for optional
            throw new UsernameNotFoundException("User not found");
        }
        return new UserDetailsImpl(user);
    }

    public void register(User user) {
        if (userRepo.findByEmail(user.getEmail()) != null)
            throw new RegistrationException("This email is already used");
        if (userRepo.findByUsername(user.getUsername()) != null)
            throw new RegistrationException("This username is already used");
        String encodedPassword = passwordEncoder.encode(user.getPassword());
        user.setPassword(encodedPassword);

        if (user.getUsername().equals("admin")) {
            user.setAdmin(true);
            user.setEnabled(true);
            return;
        }

        String randomCode = RandomString.make(64);
        user.setVerificationCode(randomCode);
        user.setEnabled(false);
    }

    public boolean verify(String verificationCode) {
        User user = userRepo.findByVerificationCode(verificationCode);

        if (user == null || user.isEnabled()) {
            return false;
        } else {
            user.setVerificationCode(null);
            user.setEnabled(true);
            userRepo.save(user);

            return true;
        }

    }

    public boolean verifyEmail(User user) {
        Pattern pattern = Pattern.compile("^[\\w-\\.]+@([\\w-]+\\.)+[\\w-]{2,4}$");
        Matcher matcher = pattern.matcher(user.getEmail());
        System.out.println(user.getEmail() + " " + matcher.matches());
        return matcher.matches();
    }

    public void setAdmin(long id, boolean status) throws UserException {
        Optional<User> user = userRepo.findById(id);
        if (user.isPresent()) {
            if (user.get().getUsername().equals("admin")) {
                throw new UserException("can't change admin status of user 'admin'");
            }
            user.ifPresent(value -> value.setAdmin(status));
            userRepo.save(user.get());
        }
    }

    public boolean isAdmin() {
        User user = userRepo.findByUsername(servletRequest.getRemoteUser());
        return user.isAdmin();
    }
}
