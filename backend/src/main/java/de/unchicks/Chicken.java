package de.unchicks;

import jakarta.persistence.*;

@Entity
public class Chicken {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) Long id;
    @Column(nullable = false) String name;
    String vendor;       // Stand / Metzger / Restaurant
    String city;
    @Column(length = 1000) String description;

    protected Chicken() {}
    public Chicken(String name, String vendor, String city, String description) {
        this.name = name; this.vendor = vendor; this.city = city; this.description = description;
    }
}
