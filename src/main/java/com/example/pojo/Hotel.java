package com.example.pojo;

import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.*;
import javax.persistence.*;

import org.springframework.web.servlet.FlashMapManager;

@Data
@NoArgsConstructor
@Entity
@Table(name = "hotels")
public class Hotel {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Column(name = "name", nullable = false, unique = true, length = 128)
    private String name;

    @Column(name = "star", nullable = false)
    private int star;
    
    @Column(name = "address", nullable = false, length = 256)
    private String address;

    @Column(name = "phone", nullable = false, length = 32)
    private String phone;

    @Column(name = "postcode", nullable = false, length = 20)
    private String postcode;

    @Column(name = "avatar", nullable = true, length = 256)
    private String avatar;

    @Column(name = "description", nullable = true, length = 1024)
    private String description;

    @Column(name = "quarantine")
    private boolean quarantine;

    @ManyToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    @JoinTable(
        name = "hotels_comments",
        joinColumns = @JoinColumn(name = "hotel_id"),
        inverseJoinColumns = @JoinColumn(name = "comment_id")
    )
    private Set<Comment> comments = new HashSet<>();

    @ManyToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    @JoinTable(
        name = "hotels_rooms",
        joinColumns = @JoinColumn(name = "hotel_id"),
        inverseJoinColumns = @JoinColumn(name = "room_id")
    )
    private Set<Room> rooms = new HashSet<>();
}
