package de.unchicks;

import jakarta.persistence.*;

@Entity @Table(name = "app_user")
public class AppUser {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) Long id;
    @Column(nullable = false, unique = true) String username;
    @Column(nullable = false) String passwordHash;

    protected AppUser() {}
    public AppUser(String username, String passwordHash) { this.username = username; this.passwordHash = passwordHash; }
}
