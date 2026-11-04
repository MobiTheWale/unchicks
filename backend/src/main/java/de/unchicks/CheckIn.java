package de.unchicks;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
public class CheckIn {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) Long id;
    @ManyToOne(optional = false, fetch = FetchType.LAZY) Chicken chicken;
    @Column(nullable = false) String username;
    @Column(nullable = false) int rating; // 1..5
    @Column(length = 1000) String comment;
    @Column(nullable = false) Instant createdAt = Instant.now();

    protected CheckIn() {}
    public CheckIn(Chicken chicken, String username, int rating, String comment) {
        this.chicken = chicken; this.username = username; this.rating = rating; this.comment = comment;
    }
}
