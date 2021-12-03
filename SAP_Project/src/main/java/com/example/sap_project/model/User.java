package com.example.sap_project.model;

import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.ColumnDefault;

import javax.persistence.*;
import java.util.List;

@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 45)
    private String email;

    @Column(nullable = false, length = 64)
    private String password;

    @Column(name = "username", nullable = false, length = 20)
    private String username;

    @Column(name = "verification_code", length = 64)
    private String verificationCode;

    private boolean enabled;

    @ColumnDefault("false")
    private boolean isAdmin;

    @OneToMany
    @Cascade(org.hibernate.annotations.CascadeType.DELETE)
    private List<Offer> entityList;

    public List<Offer> getEntityList() {
        return entityList;
    }

    public void setEntityList(List<Offer> entityList) {
        this.entityList = entityList;
    }

    public void addOffer(Offer offer) {
        entityList.add(offer);
    }

    public void removeOffer(Offer offer) {
        entityList.remove(offer);
    }

    public boolean isAdmin() {
        return isAdmin;
    }

    public void setAdmin(boolean admin) {
        isAdmin = admin;
    }

    public String getVerificationCode() {
        return verificationCode;
    }

    public void setVerificationCode(String verificationCode) {
        this.verificationCode = verificationCode;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }
}
