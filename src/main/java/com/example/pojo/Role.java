package com.example.pojo;

import javax.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;




@Data
@NoArgsConstructor
@Entity
@Table(name = "roles")
public class Role
{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;
    
    @Column(unique = true)
    private String name;
}


