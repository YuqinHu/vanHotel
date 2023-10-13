package com.example.pojo;

import javax.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@NoArgsConstructor
@Entity
@Table(name = "rooms")
public class Room
{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;
    
    @Column(name = "name", nullable = false, length = 64)
    private String name;

    @Column(name = "beds", nullable = false)
    private int beds;

    @Column(name = "people", nullable = false)
    private int people;

    @Column(name = "price", nullable = false)
    private double price;

    @Column(name = "size", nullable = false)
    private double size;

    @Column(name = "details", nullable = false, length = 1024)
    private String details;

    @Column(name = "avatar", nullable = false, length = 256)
    private String avatar;

    @Column(name = "hotel_name", nullable = false, length = 128)
    private String hotelName;
}


